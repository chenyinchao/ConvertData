package com.ycchen.dev;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // String inputFilePath = args[0];
        // String inputFilePath = "C:\\Users\\smile\\Desktop\\Test.java";
        System.out.println("请输入需要格式化的文件路径：");
        String inputFilePath = sc.nextLine();
        System.out.println("请输入数字：\n 0: 根据后端JavaBean转换成Android端的JavaBean \n 1: 根据后端JavaBean生成get方法 ---》"
                + " Android插件“Generate all setter ”可以生成set方法 \n 2: 根据后端API注释生成Android端RxHttp请求的形参"
                + "+ \n 3: 根据后端API注释生成Android端RxHttp请求的.add()方法");
        String convertType = sc.nextLine();

        try {
            FileInputStream in = new FileInputStream(inputFilePath);
            InputStreamReader inReader = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader bufReader = new BufferedReader(inReader);
            FileOutputStream out = new FileOutputStream(inputFilePath + "ConvertFile.txt");
            OutputStreamWriter outWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            BufferedWriter bufWrite = new BufferedWriter(outWriter);
            String lineText;
            int i = 1;
            while ((lineText = bufReader.readLine()) != null) {
                i++;
                lineText = startConvert(convertType, lineText).trim();
                if (lineText.isEmpty()) {
                    continue;
                }
                bufWrite.write(lineText + "\r\n");
            }
            bufReader.close();
            inReader.close();
            in.close();
            bufWrite.close();
            outWriter.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String startConvert(String convertType, String lineText) {
        switch (convertType) {
            case "0":
                // 根据后端JavaBean转换成Android端的JavaBean
                return convertToAndroidBean(lineText);
            case "1":
                // 根据后端JavaBean生成get方法 ---》 Android插件“Generate all setter ”可以生成set方法
                return generateAllGetter(lineText);
            case "2":
                // 根据后端API注释生成Android端RxHttp请求的形参
                return generateAllParams(lineText);
            case "3":
                // 根据后端API注释生成Android端RxHttp请求的.add()方法
                return generateAllAddMethod(lineText);
            default:
                return "";
        }
    }

    // 将以下格式的后端JavaBean
    // /**
    // * 货主id
    // */
    // @ApiModelProperty("货主id")
    // @TableField("owner_id")
    // private String ownerId;
    // 转换成适合Android端的JavaBean------------》1.注释，2.变量名
    // //"货主id"
    // private String ownerId;
    private static String convertToAndroidBean(String lineText) {
        // @Table 开头则跳过
        Matcher m1 = Pattern.compile("@Table.+").matcher(lineText);
        // *
        Matcher m2 = Pattern.compile("\\*").matcher(lineText);
        // import空格
        Matcher m3 = Pattern.compile("import ").matcher(lineText);
        // package空格
        Matcher m4 = Pattern.compile("package ").matcher(lineText);
        while (m1.find() || m2.find() || m3.find() || m4.find()) {
            return "";
        }
        Matcher matcher = Pattern.compile("(\".+\")").matcher(lineText);
        while (matcher.find()) {
            return "// " + matcher.group();
        }
        return lineText;
    }

    // 将以下格式的后端JavaBean
    // /**
    // * 货主id
    // */
    // @ApiModelProperty("货主id")
    // @TableField("owner_id")
    // private String ownerId;
    // 先转换成适合Android端的JavaBean------------》1.注释，2.变量名
    // //"货主id"
    // private String ownerId;
    // 后转换成------------》1.成员变量加m，2.驼峰命名法，3.加get方法
    // mOwnerId = dataBean.getOwnerId();
    private static String generateAllGetter(String lineText) {
        String str = convertToAndroidBean(lineText).trim();
        /**
         * 生成dataBean引用 例子：“ private Integer allocSn;” 1. 提取最后一个单词并去掉空格 2. 使单词首字母大写 3. 拼接get方法
         */
        if (str.isEmpty() || str.lastIndexOf(" ") < 0 || str.lastIndexOf(";") < 0) {
            return "";
        }
        String lastWord = str.substring(str.lastIndexOf(" "), str.lastIndexOf(";")).trim();
        String upperCaseWord = (lastWord.substring(0, 1).toUpperCase() + lastWord.substring(1)).trim();
        return ("m" + upperCaseWord + " = dataBean.get" + upperCaseWord + "();\n").trim();
    }

    // 将以下格式的后端API注释
    // (@Pd(name = "staffName", desc = "职员名称", required = false) String staffName,
    // 转换成------------》
    // String staffName,
    private static String generateAllParams(String lineText) {
        String str = lineText;
        // @Pd到空格
        Matcher m1 = Pattern.compile("@Pd.+\\) ").matcher(str);
        while (m1.find()) {
            return str.replace(m1.group(), "").trim();
        }
        return str;
    }

    // 将以下格式的后端API注释
    // (@Pd(name = "staffName", desc = "职员名称", required = false) String staffName,
    // 先转换成------------》
    // String staffName,
    // 后转换成------------》
    // .add("staffName", staffName)
    private static String generateAllAddMethod(String lineText) {
        StringBuilder stringBuilder = new StringBuilder("");
        String str = generateAllParams(lineText).trim();
        // 对String abc, ----> 按照空格切割
        String[] split = str.split(" ");
        String word = split[1];
        if (word.endsWith(",")) {
            word = word.replace(",", "");
        }
        return stringBuilder.append(".add(\"" + word + "\", " + word + ")").toString().trim();
    }
}
