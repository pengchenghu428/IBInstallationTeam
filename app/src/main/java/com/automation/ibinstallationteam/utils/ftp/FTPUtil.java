package com.automation.ibinstallationteam.utils.ftp;

import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengchenghu on 2019/4/14.
 * Author Email: 15651851181@163.com
 * Describe: FTP上传/下载
 */

public class FTPUtil {
    private final static String TAG = "FTPUtil";
    /**
     * 服务器名.
     */
    private String ip;

    /**
     * 服务器端口号
     */
    private int port;

    /**
     * 用户名.
     */
    private String userName;

    /**
     * 密码.
     */
    private String password;

    /**
     * FTP连接.
     */
    private FTPClient ftpClient;

    /**
     * FTP列表.
     */
    private List<FTPFile> list;

    /**
     * FTP根目录.
     */
    public static final String BASE_REMOTE_PATH = "../var/ftp/smartNacelle";

    /**
     * FTP当前目录.
     */
    private String currentPath = "";

    /**
     * 统计流量.
     */
    private double response;

    /**
     * 构造函数.
     *
     * @param ip
     *            hostName 服务器名
     * @param user
     *            userName 用户名
     * @param pass
     *            password 密码
     */
    public FTPUtil(String ip, int port, String user, String pass) {
        this.ip = ip;
        this.port = port;
        this.userName = user;
        this.password = pass;
        this.ftpClient = new FTPClient();
        this.list = new ArrayList<FTPFile>();
    }

