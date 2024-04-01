package com.chen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class GetFilePathFromFile {//输入的长序列数据集，生成一个.txt文件，里面每行是一个数据集文件的文件路径
    public static void listFiles(String folderPath, BufferedWriter writer) throws IOException {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    writer.write(file.getAbsolutePath());
                    writer.newLine();
                } else if (file.isDirectory()) {
                    listFiles(file.getAbsolutePath(), writer);
                }
            }
        }
    }

    public static void generatePathFile(){
        //获得项目根目录
        String rootDirectory = ConfigReader.getProperty("project-root-directory");
        //存储 数据集文件路径的目录
        String inputdatasetFolder=ConfigReader.getProperty("input_folder");
        String folderPath=rootDirectory+"/"+inputdatasetFolder;//存储 输入长序列数据集文件 的文件夹
        String outputPath = rootDirectory+"/"+"InputFilePath.txt"; // 将 数据集文件路径 写入InputFilePath.txt，每行一个数据集文件路径

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            listFiles(folderPath, writer);
            ConfigReader.addProperty("InputFilePath",outputPath);
        } catch (IOException e) {
            System.err.println("写入文件时出错：" + e.getMessage());
        }
    }

}
