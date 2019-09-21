package com.tincery.common.util;

/**
 * @author: gxz
 * @date: 2019/9/20 13:11
 **/

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Zip文件工具类
 *
 * @author gxz
 */
public class FileUtil {
    private static final String ZIP_SUFFIX = ".zip";

    /**
     * 把文件压缩成zip格式
     *
     * @param files       需要压缩的文件
     * @param zipFilePath 压缩后的zip文件路径   ,如"D:/test/aa.zip";
     */
    public static void compressFiles2Zip(File[] files, String zipFilePath) {
        if (files != null && files.length > 0) {
            if (StringUtils.endsWithIgnoreCase(zipFilePath, ZIP_SUFFIX)) {
                File zipFile = new File(zipFilePath);
                try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(zipFile)) {
                    //Use Zip64 extensions for all entries where they are required
                    zipArchiveOutputStream.setUseZip64(Zip64Mode.AsNeeded);
                    //再用ZipArchiveOutputStream写到压缩文件中
                    for (File file : files) {
                        if (file != null) {
                            ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(file, file.getName());
                            zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);
                            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                                byte[] buffer = new byte[1024 * 5];
                                int len;
                                while ((len = is.read(buffer)) != -1) {
                                    //把缓冲区的字节写入到ZipArchiveEntry
                                    zipArchiveOutputStream.write(buffer, 0, len);
                                }
                                //Writes all necessary data for this entry.
                                zipArchiveOutputStream.closeArchiveEntry();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    zipArchiveOutputStream.finish();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }

        }

    }
    /**
     * 清空文件夹内容
     * @param path
     * 文件夹的绝对路径
     */
    public static void clearDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] tempList = file.list();
        File temp;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                // 先删除文件夹里面的文件
                clearDir(path + "/" + tempList[i]);
                temp.delete();
            }
        }
    }

    /**
     * 把zip文件解压到指定的文件夹(zip中不能有文件夹)
     *
     * @param zipFilePath zip文件路径, 如 "D:/test/aa.zip"
     * @param saveFileDir 解压后的文件存放路径, 如"D:/test/"
     */
    public static void decompressZip(String zipFilePath, String saveFileDir) {
        if (StringUtils.endsWithIgnoreCase(zipFilePath, ZIP_SUFFIX)) {
            File file = new File(zipFilePath);
            if (file.exists()) {
                try (InputStream is = new FileInputStream(file);
                ZipArchiveInputStream  zipArchiveInputStream = new ZipArchiveInputStream(is)){
                    ArchiveEntry archiveEntry;
                    //把zip包中的每个文件读取出来
                    //然后把文件写到指定的文件夹
                    while ((archiveEntry = zipArchiveInputStream.getNextEntry()) != null) {
                        //获取文件名
                        String entryFileName = archiveEntry.getName();
                        //构造解压出来的文件存放路径
                        String entryFilePath = saveFileDir + entryFileName;
                        byte[] content = new byte[(int) archiveEntry.getSize()];
                        zipArchiveInputStream.read(content);
                        File entryFile = new File(entryFilePath);
                        try(OutputStream os = new BufferedOutputStream(new FileOutputStream(entryFile))) {
                            //把解压出来的文件写到指定路径
                            os.write(content);
                        } catch (IOException e) {
                            throw new IOException(e);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public static void main(String[] args) throws IOException {
        decompressZip("d:/阿斯蒂芬.zip","d:/");

    }
    /**
     * 使用nio下载文件
     *
     * @param URL      文件夹路径，不包含文件名
     * @param fileName 文件名
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    public static void nioDownload(String URL, String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 获取文件
        File file = new File(URL, fileName);
        // 判断文件是否存在
        if (file.exists()) {
            // 设置编码
            response.setCharacterEncoding("UTF-8");
            // 设置文件格式
            response.setContentType("application/zip;charset=UTF-8");
            // 设置文件名，解决乱码
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            // 设置缓冲区大小
            int bufferSize = 4096;
            int readSize;
            int writeSize;
            // allocateDirect速度更快
            ByteBuffer buff = ByteBuffer.allocateDirect(bufferSize);
            try(FileInputStream fileInputStream = new FileInputStream(file);
                FileChannel fileChannel = fileInputStream.getChannel()){
                while ((readSize = fileChannel.read(buff)) != -1) {
                    if (readSize == 0) {
                        continue;
                    }
                    buff.position(0);
                    buff.limit(readSize);
                    while (buff.hasRemaining()) {
                        writeSize = Math.min(buff.remaining(), bufferSize);
                        byte[] byteArr = new byte[writeSize];
                        buff.get(byteArr, 0, writeSize);

                        response.getOutputStream().write(byteArr);
                    }
                    buff.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                buff.clear();
            }
        }
    }

    /**
     * 复制文件(nio)
     * @param fromPath
     * @param toPath
     * @throws IOException
     */
    public static void copyFile(String fromPath , String toPath) throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get(fromPath), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get(toPath), StandardOpenOption.WRITE ,StandardOpenOption.READ );
        inChannel.transferTo(0,inChannel.size(),outChannel);
        inChannel.close();
        outChannel.close();
    }



}
