package com.chen;

import org.apache.datasketches.hash.XxHash;
import org.apache.datasketches.hash.MurmurHash3;
import org.apache.datasketches.hash.XxHash;
import org.apache.datasketches.memory.Memory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Utils {
    //从存储数据集索引和对应基数的文件中读取信息
    public static void getCardinality(String cardinalityFile,long b){
        //获得项目根目录
        String rootDirectory = ConfigReader.getProperty("project-root-directory");
        String segementedDirectory=rootDirectory+"/"+"SegementedResults";
        File segmentedDirectory = new File(segementedDirectory);
        if (!segmentedDirectory.exists()) {
            segmentedDirectory.mkdirs(); // 创建目录及其所有父目录
        }
        HashMap<Integer,BufferedWriter> writerMap=new HashMap<>();
        try (BufferedReader reader=new BufferedReader(new FileReader(cardinalityFile))){
            String line;
            while((line=reader.readLine())!=null){
                //每行使用逗号分隔数据集索引和基数
                String[] parts=line.split(",");
                if(parts.length==2){//每行有两个元素
                    int index=Integer.parseInt(parts[0]);//解析数据集索引
                    long cardinality=Long.parseLong(parts[1]);//解析基数
                    int group_nums= (int) (cardinality/b)+1;
                    BufferedWriter writer=writerMap.computeIfAbsent(group_nums,k->{
                        try {
                            return new BufferedWriter(new FileWriter(segementedDirectory+"/"+"segement_"+k+".txt"));
                        }catch (IOException e){
                            e.printStackTrace();
                            return null;
                        }
                    });
//                    System.out.println(group_nums);
                    MetaData.addGroupNumToSamples(group_nums,index);
//                    System.out.println("Index:"+index+",Cardinality:"+cardinality);
                    if (writer!=null){
                        writer.write(MetaData.getDatapathByIdx(index));
                        writer.newLine();
                    }
                }else {
                    System.err.println("Invalid line"+line);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            // 关闭所有文件写入器
            for (BufferedWriter writer : writerMap.values()) {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //根据各个数据集的基数确定每个数据集需要的基础布隆过滤器数量，然后填充元数据中的groupNum_to_samples
    public static void divideSegment(long b){
        //获得项目根目录
        String rootDirectory = ConfigReader.getProperty("project-root-directory");
        //存储 数据集索引和对应基数的文件
        String cardinalitiesFile=ConfigReader.getProperty("cardinalitiesFile");
        String nameidx_to_cardinality=rootDirectory+"/"+cardinalitiesFile;
        getCardinality(nameidx_to_cardinality,b);
    }



    //传递kmer key，哈希函数个数k，布隆过滤器大小range，随机种子seed，返回k个哈希值
    public static List<Long> myHash(String key,int k,int range,int seed){
        List<Long> hashValues=new ArrayList<>();

        // Convert key to bytes
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < k; i++) {
            // Calculate hash using XxHash
            long hash = XxHash.hash(Memory.wrap(keyBytes), 0, keyBytes.length, seed + (i * seed)); // 64-bit hash

            // Map hash value to the specified range
            long mappedHash = (int) Math.floorMod(hash, range);

            hashValues.add(mappedHash);
        }

        return hashValues;
    }

}
