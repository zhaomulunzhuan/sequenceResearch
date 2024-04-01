package com.chen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class BloomFilter implements Serializable {
    private static final long serialVersionUID = 1L;
    private int size;
    private int k;


    private BitSet bitarray;
    public BloomFilter() {
        int m= Integer.parseInt(ConfigReader.getProperty("BF-size"));
        int k= Integer.parseInt(ConfigReader.getProperty("k"));
        this.size = m;
        this.k = k;
        this.bitarray = new BitSet(size);
    }

    public BitSet getbitarray(){
        return bitarray;
    }

    public void insertElement(String element) {
        List<Long> hashValues = Utils.myHash(element, k, size);
        for (long index : hashValues) {
            int positiveIndex = (int) (index & Long.MAX_VALUE); // 将负数索引转换为正数
            bitarray.set(positiveIndex);
        }
    }

    // 插入元素列表到布隆过滤器
    public void insertElements(List<String> elements) {
        List<Long> hashValues = new ArrayList<>();
        for (String element : elements) {
            hashValues.addAll(Utils.myHash(element, k, size));
        }
        for (long index : hashValues) {
            bitarray.set((int) index);
        }
    }

    public boolean test(String element) {
        List<Long> hashValues = new ArrayList<>();
        hashValues = Utils.myHash(element, k, size);
        for (long index : hashValues) {
//            System.out.println("哈希值"+index+bitarray.get((int) index));
            if (!bitarray.get((int) index)) {
                return false;
            }
        }
        return true;
    }

    // 统计布隆过滤器中被设置为 1 的位的数量
    public int countOnes() {
        return bitarray.cardinality();
    }
    // 清除布隆过滤器中的所有位
    public void clear() {
        bitarray.clear();
    }

    public BitSet getBitarray(){
        return bitarray;
    }

    // 获取位数组中 0 的数量
    public int countZeros() {
        int count = 0;
        for (int i = 0; i < bitarray.size(); i++) {
            if (!bitarray.get(i)) {
                count++;
            }
        }
        return count;
    }
    // 打印 BitSet 中的每一位
    public void printBitSet() {
//        File filePath= new File("D:\\SequenceSearch\\bfoutput.txt");
//        try (FileWriter writer = new FileWriter(filePath)) {
//            writer.write("一个布隆过滤器的BitSet的内容");
//            for (int i = 0; i < size; i++) {
//                boolean bit = bitarray.get(i);
//                writer.write(bit ? '1' : '0');
//                if ((i + 1) % 100 == 0) {
//                    writer.write(System.lineSeparator()); // 每100位换行
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        System.out.println("布隆过滤器被设置为1的数量"+countOnes());
        double onePercentage = ((double) countOnes() / size) * 100;
        System.out.println("布隆过滤器的1占比"+ onePercentage + "%");
    }

}
