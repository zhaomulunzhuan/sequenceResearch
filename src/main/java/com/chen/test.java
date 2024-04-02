package com.chen;

import java.io.*;
import java.sql.SQLOutput;
import java.util.*;

public class test {
    public static void main(String[] args) {
        //初次构建
//        DataPreProcessing.dataPreprocessing();
//        Build.buildIndex();
        //不是初次构建，从序列化文件中加载
        Build.buildIndexFromSER();
        Query.querykmer("TACTCACATATATTTTTATATTTTGTTATAC");
//        MetaData.outputMetadata();
        Delete.deleteDataset("GCF_000006885.1_ASM688v1_genomic.fna");
//        Delete.deleteDataset("GCF_000006845.1_ASM684v1_genomic.fna");
//        System.out.println(index.getBlock(6).getStatusList());
//        System.out.println(index.getBlock(7).getStatusList());
//        System.out.println(index.getBlock(8).getStatusList());
//        Insert.insertDatasets("Insert_2.txt");
//
//        MetaData.outputMetadata();
//
//        Build.serializeAll();
//        Query.querykmer("TACTCACATATATTTTTATATTTTGTTATAC");

    }


}
