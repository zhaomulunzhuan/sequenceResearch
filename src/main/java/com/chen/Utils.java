package com.chen;

import org.apache.datasketches.hash.XxHash;
import org.apache.datasketches.hash.MurmurHash3;
import org.apache.datasketches.hash.XxHash;
import org.apache.datasketches.memory.Memory;

import com.google.common.hash.Hashing;
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
        ConfigReader.addProperty("segementedDirectory","SegementedResults");
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


    public static List<Long> myHash(String key, int k, int range) {
        List<Long> hashValues = new ArrayList<>();

        // Iterate over k hash functions
        for (int i = 0; i < k; i++) {
            // Calculate hash using MurmurHash algorithm
            long hash = Hashing.murmur3_128(i).hashString(key, StandardCharsets.UTF_8).asLong();

            // Map hash value to the specified range
            long mappedHash = Math.abs(hash) % range;

            hashValues.add(mappedHash);
        }

        return hashValues;
    }


    //chunks 方法接受一个列表 list 和一个整数 n，并返回一个迭代器，该迭代器用于生成 list 的分块结果。
    // 在 next 方法中，我们使用 subList 方法从原始列表中提取长度为 n 的子列表，并更新索引以继续生成下一个分块。通过迭代这个迭代器，我们可以按需生成给定列表的分块结果。
    public static <T> Iterable<List<T>> chunks(List<T> list, int n) {
        return new Iterable<List<T>>() {
            @Override
            public Iterator<List<T>> iterator() {
                return new Iterator<List<T>>() {
                    private int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < list.size();
                    }

                    @Override
                    public List<T> next() {
                        int endIndex = Math.min(index + n, list.size());
                        List<T> chunk = list.subList(index, endIndex);
                        index = endIndex;
                        return chunk;
                    }
                };
            }
        };
    }

    public static void printBitSet(BitSet bitSet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < bitSet.length(); i++) {
            sb.append(bitSet.get(i) ? "1" : "0");
        }
        sb.append("}");
        System.out.println(sb.toString());
    }

}
