package com.chen;

import java.io.*;
import java.sql.SQLOutput;
import java.util.*;

public class test {
    public static void main(String[] args) throws IOException {
        //初次构建
//        DataPreProcessing.dataPreprocessing();
//        Build.buildIndex();
        //不是初次构建，从序列化文件中加载
        Build.buildIndexFromSER();
        Query.querykmer("TTTACGCGCTGACTGCTGTGAAATCTGGACT");

        Insert.insertDatasets("Insert_1.txt");

        Build.serializeAll();
        Query.querykmer("TTTACGCGCTGACTGCTGTGAAATCTGGACT");

    }

}
