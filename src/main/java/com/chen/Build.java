package com.chen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Build {
    private static int cur_block_index;

    static {
        cur_block_index=-1;
    }

    public static void main(String[] args) {
        DataPreProcessing.dataPreprocessing();
        //遍历每个段，确定group_nums和这个段的数据集列表
        //获得分段结果目录
        String segementedDirectory=ConfigReader.getProperty("project-root-directory")+"/"+ConfigReader.getProperty("segementedDirectory");

        //遍历目录下的所有文件
        File folder=new File(segementedDirectory);
        File[] files=folder.listFiles();
        if(files!=null){
            for(File file:files){//一个file代表处理一个段
                if(file.isFile()&& file.getName().startsWith("segement_") && file.getName().endsWith(".txt")){
                    // 提取文件名中的数字 是当前段的组数
                    int group_nums = extractSegmentNumber(file.getName());
                    System.out.println("Segment number: " + group_nums);
                    //当前段存储的数据集的文件路径
                    ArrayList<String> samplesList=processTxtFile(file);
                    //当前段每个组需要的block数量
                    assert samplesList != null;
                    int group_block_nums=(int) Math.ceil((double) samplesList.size() / 64);
                    for(String path:samplesList){
                        System.out.println(path);
                    }
                    //按照64大小分成子列表
                    // 调用 chunks 方法并指定类型参数为 String
                    Iterable<List<String>> chunkIterator = Utils.<String>chunks(samplesList, 2);
                    for (List<String> chunk : chunkIterator) {//chunk为当前要处理的子列表，长度小于等于64
                        for(int group_index=0;group_index<group_nums;group_index++){
                            int block_index=cur_block_index+1;
                            Block block=new Block(block_index,chunk.size());
                            index.addBlock(block);
                            MetaData.addGroupNumAndGroupIdxToBlocks(group_nums,group_index,block_index);
                            cur_block_index++;
                        }
                        for(int j=0;j<chunk.size();j++){
                            String samplePath=chunk.get(j);//获得当前要处理的数据集的文件路径
                            try (BufferedReader reader=new BufferedReader(new FileReader(samplePath))){
                                String kmer;
                                while ((kmer=reader.readLine())!=null){
                                    // 对每个 kmer 进行哈希操作，确保哈希值在 group_nums 范围内
                                    int group_idx = Math.abs(kmer.hashCode()) % group_nums;
                                    int global_block_idx=cur_block_index-group_nums+1+group_idx;
                                    index.getBlock(global_block_idx).addElement(j,kmer);
                                }
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                        System.out.println(chunk);
                    }

                }
            }
            index.printIndex();
        }
    }


    private static int extractSegmentNumber(String fileName) {
        String[] parts = fileName.split("_");
        String numberPart = parts[1].split("\\.")[0]; // 获取_后面的数字部分，并移除.txt后缀
        return Integer.parseInt(numberPart);
    }

    private static ArrayList<String> processTxtFile(File file) {
        ArrayList<String> kmersDatasetPaths=new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 读取每行数据集文件路径
                kmersDatasetPaths.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return kmersDatasetPaths;
    }
}
