package com.chen;

import org.apache.datasketches.hll.HllSketch;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HyperLogLogEstimator {//基数估计

    public static void estimateKmerCardinalities(String folderPath) {//输入一个文件夹路径，估计这个文件夹中的每个数据集文件的基数
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            System.err.println("Input is not a folder.");
            return;
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            System.err.println("No files found in the folder.");
            return;
        }

        String rootDirectory = ConfigReader.getProperty("project-root-directory");
        String cardinalityFile = rootDirectory+"/"+"dataidx_to_cardinality.txt";
        try(BufferedWriter writer=new BufferedWriter((new FileWriter(cardinalityFile)))){
            for (File file : files) {
                if (file.isFile()) {
//                    System.out.println(MetaData.getIdxByName(file.getName()));
//                    System.out.println("File: " + file.getName() + ", Estimated Kmer Cardinality: " + estimateFileKmerCardinality(file));
                    int index = MetaData.getIdxByName(file.getName());
                    long cardinality = estimateFileKmerCardinality(file);
                    String line = index + "," + cardinality; // 将索引和基数连接为一行字符串
                    //写入数据集索引和对对应基数
                    writer.write(line);
                    writer.newLine();
//                    System.out.println(line);
//                System.out.println("File: " + file.getName() + ", Exact Kmer Cardinality: " + calculateExactKmerCardinality(file));
                }
            }
        }catch (IOException e){
            System.err.println("写入数据集基数信息时出错"+e.getMessage());
        }
        ConfigReader.addProperty("cardinalitiesFile","dataidx_to_cardinality.txt");//将存储基数信息的文件名记录到配置信息中
    }


    public static long estimateFileKmerCardinality(File file) {//估计一个文件的基数
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            HllSketch sketch = new HllSketch(12); // 12 is the log2m value, adjust based on your requirements
            //log2m 决定了 HLL 中存储桶（buckets）的数量，桶的数量为 2 的 log2m 次方
            //更大的 log2m 值通常意味着更大的内存开销和更高的精度
            String line;
            while ((line = reader.readLine()) != null) {
                sketch.update(line.trim().getBytes()); // Assuming each line contains a kmer
            }
            return (long) sketch.getEstimate();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if estimation fails
    }


    //使用集合计算一个数据集的精确基数
    public static long calculateExactKmerCardinality(File file) {
        Set<String> kmerSet = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                kmerSet.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return kmerSet.size();
    }
}

//import java.io.*;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class HyperLogLogEstimator {
//
//    public static void estimateKmerCardinalities(String folderPath) {
//        File folder = new File(folderPath);
//        if (!folder.isDirectory()) {
//            System.err.println("Input is not a folder.");
//            return;
//        }
//
//        File[] files = folder.listFiles();
//        if (files == null || files.length == 0) {
//            System.err.println("No files found in the folder.");
//            return;
//        }
//
//        String rootDirectory = ConfigReader.getProperty("project-root-directory");
//        String cardinalityFile = rootDirectory+"/"+"dataidx_to_cardinality.txt";
//
//        // 创建一个线程池
//        int numThreads = Runtime.getRuntime().availableProcessors();
//        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
//
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cardinalityFile))) {
//            List<Callable<Void>> tasks = new ArrayList<>();
//            for (File file : files) {
//                if (file.isFile()) {
//                    tasks.add(() -> {
//                        int index = MetaData.getIdxByName(file.getName());
//                        long cardinality = estimateFileKmerCardinality(file);
////                        System.out.println("File: " + file.getName() + ", Estimated Kmer Cardinality: " + cardinality);
//                        String line = index + "," + cardinality; // 将索引和基数连接为一行字符串
//                        // 写入数据集索引和对对应基数
//                        synchronized (writer) {
//                            writer.write(line);
//                            writer.newLine();
//                        }
//                        return null;
//                    });
//                }
//            }
//            executorService.invokeAll(tasks);
//        } catch (IOException | InterruptedException e) {
//            System.err.println("写入数据集基数信息时出错" + e.getMessage());
//        }
//        ConfigReader.addProperty("cardinalitiesFile","dataidx_to_cardinality.txt");//将存储基数信息的文件名记录到配置信息中
//
//        executorService.shutdown(); // 关闭线程池
//    }
//
//    public static long estimateFileKmerCardinality(File file) {
//        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
//            // 使用HllSketch估计基数
//            HllSketch sketch = new HllSketch(12);
//            String line;
//            while ((line = reader.readLine()) != null) {
//                sketch.update(line.trim().getBytes());
//            }
//            return (long) sketch.getEstimate();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return -1;
//    }
//
//    //使用集合计算一个数据集的精确基数
//    public static long calculateExactKmerCardinality(File file) {
//        Set<String> kmerSet = new HashSet<>();
//        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                kmerSet.add(line.trim());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return kmerSet.size();
//    }
//}

