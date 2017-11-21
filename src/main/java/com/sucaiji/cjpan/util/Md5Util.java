package com.sucaiji.cjpan.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

public interface Md5Util {

    /**
     * 获取该输入流的MD5值
     *
     * @param is
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    String getMD5(InputStream is) throws NoSuchAlgorithmException, IOException;

    /**
     * 获取该文件的MD5值
     *
     * @param file
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    String getMD5(File file) throws NoSuchAlgorithmException, IOException;

    /**
     * 获取指定路径文件的MD5值
     *
     * @param path
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    String getMD5(String path) throws NoSuchAlgorithmException, IOException;

    /**
     * 校验该输入流的MD5值
     *
     * @param is
     * @param toBeCheckSum
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    boolean md5CheckSum(InputStream is, String toBeCheckSum)throws NoSuchAlgorithmException, IOException;

    /**
     * 校验该文件的MD5值
     *
     * @param file
     * @param toBeCheckSum
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    boolean md5CheckSum(File file, String toBeCheckSum)throws NoSuchAlgorithmException, IOException;

    /**
     * 校验指定路径文件的MD5值
     *
     * @param path
     * @param toBeCheckSum
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    boolean md5CheckSum(String path, String toBeCheckSum) throws NoSuchAlgorithmException, IOException;
}