    /**
     * 打开FTP服务.
     *
     * @throws IOException
     */
    public boolean openConnect() throws IOException {
        // 中文转码
        ftpClient.setControlEncoding("UTF-8");
        int reply; // 服务器响应值
        // 连接至服务器
        ftpClient.connect(ip, port);
        // 获取响应值
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        }
        // 登录到服务器
        boolean login = ftpClient.login(userName, password);
        // 获取响应值
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        } else {
            // 获取登录信息
            FTPClientConfig config = new FTPClientConfig(ftpClient.getSystemType().split(" ")[0]);
            config.setServerLanguageCode("zh");
            ftpClient.configure(config);
            // 使用被动模式设为默认
            ftpClient.enterLocalPassiveMode();
            // 二进制文件支持
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            // 更换当前工作目录到根目录下
            if(!ftpClient.changeWorkingDirectory(BASE_REMOTE_PATH)) {
                Log.i(TAG, "更换至根目录失败");
                return false;
            }
            Log.i(TAG, "更换至根目录成功：" + ftpClient.printWorkingDirectory());
        }

        return login;
    }

    /**
     * 关闭FTP服务.
     *
     * @throws IOException
     */
    public void closeConnect() throws IOException {
        if (ftpClient != null) {
            if (ftpClient.isConnected()) {
                // 登出FTP
                ftpClient.logout();
                // 断开连接
                ftpClient.disconnect();
                System.out.println("logout");
            }
        }
    }

    /**
     * 列出FTP下所有文件.
     *
     * @param remotePath
     *            服务器目录
     * @return FTPFile集合
     * @throws IOException
     */
    public List<FTPFile> listFiles(String remotePath) throws IOException {
        if (ftpClient != null) {
            // 获取文件
            try {
                FTPFile[] files = ftpClient.listFiles(remotePath);
                if (files != null && files.length > 0) {
                    // 遍历并且添加到集合
                    for (FTPFile file : files) {
                        list.add(file);
                    }
                }
            } catch (Exception e) {
                Log.e("TAG", "请稍等...");
            }
        }
        return list;
    }

    /**
     * 列出FTP当前目录下所有文件.
     * @return FTPFile集合
     * @throws IOException
     */
    public List<FTPFile> listCurrentFiles() throws IOException {
        if (ftpClient != null) {
            // 获取文件
            try {
                FTPFile[] files = ftpClient.listFiles();
                if (files != null && files.length > 0) {
                    // 遍历并且添加到集合
                    for (FTPFile file : files) {
                        list.add(file);
                    }
                }
            } catch (Exception e) {
                Log.e("TAG", "请稍等...");
            }
        }
        return list;
    }

    /**
     * 下载.
     *
     * @param remotePath
     *            FTP目录
     * @param fileName
     *            文件名
     * @param localPath
     *            本地目录
     * @return Result
     * @throws IOException
     */
    public void download(String remotePath, String fileName, String localPath) throws IOException {
        // 初始化FTP当前目录
        currentPath = remotePath;
        // 初始化当前流量
        response = 0;
        // 更换当前工作目录到工作目录下
        ftpClient.changeWorkingDirectory(currentPath);
        Log.i(TAG, "更换至工作目录：" + ftpClient.printWorkingDirectory());
        // 得到FTP当前目录下所有文件
        FTPFile[] ftpFiles = ftpClient.listFiles();
        // 循环遍历
        for (FTPFile ftpFile : ftpFiles) {
            // 找到需要下载的文件
            if (ftpFile.getName().equals(fileName)) {
                System.out.println("download...");
                // 创建本地目录
                File file = new File(localPath + "/" + fileName);
                // 下载前时间
                if (ftpFile.isDirectory()) {
                    // 下载多个文件
                    //downloadMany(file);
                } else {
                    // 下载当个文件
                    downloadSingle(file, ftpFile);
                }
            }
        }
    }

    /**
     * 下载单个文件.
     *
     * @param localFile
     *            本地目录
     * @param ftpFile
     *            FTP目录
     * @return true下载成功, false下载失败
     * @throws IOException
     */
    private boolean downloadSingle(File localFile, FTPFile ftpFile) throws IOException {
        boolean flag = true;
        // 创建输出流
        OutputStream outputStream = new FileOutputStream(localFile);
        // 统计流量
        response += ftpFile.getSize();
        // 下载单个文件
        flag = ftpClient.retrieveFile(localFile.getName(), outputStream);
        // 关闭文件流
        outputStream.close();
        return flag;
    }


    /*
     * pengchenghu 定制功能
     */
    /*
     * 下载
     */
    // 初始化下载文件环境
    public void downloadingInit(String remotePath) throws IOException {
        // 初始化FTP当前目录
        currentPath = remotePath;
        // 初始化当前流量
        response = 0;
        // 二进制文件支持
        ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        // 使用被动模式设为默认
        ftpClient.enterLocalPassiveMode();
        // 设置模式
        ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
        // 改变FTP目录
        if(ftpClient.changeWorkingDirectory(currentPath))
            Log.i(TAG, "更换至工作目录：" + ftpClient.printWorkingDirectory());
    }
    // 刷新文件
    // direction: 0 寻找最新的num的图片 1 从最新的文件递归到当前文件 2 从当前文件递归12张图片
    public List<String> getDownloadFileName(int direction, String filename) throws IOException {
        List<String> filenames = new ArrayList<>();
        // 得到FTP当前目录下所有文件
        FTPFile[] ftpFiles = ftpClient.listFiles();
        // 循环遍历
        int fileLength = ftpFiles.length;  // 文件总数
        int num = 0; // 更新的文件数目
        switch(direction){
            case 0:  // 首次进来选取20张吊篮图片
                while((fileLength-num-1) >= 0 && (num < 20)){
                    if(!ftpFiles[fileLength-num-1].getName().contains(".jpg")) {
                        num++;
                        continue;
                    }
                    filenames.add(ftpFiles[fileLength-num-1].getName());
                    num++;
                }
                break;
            case 1:  // 最新的照片找目前的位置
                while((fileLength-num-1) > 0 && !ftpFiles[fileLength-num-1].getName().equals(filename)){
                    if(!ftpFiles[fileLength-num-1].getName().contains(".jpg")) {
                        num++;
                        continue;
                    }
                    filenames.add(ftpFiles[fileLength-num-1].getName());
                    num++;
                }
                break;
            case 2:  // 寻找历史的20张图片
                int index = 0;
                for(int i=0; i < fileLength; i++){
                    if(filename.equals(ftpFiles[i].getName())){
                        index = i;
                        break;
                    }
                }
                while((index-num-1) > 0 && (num < 20)){
                    if(!ftpFiles[fileLength-num-1].getName().contains(".jpg")) {
                        num++;
                        continue;
                    }
                    filenames.add(ftpFiles[index-num-1].getName());
                    num++;
                }
                break;
        }
        Log.i(TAG, filenames.toString());
        return filenames;
    }
    // 下载文件
    public void downloadFile(String fileName, String localPath) throws IOException {
        // 得到FTP当前目录下所有文件
        FTPFile[] ftpFiles = ftpClient.listFiles();
        // 反向循环遍历
        for (int i=ftpFiles.length-1; i>=0; i--) {
            // 找到需要下载的文件
            if (ftpFiles[i].getName().equals(fileName)) {
                // 创建本地目录
                File file = new File(localPath + "/" + fileName);
                downloadSingle(file, ftpFiles[i]);
            }
        }
    }
    /*
     * 上传
     */
    // 初始化上传文件环境
    public void uploadingInit(String remotePath) throws IOException {
        // 初始化FTP当前目录
        currentPath = remotePath;
        // 初始化当前流量
        response = 0;
        // 二进制文件支持
        ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        // 使用被动模式设为默认
        ftpClient.enterLocalPassiveMode();
        // 设置模式
        ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
        // 改变FTP目录
        if(!ftpClient.changeWorkingDirectory(currentPath)){
            Log.i(TAG, "更换至工作目录：" + ftpClient.printWorkingDirectory());
            String[] directoryStrArray = currentPath.split("/");
            for(int i=0; i<directoryStrArray.length; i++){
                if(!ftpClient.changeWorkingDirectory(directoryStrArray[i])){
                    ftpClient.makeDirectory(directoryStrArray[i]);
                    ftpClient.changeWorkingDirectory(directoryStrArray[i]);
                }
                Log.i(TAG, "更换至工作目录：" + ftpClient.printWorkingDirectory());
            }
        }
        Log.i(TAG, "更换至工作目录：" + ftpClient.printWorkingDirectory());
    }
    /**
     * 上传单个文件.
     *
     * @param localFile
     *            本地文件
     * @return true上传成功, false上传失败
     * @throws IOException
     */
    public void uploadingSingleRenameFile(File localFile, String fileName) throws IOException {
        // 创建输入流
        InputStream inputStream = new FileInputStream(localFile);
        // 统计流量
        response += (double) inputStream.available() / 1;
        // 上传单个文件
        ftpClient.storeFile(fileName, inputStream);
        inputStream.close();
    }
}
