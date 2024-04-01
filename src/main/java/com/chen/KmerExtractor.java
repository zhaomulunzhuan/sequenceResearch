package com.chen;

import java.io.*;

public class KmerExtractor {//原始数据集是长序列集合，每个原始数据集生成一个对应的kmers集合的数据集文件
    //生成的kmers数据集的存放目录
    static String kmer_datasets_path=ConfigReader.getProperty("project-root-directory")+"/"+ConfigReader.getProperty("kmerdatasets_path");
    // 从文件中读取DNA序列并生成kmers集合
    public static void getKmersFromFile(String filePath, int kmersize, int index) throws IOException {//两个标题行间的序列是一段连续序列，不跨标题行
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            StringBuilder sequenceBuilder = new StringBuilder(); // 用于构建序列

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(">")) { // 当前行是标题行
                    if (sequenceBuilder.length() > 0) {
                        // 遇到标题行，处理已经读取的序列
                        processSequence(sequenceBuilder.toString(), kmersize, index, filePath);
                        sequenceBuilder.setLength(0); // 清空序列缓冲区
                    }
                } else { // 当前行是序列，继续追加
                    sequenceBuilder.append(line.trim().toUpperCase()); // 将序列转换为大写并去除前后空格加入sequenceBuilder
                }
            }
            // 处理最后一段序列
            if (sequenceBuilder.length() > 0) {
                processSequence(sequenceBuilder.toString(), kmersize, index, filePath);
            }
        }
    }

    // 从DNA序列中获取kmers集合（滑动窗口实现）
    private static void processSequence(String sequence, int kmersize, int index, String filePath) throws IOException {
        String fileName= filePath.substring(filePath.lastIndexOf("\\")+1);
        String outputFilePath = kmer_datasets_path + "/" + fileName;
        MetaData.idxToKmerdatasetPath(index,outputFilePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, true))) {
            for (int i = 0; i <= sequence.length() - kmersize; i++) {
                String kmer = sequence.substring(i, i + kmersize);
                writer.write(kmer);
                writer.newLine();
            }
        }
    }
}




