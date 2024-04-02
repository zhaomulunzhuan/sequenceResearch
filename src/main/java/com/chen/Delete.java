package com.chen;

import java.util.List;

public class Delete {
    public static void main(String[] args) {
        Build.buildIndexFromSER();
        MetaData.outputMetadata();
        Query.querykmer("GTTGTGGATAAAATATCGGCGAGTCGGTATA");

//        index.printIndex();
        deleteDataset("GCF_000006885.1_ASM688v1_genomic.fna");
        deleteDataset("GCF_000006845.1_ASM684v1_genomic.fna");

//        deleteDataset("GCF_000006645.1_ASM664v1_genomic.fna");
//        deleteDataset("GCF_000008105.1_ASM810v1_genomic.fna");
//        deleteDataset("GCF_000007505.1_ASM750v1_genomic.fna");
//        Query.querykmer("ACGTCTGGTTGCAAGAGACCGTGACAGGGGG");
//        MetaData.outputMetadata();
//        Insert.insertDatasets("Insert_3.txt");
        Query.querykmer("GTTGTGGATAAAATATCGGCGAGTCGGTATA");
        MetaData.outputMetadata();
//        index.printIndex();
    }

    public static void deleteDataset(String filename){
        if (MetaData.getName_to_idx().containsKey(filename)) {
//            System.out.println("数据集" + filename + "' 存在于当前索引结构中");
            int dataset_index=MetaData.getName_to_idx().get(filename);
//            System.out.println("要删除的数据集的数据集索引是："+dataset_index);
            int group_nums= MetaData.findGroupNums(dataset_index).getKey();
            List<Integer> sampleList=MetaData.findGroupNums(dataset_index).getValue();
//            System.out.println("要删除的数据集的组数是："+group_nums);
//            System.out.println("这个组数所包含的数据集索引列表"+sampleList);
            int position=sampleList.indexOf(dataset_index);
//            System.out.println("要删除的数据集在这组数据集中的位置索引是："+ position);
            int block_max_size= Integer.parseInt(ConfigReader.getProperty("Block-max-size"));
            //组内块索引
            int inner_block_index=position/block_max_size;
            //块内布隆过滤器索引
            int inner_bf_index=position%block_max_size;
//            System.out.println("要删除的数据集在每组内存储的块索引："+inner_block_index+"以及在存储的块中存储的布隆过滤器索引："+inner_bf_index);
            boolean release=false;//是否释放块的标志，因为如果释放了块，对应数据集列表中的-1要清除
            int numsBF = -1;
            for(int group_index=0;group_index<group_nums;group_index++){//按组删除
                List<Integer> blockList=MetaData.getBlocksByGroupNumAndGroupIdx(group_nums,group_index);
//                System.out.println(group_index+"组占用的块索引列表"+blockList);
                int global_block_index=blockList.get(inner_block_index);
//                System.out.println("要删除的数据集在"+group_index+"组存储的块的全局块索引是:"+global_block_index);
                index.removeDataset(global_block_index,inner_bf_index);
                MetaData.getGroupNum_to_samples().get(group_nums).set(position,-1);
                if (index.getBlock(global_block_index).isEmpty()){//如果这个块经过数据集删除后为空
                    numsBF=index.getBlock(global_block_index).getNumsBloomFilter();
                    index.releaseBlock(global_block_index);//释放块
                    blockList.remove(Integer.valueOf(global_block_index));//将这个块索引从blockList中移除
                    release=true;
                }
            }
            //把释放块对应的-1移除
            if(release){
                //确定数据集列表中需要删除的-1的起始位置和终止位置
                boolean allMinusOne=true;
                int startIndex=inner_block_index*block_max_size;
                int endIndex=Math.min(startIndex+numsBF,sampleList.size());
                //检查从索引startIndex开始的numsBF个元素是否都是-1
                for(int i=startIndex;i<endIndex;i++){
                    if(sampleList.get(i)!=-1){
                        allMinusOne=false;
                        System.err.println("您试图删除不是-1的数据集索引");
                        break;
                    }
                }
                //都等于-1
                if(allMinusOne){
                    sampleList.subList(startIndex,endIndex).clear();
                }
            }
            //更新元数据
            MetaData.deleteDataset(dataset_index,filename);
        } else {
            System.err.println("要删除的数据集文件不存在于当前索引中");
        }
    }
}
