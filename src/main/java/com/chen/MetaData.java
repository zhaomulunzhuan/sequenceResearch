package com.chen;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.ArrayList;

public class MetaData {
    private static int numSamples;
    private static Map<Integer, List<Integer>>groupNum_to_samples;
    private static List<String> idx_to_name;
    private static Map<Integer,String> idx_to_dataFilePath;
    private static Map<String,Integer>name_to_idx;
    private static Map<GroupKey, List<Integer>> groupNum_and_groupIdx_to_blocks;


    // 构造函数
    static {
        numSamples = 0;
        groupNum_to_samples = new HashMap<>();
        idx_to_name = new ArrayList<>();
        idx_to_dataFilePath=new HashMap<>();
        name_to_idx = new HashMap<>();
        groupNum_and_groupIdx_to_blocks = new HashMap<>();
    }

    // 获得数据集数量
    public static int getNumSamples() {
        return numSamples;
    }
    //更改数据集数量
    public static void setNumSamples(int numSamples) {
        MetaData.numSamples = numSamples;
    }
    //添加数据集时name_to_idx,idx_to_name更新
    public static void addDataset(String cur_datasetPath) {
        //数据集文件名
        String datasetName = cur_datasetPath.substring(cur_datasetPath.lastIndexOf("\\")+1);
        name_to_idx.put(datasetName,idx_to_name.size());
        idx_to_name.add(datasetName);
    }
    public static void idxToKmerdatasetPath(Integer index,String kmerfilePath){
        idx_to_dataFilePath.put(index,kmerfilePath);
    }
    //idx_to_name添加多个数据集名称
    public static void addDatasets(List<String> datasetNames) {
        for(String dataName:datasetNames){
            addDataset(dataName);
        }
    }
    //根据数据名名称获得对应索引
    public static int getIdxByName(String datasetName){
        return name_to_idx.get(datasetName);
    }
    //根据数据集索引获得数据集名称
    public static String getNameByIdx(int index){
        if (index>=0 && index<idx_to_name.size()){
            return idx_to_name.get(index);
        }else {
            System.out.println("索引异常");
            return null;
        }
    }
    //根据一组数据集索引 获得 一组数据集名称
    public static List<String> getNamesByIdxs(List<Integer> indexes) {
        List<String> names = new ArrayList<>();
        for (int index : indexes) {
            if (index >= 0 && index < idx_to_name.size()) {
                names.add(idx_to_name.get(index));
            } else {
                System.out.println("索引异常：" + index);
            }
        }
        return names;
    }

    public static String getDatapathByIdx(int index){
        return idx_to_dataFilePath.get(index);
    }

    // GroupNumToSamples添加方法
    public static void addGroupNumToSamples(int groupNum, int datasetIndex) {
        if (groupNum_to_samples.containsKey(groupNum)) {
            // 如果键a已经存在，则获取对应的list，并将数据集索引b加入其中
            List<Integer> datasets = groupNum_to_samples.get(groupNum);
            datasets.add(datasetIndex);
            groupNum_to_samples.put(groupNum, datasets); // 更新映射关系
        } else {
            // 如果键a不存在，则创建新的键值对，并将数据集索引b添加到新的list中
            List<Integer> datasets = new ArrayList<>();
            datasets.add(datasetIndex);
            groupNum_to_samples.put(groupNum, datasets);
        }
    }

    public static void outputMetadata(){
        System.out.println("数据集数量"+getNumSamples());
        for (int i=0;i<idx_to_name.size();i++){
            System.out.println(idx_to_name.get(i));
        }
        System.out.println("数据集索引到对应kmer文件路径的映射");
        for(Map.Entry<Integer,String> entry:idx_to_dataFilePath.entrySet()){
            Integer index=entry.getKey();
            String dataPath=entry.getValue();
            System.out.println("Dataset Index:"+index+",kmerFilePath:"+dataPath);
        }
        System.out.println("数据集名称到数据集索引的映射");
        for (Map.Entry<String, Integer> entry : name_to_idx.entrySet()) {
            String datasetName = entry.getKey();
            int index = entry.getValue();
            System.out.println("Dataset Name: " + datasetName + ", Index: " + index);
        }
        System.out.println("组数到数据集索引集合的映射");
        for (Map.Entry<Integer, List<Integer>> entry : groupNum_to_samples.entrySet()) {
            int groupNum = entry.getKey();
            List<Integer> datasets = entry.getValue();
            System.out.println("GroupNum: " + groupNum + ", Datasets: " + datasets);
        }
    }

    // 添加方法
    public void addGroupNumAndGroupIdxToBlocks(int groupNum, int groupIdx, List<Integer> blocks) {
        GroupKey key = new GroupKey(groupNum, groupIdx);
        groupNum_and_groupIdx_to_blocks.put(key, blocks);
    }

    // 查询方法
    public List<Integer> getBlocksByGroupNumAndGroupIdx(int groupNum, int groupIdx) {
        GroupKey key = new GroupKey(groupNum, groupIdx);
        return groupNum_and_groupIdx_to_blocks.get(key);
    }

    // 自定义复合键对象，表示分区组数和分区内组索引
    class GroupKey {
        private final int groupNum;
        private final int groupIdx;

        public GroupKey(int groupNum, int groupIdx) {
            this.groupNum = groupNum;
            this.groupIdx = groupIdx;
        }

        // 重写 equals 和 hashCode 方法，使得两个对象在内容上相等时可以被认为是相等的
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            GroupKey groupKey = (GroupKey) obj;
            return groupNum == groupKey.groupNum && groupIdx == groupKey.groupIdx;
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupNum, groupIdx);
        }
    }

}
