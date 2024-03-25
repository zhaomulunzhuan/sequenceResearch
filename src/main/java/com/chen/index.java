package com.chen;

import java.util.ArrayList;
import java.util.List;

public class index {
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

    //添加块到末尾
    public static void addBlock(Block block){
        blocks.add(block);
    }

    // 输出函数，用于查看 index 的内容
    public static void printIndex() {
        System.out.println("Index content:");
        for (int i = 0; i < blocks.size(); i++) {
            System.out.println("Block " + i + ": " + blocks.get(i));
            blocks.get(i).printBlockInfo();
        }
    }

}
