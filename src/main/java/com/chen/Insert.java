package com.chen;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Insert {

    public static void insertDatasets(String insertFile){//insertFile中记载要插入的每个数据集的文件路径
        String insertFolder=ConfigReader.getProperty("project-root-directory")+"/"+ConfigReader.getProperty("insert_folder");
        String insertFilePath=insertFolder+"/"+insertFile;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(insertFilePath));
            String cur_datasetPath;
            while ((cur_datasetPath = reader.readLine()) != null) {
                // 处理每行中的文件路径
                insertDataset(cur_datasetPath);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void insertDataset(String datasetpath) throws IOException {
        //元数据更新
        if (!MetaData.addDataset(datasetpath)){//将添加的数据集相应信息添加到元数据中
            //数据集名称
            String datasetName = datasetpath.substring(datasetpath.lastIndexOf("\\") + 1);
            System.out.println("插入数据集"+datasetName);
            //数据集索引
            int dataset_indxe=MetaData.getIdxByName(datasetName);
            int kmer_size= Integer.parseInt(ConfigReader.getProperty("kmer-size"));
//            System.out.println("插入的数据集"+datasetName+"的数据集索引："+dataset_indxe);
            //提取kmer生成kmer数据集文件存储到项目目录下的kmersDatasets文件夹中
            KmerExtractor.getKmersFromFile(datasetpath,kmer_size,dataset_indxe);
            String kmerdataPath=MetaData.getDatapathByIdx(dataset_indxe);
//            System.out.println("kmer数据集文件路径"+kmerdataPath);
            //基数估计
            long cardinality=HyperLogLogEstimator.estimateFileKmerCardinality(new File(kmerdataPath));
//            System.out.println("插入的数据集"+datasetName+"的kmer基数："+cardinality);
            long b= Long.parseLong(ConfigReader.getProperty("bf-cardinality"));
            //确定组数，即存储的段
            int group_nums= (int) (cardinality/b)+1;
//            System.out.println("插入的数据集"+datasetName+"需要的组数："+group_nums);
            List<Integer> blockList=MetaData.getBlocksByGroupNumAndGroupIdx(group_nums,0);//如果当前段还未创建任何block则返回null
            boolean insertFlag=false;//记录是否找到插入位置
            int insert_block=-1;//记录要插入的块的组内块索引
            int insert_bf=-1;//记录要插入的布隆过滤器的块内布隆过滤器索引
            if (blockList!=null){//blockList是要插入的段的0索引组占用的block
//                System.out.println(datasetName+"要存储的段的0号组占用的block索引列表"+blockList);
                for(int i=0;i<blockList.size();i++){
                    Block block=index.getBlock(blockList.get(i));//逐个快进行检查
                    if(!block.isFull()){//当前块未满
                        insert_block=i;//插入位置的组内块索引为i，即将要插入每个组的索引为i的块中

                        Object[] result = block.findInsertposition();
                        // 获取块内布隆过滤器索引和是否需要添加新的布隆过滤器的标识
                        int position = (int) result[0];
                        boolean addBFflag = (boolean) result[1];
                        // position为将在第i个块的索引为position的布隆过滤器中插入，addBFflag=true说明插入的这个布隆过滤器是需要新添加的，而不是删除操作留下的已存在的空布隆过滤器
                        if (addBFflag) {
                            // 如果 insertFlag 为 true，则表示需要添加新的布隆过滤器，则每个组的这个块都需要添加
                            //这个段的每个组的索引为i的块都要添加一个新的布隆过滤器
                            for(int j=0;j<group_nums;j++){
                                List<Integer> needAddBlockList=MetaData.getBlocksByGroupNumAndGroupIdx(group_nums,j);
                                System.out.println("组数为"+group_nums+"的段的"+j+"组所占用的块索引列表"+needAddBlockList);
                                Block needAddBlock=index.getBlock(needAddBlockList.get(i));
                                System.out.println(j+"组"+"需要添加布隆过滤器的块当前信息");
                                needAddBlock.printBlockInfo();
                                needAddBlock.addBF();
                            }
                        }
                        insert_bf=position;
                        insertFlag=true;
                    }
                }
                if(!insertFlag){//如果这个段的0号组所占用的块都没有找到插入位置（所有组结构一致）,则需要创建新块
                    System.out.println(datasetName+"需要存储的段没有空余位置");
                    insert_block=blockList.size();
                    for(int i=0;i<group_nums;i++){
                        int block_index=index.getBlockList().size();//新块的全局块索引
                        Block new_block=new Block(block_index,1);//新块的布隆过滤器数量为1
                        index.addBlock(new_block);
                        //将新块索引加入到对应段和组索引中
                        MetaData.addGroupNumAndGroupIdxToBlocks(group_nums,i,block_index);
                    }
                    insert_bf=0;//插入的块内布隆过滤器索引为0
                }
            }else{//当前数据集要存储的段还未创建
                System.out.println(datasetName+"需要存储的段还未创建");
                insert_block=0;
                for(int i=0;i<group_nums;i++){
                    int block_index=index.getBlockList().size();
                    Block new_block=new Block(block_index,1);
                    index.addBlock(new_block);
                    //将新块索引假如到对应段和组索引中
                    MetaData.addGroupNumAndGroupIdxToBlocks(group_nums,i,block_index);
                }
                insert_bf=0;
            }
            System.out.println("在每组中的插入位置："+insert_block+"索引块中的"+insert_bf+"索引布隆过滤器");
            try (BufferedReader reader=new BufferedReader(new FileReader(kmerdataPath))){
                String kmer;
                while((kmer=reader.readLine())!=null){
                    int group_idx=Math.abs(kmer.hashCode())%group_nums;
                    int global_block_index=MetaData.getBlocksByGroupNumAndGroupIdx(group_nums,group_idx).get(insert_block);
                    index.getBlock(global_block_index).addElement(insert_bf,kmer);
                }
            }
            //插入的数据集在这个段中存储的位置，即在这个段对应的数据集索引列表中的位置索引
            int block_max_size= Integer.parseInt(ConfigReader.getProperty("Block-max-size"));
            int insertpositon=insert_block*block_max_size+insert_bf;
            System.out.println("插入的数据集"+datasetName+"在这个段存储的数据集列表中索引为:"+insertpositon);
            MetaData.insertSampleToGroupNumToSamples(group_nums,insertpositon,dataset_indxe);
        }
    }


}
