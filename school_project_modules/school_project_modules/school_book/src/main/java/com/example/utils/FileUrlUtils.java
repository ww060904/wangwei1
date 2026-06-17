package com.example.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 文件路径工具类
 */
@Component
public class FileUrlUtils {

    private static String urlPrefix;

    @Value("${file.url-prefix:/images/}")
    public void setUrlPrefix(String urlPrefix) {
        FileUrlUtils.urlPrefix = urlPrefix;
    }

    /**
     * 获取图片完整访问路径
     * @param imageName 图片文件名
     * @return 完整访问路径，如 /images/xxx.png
     */
    public static String getImageUrl(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            return null;
        }
        // 如果已经是完整路径，直接返回
        if (imageName.startsWith("http://") || imageName.startsWith("https://") || imageName.startsWith("/images/")) {
            return imageName;
        }
        return urlPrefix + imageName;
    }

    /**
     * 获取指定目录下的所有文件名
     *
     * @param dirPath 目录路径，例如 "C:/file/image/"
     * @return 文件名数组（不包含路径），如果目录不存在或不是文件夹，返回空数组
     */
    public List<String> listFileNames(String dirPath) {
        if (dirPath == null || dirPath.isEmpty()) {
            return List.of(); // Java 9+，返回空列表
        }

        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return List.of();
        }

        String[] fileArray = dir.list();
        if (fileArray == null) {
            return List.of();
        }

        // 转成 List<String>
        return Arrays.asList(fileArray);
    }
    /**
     * 删除文件
     * @param filePath 文件完整路径，例如 "C:/file/image/64dc19a9-64d6-4eb5-875e-57b4a46050cc.jpg"
     * @return true 删除成功，false 删除失败
     */
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            // 文件不存在
            return false;
        }

        return file.delete();
    }
}
