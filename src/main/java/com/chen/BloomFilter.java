package com.chen;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class BloomFilter {
    private int size;
    private int k;

    private BitSet bitarray;
    public BloomFilter(int size, int k) {
        this.size = size;
        this.k = k;
        this.bitarray = new BitSet(size);
    }

    public void insertElement(String element) {
        List<Long> hashValues=new ArrayList<>();
        hashValues=Utils.myHash(element,k,size);
        for(long index:hashValues) {
            bitarray.set((int) index);
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

//    public static void main(String[] args) {
//        int size = 3596288;
//        int k = 3;
//        int numElements = 745000;
//
//        // 创建布隆过滤器
//        BloomFilter bloomFilter = new BloomFilter(size, k);
//
//        // 将 745000 个元素插入到布隆过滤器中
//        for (int i = 0; i < 745000; i++) {
//            bloomFilter.insertElement("element" + i);
//        }
//
//        // 测试查询精度
//        int totalElements = 10000; // 测试查询的总元素数量
//        int positiveTests = 0; // 查询到的正样本数量
//
//        for (int i = 0; i < totalElements; i++) {
//            String element = "element" + (i+100);
//            if (bloomFilter.test(element)) {
//                positiveTests++;
//            }
//        }
//        System.out.println(bloomFilter.test("element"+750000));
//
//        // 计算查询精度
//        double precision = (double) positiveTests / totalElements;
//        System.out.println("Precision: " + precision);
//
//    }
}
