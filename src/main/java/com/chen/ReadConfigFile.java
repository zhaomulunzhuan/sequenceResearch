package com.chen;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ReadConfigFile {

    public static void main(String[] args) {
        long startTime=System.nanoTime();//起始时间
        //配置文件中的输入数据集文件生成一个 每行是一个数据集文件路径的 文件，存入配置文件中 InputFilePath.txt
        GetFilePathFromFile.generatePathFile();
        //kmer大小
        int kmer_size= Integer.parseInt(ConfigReader.getProperty("kmer-size"));
        //存储 数据集文件路径的文件
        String inputFilePaths=ConfigReader.getProperty("InputFilePath");
//        System.out.println(inputFilePaths);
        //
        //操作元数据 numSamples idx_to_name name_to_idx
        int datasetIndex=0;//数据集索引
        int datasetsNum=MetaData.getNumSamples();//数据集数量
        try(BufferedReader reader=new BufferedReader(new FileReader(inputFilePaths))) {
            String cur_datasetPath;
            while((cur_datasetPath = reader.readLine())!=null){
//                //数据集文件名
//                String datasetFileName = cur_datasetPath.substring(cur_datasetPath.lastIndexOf("\\")+1);
//                System.out.println("数据集索引"+datasetIndex+",数据集名称:"+datasetFileName);
                //传入当前数据集文件路径，kmer-size，数据集索引，生成对应kemrs集合数据集
                KmerExtractor.getKmersFromFile(cur_datasetPath,kmer_size,datasetIndex);
//                MetaData.addDataset(datasetFileName);
                MetaData.addDataset(cur_datasetPath);
                datasetIndex++;
                MetaData.setNumSamples(++datasetsNum);
//                System.out.println(MetaData.getNumSamples());
            }
        }catch (IOException e){
            System.out.println("读取数据集文件路径时发生错误"+ e.getMessage());
        }
        //基数估计
        String kmerdatasetsPath=ConfigReader.getProperty("project-root-directory")+"/"+ConfigReader.getProperty("kmerdatasets_path");
        HyperLogLogEstimator.estimateKmerCardinalities(kmerdatasetsPath);

        //根据配置文件中给定的预期误报率FPR和哈希函数个数k，选择基础布隆过滤器能存储的元素基数b，确定基础布隆过滤器大小m
        long b=745000;//假设 后面用函数确定
        long m=3596288;

        Utils.divideSegment(b);
        MetaData.outputMetadata();


        long endTime=System.nanoTime();//结束时间
        long elapsedTime=endTime-startTime;
        double elapsedTimeInMillisecondes=(double) elapsedTime/1000_000_000.0;
        System.out.println("程序运行时间"+elapsedTimeInMillisecondes+"秒");
    }


//    // 获取文件的扩展名
//    private static String getFileExtension(File file) {
//        String fileName = file.getName();
//        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
//            return fileName.substring(fileName.lastIndexOf(".") + 1);
//        } else {
//            return "";
//        }
//    }

}



