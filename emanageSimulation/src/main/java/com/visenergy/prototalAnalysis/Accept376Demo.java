package com.visenergy.prototalAnalysis;

import java.io.*;
import java.util.Map;
import java.util.Properties;

/**
 * Created by zhonghuan on 17/5/5.
 */
public class Accept376Demo {
    Convert convert=new Convert();
    Send376Demo send376Demo=new Send376Demo();

    public String acceptCommandType(String commandType) throws Exception {
        //加载文件，取值
        //InputStream is = new BufferedInputStream(new FileInputStream("src/collect.properties"));
        InputStream is=Accept376Demo.class.getResourceAsStream("collect.properties");
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        Properties properties = new Properties();
        try {
            properties.load(isr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        commandType=convert.addSpace(commandType);
        String[] commands = commandType.trim().split(" ");
        String type=commands[12]+commands[14]+commands[15]+commands[16]+commands[17];
        if(properties.getProperty(type)==null){
            return null;
        }else if(properties.getProperty(type).equals("登录")||properties.getProperty(type).equals("心跳")){
            String confirmCommand = send376Demo.loginCommand(commandType);
            //并发送确认命令
            return confirmCommand;
        }else if(properties.getProperty(type).equals("透明转发应答")){
            String meterCommand=analysis(commandType);
            Map result=new AgreementDemo().analysis(meterCommand);
            return result.toString();
        }
        return  null;
    }
    public String analysis(String command) throws Exception {
        String[] commands = command.trim().split(" ");
        //链路层长度：用户数据长度和协议标识
        String L = convert.convertHexTo2String(commands[1]);
        String dataL = L.substring(0, 6);
        //报文长度
        int dataLength = convert.convert2To10String(dataL);
        //加载文件，取值
        //InputStream is = new BufferedInputStream(new FileInputStream("src/collect.properties"));
        InputStream is=Accept376Demo.class.getResourceAsStream("collect.properties");

        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        Properties properties = new Properties();
        try {
            properties.load(isr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("数据长度为："+dataLength);
        String dataB = L.substring(6);
        System.out.print("协议标识为：");
        if (dataB.equals("00")) {
            System.out.println("禁用;");
        } else if (dataB.equals("01")) {
            System.out.println("为《Q/GDW 130—2005电力负荷管理系统数据传输规约》使用;");
        } else if (dataB.equals("10")) {
            System.out.println("为本协议使用；");

        } else {
            System.out.println("为保留。");

        }
        //控制域（1）：传输方向位DIR（上行/下行），启动标志位PRM，帧计数位FCB/要求访问位ACD，帧计数有效位FCV/保留,功能码
        String control = convert.convertHexTo2String(commands[6]);
        String controlDIR = control.substring(0, 1);
        String controlPRM = control.substring(1, 2);
        String controlACD = control.substring(2, 3);
        String controlGNM = control.substring(4);
        int GNM = convert.convertHexTo10String(controlGNM);
        System.out.println(properties.getProperty("C.DIR."+controlDIR));
        System.out.println(properties.getProperty("C.PRM."+controlPRM));
        System.out.println(properties.getProperty("C.ACD."+controlACD));

        //地址域（5）：行政区划码A1（2），终端地址A2（2），主站地址和组地址标志A3（1）
        String A1=commands[8]+commands[7];
        String A2=commands[10]+commands[9];
        String A3=commands[11];
        String A3_2=convert.convertHexTo2String(A3);
        String A3_D0=A3_2.substring(7);
        String A3_MSA=A3_2.substring(0,7);
        System.out.println(properties.getProperty("A.A1")+":"+A1);
        System.out.print(properties.getProperty("A.A2")+":"+A2+" ");
        String A3D0=convert.convertHexTo2String(A3).substring(7);
        String MSA=convert.convertHexTo2String(A3).substring(0,7);
        System.out.println(properties.getProperty("A.A3.D0."+A3D0));
        System.out.println(properties.getProperty("A.MSA")+":"+convert.convert2To10String(MSA));

        //应用层功能码(1)
        String AFN=commands[12];
        System.out.println("应用功能AFN："+properties.getProperty("AFN."+AFN));

        //帧序列域SEQ(1):帧时间标签有效位TpV，首帧标志FIR，末帧标志FIN，请求确认标志位CON
        String SEQ=commands[13];
        String SEQ_2=convert.convertHexTo2String(SEQ);
        String SEQ_TpV=SEQ_2.substring(0,1);
        String SEQ_FIRFIN=SEQ_2.substring(1,3);
        String SEQ_CON=SEQ_2.substring(3,4);
        String SEO_PSEQ_RSEQ=SEQ_2.substring(4);
        if(SEQ_TpV.equals("0")){
            System.out.println("TpV＝0 附加信息域中无时间标签Tp");
        }else{
            System.out.println("TpV＝1 附加信息域中有时间标签Tp");
        }
        if(SEQ_FIRFIN.equals("00")){
            System.out.println("多帧：中间帧");
        }else if(SEQ_FIRFIN.equals("01")){
            System.out.println(" 多帧：结束帧");
        }else if(SEQ_FIRFIN.equals("10")){
            System.out.println("多帧：第1帧，有后续帧");
        }else{
            System.out.println("单帧");
        }
        if(SEQ_CON.equals("1")){
            System.out.println("需要对该报文进行确认");
        }else{
            System.out.println("不需要对该报文进行确认");
        }
        //数据单元标识（4）：信息点标识DA，信息类标识DT
        String DA1=commands[14];
        String DA2=commands[15];
        String DT1=commands[16];
        String DT2=commands[17];
        System.out.println(properties.getProperty(AFN+DA1+DA2+DT1+DT2));
        StringBuffer contentCommandB=new StringBuffer();
        //透明转发长度
        String contentCommandL=commands[20]+commands[19];
        int length=convert.convertHexTo10String(contentCommandL);
        //int con=Integer.valueOf(contentCommandL,16);
        int i=6+1+5+1+1+4+3;
        int j=6+1+5+1+1+4+3+length-1;
        for (;i<=j;i++){
            contentCommandB.append(commands[i]+" ");
        }
        return contentCommandB.toString();

    }


    public static void main (String[]args) throws Exception {

        String contentCommand=new Accept376Demo().analysis("68 9E 00 9E 00 68 80 31 07 01 00 02 10 E0 00 00 01 00 02 12 00 68 03 00 00 12 47 11 68 91 06 33 36 34 35 A5 56 A1 16 00 42 16 19 09 10 A2 16");
        System.out.println("转发内容"+contentCommand);
       //String result=new AgreementDemo().analysis(contentCommand);
        //System.out.println(result);
    }

}
