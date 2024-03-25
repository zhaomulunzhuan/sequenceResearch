package com.chen;

import java.util.ArrayList;
import java.util.List;

public class Block {
    private int numsBloomFilter;
    private List<Boolean> statusList;
    private List<BloomFilter> BloomFilterList;
    private int BlockIndex;
    private int max_numBloomFilter=64;

    public Block(int BlockIndexnt,int numsBloomFilter){
        this.BlockIndex=BlockIndex;
        this.numsBloomFilter=numsBloomFilter;
        this.statusList=new ArrayList<>(numsBloomFilter);
        this.BloomFilterList=new ArrayList<>(numsBloomFilter);
        //初始化状态列表
        for (int i = 0; i < numsBloomFilter; i++) {
            statusList.add(false);
        }
        // 初始化 BloomFilterList
        int m=3596288;
        int k= Integer.parseInt(ConfigReader.getProperty("k"));
        for (int i = 0; i < numsBloomFilter; i++) {
            BloomFilterList.add(new BloomFilter(m,k));
        }
    }

    public void addElement(int bfIndex,String element){
        BloomFilterList.get(bfIndex).insertElement(element);
        statusList.set(bfIndex,true);
    }


    // 输出函数，用于查看 Block 的信息
    public void printBlockInfo() {
        System.out.println("Block Index: " + BlockIndex);
        System.out.println("Number of Bloom Filters: " + numsBloomFilter);
        System.out.println("Bloom Filter Status:");
        for (int i = 0; i < numsBloomFilter; i++) {
            System.out.println("Bloom Filter " + i + ": " + (statusList.get(i) ? "Active" : "Inactive"));
            System.out.println("1的数量"+BloomFilterList.get(i).countOnes());
        }
    }



}
