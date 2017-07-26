package com.visenergy.prototalAnalysis;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by WuSong
 * 2017-05-02
 */
public class AgreementDemo {

    public Map analysis(String command) throws Exception {
        Map map=new HashMap();
        String result = null;
        String[] commands = command.trim().split(" ");
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < commands.length; i++) {
            if (!commands[i].equalsIgnoreCase("FE")) {
                list.add(commands[i]);
            }
        }
        //解析报文格式
        String[] newCommands = list.toArray(new String[list.size()]);
        /*for (int i = 0; i < newCommands.length; i++) {
            //System.out.println(Integer.parseInt(newCommands[newCommands.length-1]));
            System.out.print(newCommands[i]+" ");
        }*/

        if (newCommands.length < 16 || newCommands.length > 22 || Integer.parseInt(newCommands[0]) != 68 || Integer.parseInt(newCommands[newCommands.length - 1]) != 16) {
            System.err.print("非法帧，无法解析！");
            result = "非法帧，无法解析！";

        } else {
            System.out.println("您的输入：" + command);
            System.out.println("原始地址：" + list);
            System.out.println("帧起始符：" + newCommands[0]);
            System.out.println("电表地址：" + Byte.parseByte(newCommands[6]) + Byte.parseByte(newCommands[5]) + Byte.parseByte(newCommands[4]) + Byte.parseByte(newCommands[3]) + Byte.parseByte(newCommands[2]) + Byte.parseByte(newCommands[1]));
            System.out.println("控制域：" + newCommands[8]);
            System.out.println("数据域长度：" + newCommands[9]);
            System.out.println("校验码：" + newCommands[newCommands.length - 2]);
            System.out.println("停止位：" + newCommands[newCommands.length - 1]);

            //int DTID=newCommands[newCommands.length - 2 - newCommands[9]];
            //解析数据标识
            List<String> list2 = new ArrayList<String>();
            for (int i = 0; i < 4; i++) {
                list2.add(Integer.toHexString(Integer.parseInt(newCommands[newCommands.length - 3 - i - (Integer.parseInt(newCommands[9], 16) - 4)], 16) - 51));
            }
            String[] DTID = list2.toArray(new String[list2.size()]);
            StringBuffer sbr = new StringBuffer();
            for (int i = 0; i < DTID.length; i++) {
                if (DTID[i].length() == 1) {
                    DTID[i] = String.format("%02d", Integer.parseInt(DTID[i]));
                } else if (DTID[i].length() == 8) {
                    DTID[i] = "FF";
                }
                sbr.append(DTID[i]);
            }

            //InputStream is=this.getClass().getClassLoader().getResourceAsStream("resource/config.properties");
            //加载文件，取值
            //InputStream is = new BufferedInputStream(new FileInputStream("src/config.properties"));
            InputStream is=AgreementDemo.class.getResourceAsStream("config.properties");

            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            Properties properties = new Properties();
            try {
                properties.load(isr);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(sbr.toString());
            System.out.println("数据项名称：" + properties.getProperty(sbr.toString()));

            //解析返回数据
            if (newCommands.length > 16) {
                int DTID0 = Integer.parseInt(DTID[0]);
                int DTID1 = Integer.parseInt(DTID[1]);
                List<String> list3 = new ArrayList();
                for (int i = 0; i < Integer.parseInt(newCommands[9], 16) - 4; i++) {
                    list3.add(newCommands[newCommands.length - 3 - i]);
                }

                String[] data = list3.toArray(new String[list3.size()]);
                //System.out.println((this.DataFormat(data)).toString());
                long num = Long.parseLong((this.DataFormat(data)).toString());
                //System.out.println((this.DataFormat(data)).toString());
                BigDecimal bigDecimal = new BigDecimal(num);
                String type = null;
                result = properties.getProperty(sbr.toString()) + ":";
                if (DTID0 == 2 && DTID1 == 1 && !String.valueOf(DTID[2]).equals("FF")) { //电压0.1v
                    //new BigDecimal()
                    System.out.println(properties.getProperty(sbr.toString()) + "：" + bigDecimal.multiply(new BigDecimal("0.1")) + "v");
                    type = bigDecimal.multiply(new BigDecimal("0.1")) + "v";
                    //result += type;


                } else if (DTID0 == 2 && DTID1 == 2) { //电流0.001A
                    System.out.println(properties.getProperty(sbr.toString()) + "：" + bigDecimal.multiply(new BigDecimal("0.001")) + "A");
                    type = bigDecimal.multiply(new BigDecimal("0.001")) + "v";


                } else if ((DTID0 == 2 && DTID1 == 3) || (DTID0 == 2 && DTID1 == 4) || (DTID0 == 2 && DTID1 == 5)) { //有无功功率0.0001
                    System.out.println(properties.getProperty(sbr.toString()) + "：" + bigDecimal.multiply(new BigDecimal("0.0001")));
                    type = bigDecimal.multiply(new BigDecimal("0.0001")) + "v";

                } else if (DTID0 == 2 && DTID1 == 6) { //功率因数0.001
                    System.out.println(properties.getProperty(sbr.toString()) + "：" + bigDecimal.multiply(new BigDecimal("0.001")));
                    type = bigDecimal.multiply(new BigDecimal("0.001")) + "v";


                } else if ((DTID0 == 0 && DTID1 == 0) || (DTID0 == 0 && DTID1 == 1) || (DTID0 == 0 && DTID1 == 2) || (DTID0 == 0 && DTID1 == 3) || (DTID0 == 0 && DTID1 == 4) || (DTID0 == 0 && DTID1 == 5) || (DTID0 == 0 && DTID1 == 6) || (DTID0 == 0 && DTID1 == 7) || (DTID0 == 0 && DTID1 == 8)) { //有无功总电能、四象限无功总电能0.01
                    System.out.println(properties.getProperty(sbr.toString()) + "：" + bigDecimal.multiply(new BigDecimal("0.01")));
                    type = bigDecimal.multiply(new BigDecimal("0.01")) + "v";


                } else if (DTID0 == 2 && DTID1 == 1 && String.valueOf(DTID[2]).equals("FF")) { //电压数据块
                    System.out.println(String.valueOf(num));
                    System.out.println(String.valueOf(num).substring(0, 4));
                    System.out.println(String.valueOf(num).substring(4, 8));
                    System.out.println(String.valueOf(num).substring(8));

                    System.out.println("C相电压" + new BigDecimal(String.valueOf(num).substring(0, 4)).multiply(new BigDecimal("0.1")));
                    System.out.println("B相电压" + new BigDecimal(String.valueOf(num).substring(4, 8)).multiply(new BigDecimal("0.1")));
                    System.out.println("A相电压" + new BigDecimal(String.valueOf(num).substring(8)).multiply(new BigDecimal("0.1")));

                } else {
                    System.out.println(properties.getProperty(sbr.toString()) + "：" + num);
                    type = String.valueOf(num);

                }
                map.put("type",type);
                map.put("result",result);
                return map;
            }

        }
        return map;
    }

    public StringBuffer DataFormat(String data[]){
        StringBuffer sbr=new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            String data1=String.valueOf(Integer.parseInt(data[i].substring(0,1),16)-3);
            String data2=String.valueOf(Integer.parseInt(data[i].substring(1),16)-3);
            sbr.append(data1);
            sbr.append(data2);
        }
        return sbr;
    }

}
