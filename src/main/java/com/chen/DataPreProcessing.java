package com.chen;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DataPreProcessing {

    public static void dataPreprocessing() {
        long startTime=System.nanoTime();//起始时间
        //提取输入数据集文件 每行是一个数据集文件路径的 文件，存入配置文件中 InputFilePath.txt
        GetFilePathFromFile.generatePathFile();
        //kmer大小
        int kmer_size= Integer.parseInt(ConfigReader.getProperty("kmer-size"));
        //存储 数据集文件路径的文件
        String inputFilePaths=ConfigReader.getProperty("InputFilePath");
        //修改元数据 numSamples idx_to_name name_to_idx
        int datasetIndex=0;//数据集索引
        int datasetsNum=MetaData.getNumSamples();//数据集数量
        try(BufferedReader reader=new BufferedReader(new FileReader(inputFilePaths))) {
            String cur_datasetPath;
            while((cur_datasetPath = reader.readLine())!=null){
                //读取一个数据集文件路径
                //传入当前数据集文件路径，kmer-size，数据集索引，生成对应kemrs集合数据集
                KmerExtractor.getKmersFromFile(cur_datasetPath,kmer_size,datasetIndex);
                MetaData.addDataset(cur_datasetPath);
                datasetIndex++;
            }
        }catch (IOException e){
            System.out.println("读取数据集文件路径时发生错误"+ e.getMessage());
        }
        //基数估计
        String kmerdatasetsPath=ConfigReader.getProperty("project-root-directory")+"/"+ConfigReader.getProperty("kmerdatasets_path");
        HyperLogLogEstimator.estimateKmerCardinalities(kmerdatasetsPath);

        //根据配置文件中给定的预期误报率FPR和哈希函数个数k，选择基础布隆过滤器能存储的元素基数b，确定基础布隆过滤器大小m
        long b=650000;//假设 后面用函数确定
        double FPR= Double.parseDouble(ConfigReader.getProperty("FPR"));
        int k= Integer.parseInt(ConfigReader.getProperty("k"));
        long m= (long) (-1 * (k * b) / Math.log(1 - Math.pow(FPR, 1.0 / k)));
        ConfigReader.addProperty("BF-size", String.valueOf(m));
        ConfigReader.addProperty("bf-cardinality", String.valueOf(b));

        Utils.divideSegment(b);

        long endTime=System.nanoTime();//结束时间
        long elapsedTime=endTime-startTime;
        double elapsedTimeInsecondes=(double) elapsedTime/1000_000_000.0;
        System.out.println("数据预处理程序运行时间"+elapsedTimeInsecondes+"秒");
//        MetaData.outputMetadata();
    }

}



