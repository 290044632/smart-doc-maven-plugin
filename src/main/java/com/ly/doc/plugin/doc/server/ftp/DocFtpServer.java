package com.ly.doc.plugin.doc.server.ftp;

import com.ly.doc.plugin.config.ApiConfigWrapper;
import com.ly.doc.plugin.config.FtpServerConfig;
import com.ly.doc.plugin.doc.server.AbstractDocServer;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DocFtpServer extends AbstractDocServer {
    private static final String API_DOCS_DIR = "api-docs";

    private final Log log = new SystemStreamLog();

    public DocFtpServer(ApiConfigWrapper apiConfigWrapper, MavenProject mavenProject) {
        super(apiConfigWrapper, mavenProject);
    }

    @Override
    protected void runWithArg(ApiConfigWrapper apiConfigWrapper, MavenProject mavenProject) {
        this.ftp(apiConfigWrapper, mavenProject);
    }

    public void ftp(ApiConfigWrapper apiConfig, MavenProject mavenProject) {
        FtpServerConfig ftpConfig = apiConfig.getDocServerConfig().getFtpConfig();
        String host = ftpConfig.getHost();
        if (host == null || host.isEmpty()) {
            return;
        }
        String outPath = apiConfig.getOutPath();
        if (null == outPath || outPath.isEmpty()) {
            return;
        }
        File localRoot = new File(outPath);
        if (!localRoot.exists() || localRoot.isFile()) {
            return;
        }
        File[] listFiles = localRoot.listFiles();
        if (null == listFiles || listFiles.length == 0) {
            return;
        }
        FTPClient ftpClient = new FTPClient();
        try {
            String hostname = host;
            int port = 21;
            if (host.contains(":")) {
                String[] hosts = host.split(":");
                hostname = hosts[0];
                try {
                    port = Integer.parseInt(hosts[1]);
                } catch (Exception e) {
                    log.error("FTP server port invalid: " + hosts[1]);
                }
            }
            ftpClient.connect(hostname, port);
            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect();
                log.error("FTP server refused connection.Details: " + ftpConfig + " , replyCode=" + replyCode);
                return;
            }
            ftpClient.enterLocalPassiveMode();
            // 设置以二进制流的方式传输
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
            ftp(ftpClient, ftpConfig.getRootDir(), localRoot, (Void) -> {
                this.createAndChangeFtpDir(ftpClient, API_DOCS_DIR, true);
                this.createAndChangeFtpDir(ftpClient, mavenProject.getArtifactId(), true);
                this.createAndChangeFtpDir(ftpClient, mavenProject.getVersion(), true);
            });
            ftpClient.logout();
        } catch (Exception e) {
            log.error("Ftp server connect error", e);
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    log.error("Ftp server disconnect error", e);
                }
            }
        }
    }

    private void ftp(FTPClient ftpClient, String ftpRoot, File localRoot) {
        ftp(ftpClient, ftpRoot, localRoot, null);
    }

    private void ftp(FTPClient ftpClient, String ftpRoot, File localRoot, Consumer<Void> createFtpRootDirFun) {
        File[] listFiles = localRoot.listFiles();
        if (null == listFiles || listFiles.length == 0) {
            return;
        }
        boolean gotoed = this.changeFtpDir(ftpClient, ftpRoot);
        if (!gotoed) {
            return;
        }
        if (createFtpRootDirFun != null) {
            createFtpRootDirFun.accept(null);
        }
        for (File file : listFiles) {
            String name = file.getName();
            if (file.isDirectory()) {
                createFtpDir(ftpClient, name);
                ftp(ftpClient, name, file);
            } else {
                try {
                    boolean ok = ftpClient.storeFile(name, Files.newInputStream(file.toPath()));
                    log.info("FTP server finish upload file [" + name + "], Status=" + ok + "......");
                } catch (IOException e) {
                    log.error("FTP server create file [" + name + "] error ", e);
                }
            }
        }
        this.backParentDirectory(ftpClient);
    }

    private void backParentDirectory(FTPClient ftpClient) {
        try {
            ftpClient.changeToParentDirectory();
            log.info("FTP server back parent directory......");
        } catch (IOException e) {
            log.error("FTP server back parent directory error ", e);
        }
    }

    private void createFtpDir(FTPClient ftpClient, String dir) {
        this.createAndChangeFtpDir(ftpClient, dir, false);
    }

    private void createAndChangeFtpDir(FTPClient ftpClient, String dir, boolean cd) {
        try {
            log.info("dir ="+dir);
            String[] names = ftpClient.listNames();
            if (null == names || !Arrays.asList(names).contains(dir)) {
                ftpClient.makeDirectory(dir);
                log.info("FTP server create directory [" + dir + "] success......");
            }
            if (cd) {
                this.changeFtpDir(ftpClient, dir);
            }
        } catch (IOException e) {
            log.error("FTP server create directory [" + dir + "] error", e);
        }
    }

    private boolean changeFtpDir(FTPClient ftpClient, String dir) {
        boolean changed = false;
        log.info("change dir ="+dir);
        try {
            changed = ftpClient.changeWorkingDirectory(dir);
            if (!changed) {
                log.warn("FTP server change directory  [" + dir + "] faild......");
            } else {
                log.info("FTP server change directory [" + dir + "] success......");
            }
        } catch (IOException e) {
            log.error("FTP server change directory [" + dir + "] error", e);
        }
        return changed;
    }
}
