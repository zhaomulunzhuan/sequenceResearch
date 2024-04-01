package com.chen;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public class index implements Serializable{
    private static final long serialVersionUID = 1L;
    private static List<Block> blocks;

    //构造函数
    static {
        blocks=new ArrayList<>();
    }
    //获取指定索引的块
    public static Block getBlock(int index){
        if(index<0 || index>=blocks.size()){
            throw new IllegalArgumentException("Invalid index");
        }
        return blocks.get(index);
    }

    public static List<Block> getBlockList(){
        return blocks;
    }

    //添加块到末尾
    public static void addBlock(Block block){
        blocks.add(block);
    }

    public static void releaseBlock(int index) {
        if (index < 0 || index >= blocks.size()) {
            System.out.println("Error: Index out of bounds");
            return;
        }

        blocks.set(index, null);
    }

    public static List<String> searchBlocks(List<Integer> blockList, List<Integer> samplesIdxList, String querykmer){
        List<String> resultDataset=new ArrayList<>();
        int datasetnums=samplesIdxList.size();
        ArrayList<Boolean> result = new ArrayList<>();
        for(int global_block_idx:blockList){
            ArrayList<Boolean> cur_result=index.getBlock(global_block_idx).queryExistence(querykmer);
//            System.out.println("拼接结果");
            result.addAll(cur_result);
//            System.out.println(result);
        }
        // 输出 BitSet 的内容
//        System.out.print("Result Bitarray content: ");
//        int count = 0; // 计数器，用于限制输出的位数
        for (int i = 0; i < datasetnums; i++) {
            boolean bit = result.get(i); // 获取第i位的值
//            System.out.print(bit ? "1" : "0"); // 输出该位的值（1或0）
//            count++;
            // 每输出8位后换行，以保持输出格式
//            if (count % 8 == 0) {
//                System.out.print(" "); // 输出空格分隔
//            }
            if(bit){
                String fileName = MetaData.getNameByIdx(samplesIdxList.get(i));
                resultDataset.add(fileName);
            }
        }
//        System.out.println(); // 输出换行符
        return resultDataset;
    }

    // 加载列表内容
    public static void loadBlocks(List<Block> loadedBlocks) {
        blocks = loadedBlocks;
    }

    //删除global_block_index块中的inner_bf_index对应布隆过滤器中的元素
    public static void removeDataset(int global_block_index,int inner_bf_index){
        // 检查 global_block_index 是否在合法范围内
        if (global_block_index < 0 || global_block_index >= blocks.size()) {
            System.out.println("Error: global_block_index out of bounds");
            return;
        }
        //在Java中，当获取一个对象（例如通过blocks.get(global_block_index)获取一个Block对象），实际上是获取了该对象的引用。因此，对该引用所指向的对象的修改会影响到原始列表中该位置的对象。
        Block block = blocks.get(global_block_index);
        List<BloomFilter> bloomFilters = block.getBloomFilterList();
        List<Boolean> statusList=block.getStatusList();


        // 检查 inner_bf_index 是否在合法范围内
        if (inner_bf_index < 0 || inner_bf_index >= bloomFilters.size()) {
            System.out.println("Error: inner_bf_index out of bounds");
            return;
        }

        // 清空对应的 BloomFilter 的 bitarray
        BloomFilter bloomFilter = bloomFilters.get(inner_bf_index);
        bloomFilter.clear();
        //将对应位置的状态更新为false
        statusList.set(inner_bf_index,false);
    }

    // 序列化方法
    public static void serialize(String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(blocks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 反序列化方法
    public static void deserialize(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            List<Block> loadedBlocks = (List<Block>) ois.readObject();
            loadBlocks(loadedBlocks);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 输出函数，用于查看 index 的内容
    public static void printIndex() {
        System.out.println("Index content:");
        for (int i = 0; i < blocks.size(); i++) {
            System.out.println("Block " + i + ": " + blocks.get(i));
            if (blocks.get(i)!=null){
                blocks.get(i).printBlockInfo();
            }
        }
    }

}
