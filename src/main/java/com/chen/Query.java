package com.chen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Query {
    public static void main(String[] args) throws IOException {
        Build.buildIndexFromSER();
        Exactquerykmers("D:\\SequenceSearch\\query.txt");
    }


    public static void Exactquerykmers(String filePath){//查询一个输入文件，文件里面是一段长序列，返回包含这个序列所有kmers的数据集
        int kmersize= Integer.parseInt(ConfigReader.getProperty("kmer-size"));
        String sequence = ""; // 初始化sequence变量
        try (BufferedReader reader=new BufferedReader(new FileReader(filePath))){
            String line;
            while ((line=reader.readLine())!=null){
                sequence+=line;
            }
        } catch (IOException e){
            System.err.println(e);
        }
        List<String> kmerList=new ArrayList<>();
        // 切割sequence并将长度为kmersize的子字符串加入kmerList
        for (int i = 0; i <= sequence.length() - kmersize; i++) {
            String kmer = sequence.substring(i, i + kmersize);
            kmerList.add(kmer);
        }
//        for (String kmer : kmerList) {
//            System.out.println(kmer);
//        }
        List<String> result=new ArrayList<>(querykmer(kmerList.get(0)));
        for(String kmer:kmerList){
            result.retainAll(querykmer(kmer));
        }
        System.out.println("包含输入文件中查询序列的数据集");
        for (String datasetName:result){
            System.out.println(datasetName);
        }
    }


    public static List<String> querykmer(String kmer){
        List<String> results=new ArrayList<>();

        for(Map.Entry<Integer,List<Integer>> entry:MetaData.getGroupNum_to_samples().entrySet()) {
            int group_nums = entry.getKey();
            List<Integer> datasetIdxs = entry.getValue();
            //得到段内组索引
            int group_idx = Math.abs(kmer.hashCode()) % group_nums;
            List<Integer> blockList = MetaData.getBlocksByGroupNumAndGroupIdx(group_nums, group_idx);
            //k个哈希索引
//            List<Long> rowIdxs = new ArrayList<>();
            int k = Integer.parseInt(ConfigReader.getProperty("k"));
            int m = Integer.parseInt(ConfigReader.getProperty("BF-size"));
//            rowIdxs = Utils.myHash(kmer, k, m);
            List<String> cur_datasetResult = index.searchBlocks(blockList, datasetIdxs, kmer);
            results.addAll(cur_datasetResult);
        }
        System.out.println(kmer+"查询到数据集：");
        for(String dataset:results){
            System.out.println(dataset);
        }
        return results;
    }

}
