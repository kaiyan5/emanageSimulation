package com.visenergy.prototalAnalysis;

import java.io.*;
import java.util.*;

/**
 * Created by zhonghuan on 17/5/8.
 */
public class Send376Demo {
    Convert convert=new Convert();
    private Calendar calendar=new GregorianCalendar();
    private static int count=0;
    private static List<String> collectList=new ArrayList<>();
    //透明转发命令生成
    public String[] getSendCommand(String dataType, String port, String A_colletor, String[] A_meter) {
        String[] commands = new String[A_meter.length];
        /*if (A_meter.contains(","))
         commands=A_meter.split(",");
        else{
            commands = new String[1];
            commands[0]=A_meter;
        }
*/
        for (int i = 0; i < A_meter.length; i++) {

            if (count > 255) {
                count = 0;
            }
            String PFC;
            if (count < 16) {
                PFC = "0" + String.valueOf(Integer.toHexString(count));
            } else PFC = Integer.toHexString(count);
            //  System.out.println("++++++++++++"+count);
            //PFC = String.format("%02d", Integer.valueOf(Integer.toHexString(count)));
            String PSEQ_1 = "1110";
            String PSEQ_2 = convert.convertHexTo2String(PFC).substring(4);
            String PESQ = convert.convert2ToHexString(PSEQ_1 + PSEQ_2);
            count = count + 1;

            StringBuffer sendCommand = new StringBuffer();
            //透明转发起始段
            sendCommand.append("68E200E20068");
            //控制域
            sendCommand.append("41");
            //集中器地址
            sendCommand.append(A_colletor);
            //AFN功能码
            sendCommand.append("10");
            //PESQ计数器，取PFC低四位
            sendCommand.append(PESQ);
            //信息点标识DA1，DA2，DT1，DT2，DT2
            sendCommand.append("00000100");
            //通信端口号02，透明转发控制字6B（波特率、停止位、有无校验、奇偶校验，位数）
            //透明转发接收等待报文超时时间BS8，透明转达接收等待字节时间，透明转发内容字节数K
            sendCommand.append(port);
            sendCommand.append("6B8A1E1000");
            //透明转发内容起始符
            sendCommand.append("68");
            //电能表地址
            sendCommand.append(A_meter[i]);
            //电能表结束符以及控制域还有长度
            sendCommand.append("681104");
            //command为命令类型
            String a1 = dataType.substring(0, 2);
            String a2 = dataType.substring(2, 4);
            String a3 = dataType.substring(4, 6);
            String a4 = dataType.substring(6);
            String b1 = Integer.toHexString(Integer.parseInt(a1, 16) + 51);
            String b2 = Integer.toHexString(Integer.parseInt(a2, 16) + 51);
            String b3 = Integer.toHexString(Integer.parseInt(a3, 16) + 51);
            String b4 = Integer.toHexString(Integer.parseInt(a4, 16) + 51);
            String c1 = a1.equals("FF") ? b1.substring(1) : b1;
            String c2 = a2.equals("FF") ? b2.substring(1) : b2;
            String c3 = a3.equals("FF") ? b3.substring(1) : b3;
            String c4 = a4.equals("FF") ? b4.substring(1) : b4;

            String bs = c4 + c3 + c2 + c1;
            sendCommand.append(String.valueOf(bs));

            //计算透明转发内容的校验
            String CS = "68" + A_meter[i] + "681104" + bs;
            String CS_data = convert.toHexString(convert.SumCheck(convert.hexStringToBytes(CS), 1));
            sendCommand.append(CS_data);
            //透明转发结束符
            sendCommand.append("16");
            //附加字段PW消息认证
            sendCommand.append("00000000000000000000000000000000");
            //计数器
            sendCommand.append(PFC);
            //BCD码日期字段
            int second = calendar.get(Calendar.SECOND);
            int minute = calendar.get(Calendar.MINUTE);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int day = calendar.get(Calendar.DATE);
            sendCommand.append(String.format("%02d", second));
            sendCommand.append(String.format("%02d", minute));
            sendCommand.append(String.format("%02d", hour));
            sendCommand.append(String.format("%02d", day));
            sendCommand.append("10");
            String CS_jisuan = sendCommand.toString().substring(12);
            String CS_All = convert.toHexString(convert.SumCheck(convert.hexStringToBytes(CS_jisuan), 1));
            sendCommand.append(CS_All);
            sendCommand.append("16");
            commands[i] = sendCommand.toString();
        }
        return commands;
    }
    //3类数据上行报文
    public String getFailCommand(String A_colletor){
        if (count > 255) {
            count = 0;
        }
        String PFC;
        if (count < 16) {
            PFC = "0" + String.valueOf(Integer.toHexString(count));
        } else PFC = Integer.toHexString(count);
        //  System.out.println("++++++++++++"+count);
        //PFC = String.format("%02d", Integer.valueOf(Integer.toHexString(count)));
        String PSEQ_1 = "1110";
        String PSEQ_2 = convert.convertHexTo2String(PFC).substring(4);
        String PESQ = convert.convert2ToHexString(PSEQ_1 + PSEQ_2);
        count = count + 1;

        StringBuffer sendCommand = new StringBuffer();
        //起始
        sendCommand.append("68DA00DA0068");
        //控制域
        sendCommand.append("A1");
        //集中器地址
        sendCommand.append(A_colletor);
        //AFN功能码
        sendCommand.append("0E");
        //PESQ计数器，取PFC低四位
        sendCommand.append(PESQ);
        //信息点标识DA1，DA2，DT1，DT2，DT2
        sendCommand.append("00000100");

        //EC1/EC2
        sendCommand.append("0100");
        //事件记录指针
        sendCommand.append("0001");
        //事件记录类型
        sendCommand.append("09");
        //事件记录长度
        sendCommand.append("1C");
        //变更事件：分时日月年
        sendCommand.append("3013260717");
        //起止标志、测量点号
        sendCommand.append("8002");
        //异常标志
        sendCommand.append("C7");
        //发生时三相电压
        sendCommand.append("782306243322");
        //发生时三相电流
        sendCommand.append("C337B3A337B3A537B3");
        //发生时电能表正向有功电能总示值
        sendCommand.append("3333332222");
        //附加信息域EC
        sendCommand.append("0100");

        //附加信息域TP
        //计数器
        sendCommand.append(PFC);
        //BCD码日期字段
        int second = calendar.get(Calendar.SECOND);
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int day = calendar.get(Calendar.DATE);
        sendCommand.append(String.format("%02d", second));
        sendCommand.append(String.format("%02d", minute));
        sendCommand.append(String.format("%02d", hour));
        sendCommand.append(String.format("%02d", day));
        sendCommand.append("10");
        String CS_jisuan = sendCommand.toString().substring(12);
        String CS_All = convert.toHexString(convert.SumCheck(convert.hexStringToBytes(CS_jisuan), 1));
        sendCommand.append(CS_All);
        sendCommand.append("16");
        return sendCommand.toString();
    }


    //请求1类数据，F25 当前三相及总有/无功功率、功率因数，三相电压、电流、零序电流、视在功率命令生成
    public String[] getAllCommand(String A_colletor){
        if (count > 255) {
            count = 0;
        }
        String PFC;
        if(count<16){
            PFC = "0"+String.valueOf(Integer.toHexString(count));
        }else PFC=Integer.toHexString(count);
        String PSEQ_1 = "1110";
        String PSEQ_2 = convert.convertHexTo2String(PFC).substring(4);
        String PESQ = convert.convert2ToHexString(PSEQ_1 + PSEQ_2);
        count ++;
        //System.out.println(PFC+"++++++++++++++++"+count);
        StringBuffer sendCommand = new StringBuffer();
        sendCommand.append("684A004A0068");
        //控制域
        sendCommand.append("4B");
        //集中器地址
        sendCommand.append(A_colletor);
        //AFN功能码
        sendCommand.append("0c");
        sendCommand.append(PESQ);
        //信息点标识DA1，DA2，DT1，DT2，DT2
        sendCommand.append("01010103");
        sendCommand.append(PFC);
        //BCD码日期字段
        int second = calendar.get(Calendar.SECOND);
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int day = calendar.get(Calendar.DATE);
        sendCommand.append(String.format("%02d", second));
        sendCommand.append(String.format("%02d", minute));
        sendCommand.append(String.format("%02d", hour));
        sendCommand.append(String.format("%02d", day));
        sendCommand.append("10");
        String CS_jisuan = sendCommand.toString().substring(12);
        String CS_All = convert.toHexString(convert.SumCheck(convert.hexStringToBytes(CS_jisuan), 1));
        sendCommand.append(CS_All);
        sendCommand.append("16");
        String []a=new String[]{sendCommand.toString()};
        return a;
    }
    //解析集中器发过来的登录命令
    //举例：683200320068 C9 3107010000 02 70 00000100 75 16
    public String loginCommand(String loginCommand) throws FileNotFoundException, UnsupportedEncodingException {
        //加载文件，取值
       // InputStream is = new BufferedInputStream(new FileInputStream("src/collect.properties"));
        InputStream is=Send376Demo.class.getResourceAsStream("collect.properties");
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        Properties properties = new Properties();
        try {
            properties.load(isr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] comands=loginCommand.split(" ");
        String length=comands[2]+comands[1];
        String length2=convert.convertHexTo2String(length);
        int lengthdata=convert.convert2To10String(length2.substring(0,14));
        String control=convert.convertHexTo2String(comands[6]);
        String controlDIR = control.substring(0, 1);
        String controlPRM = control.substring(1, 2);
        String controlACD = control.substring(2, 3);
        String controlGNM = control.substring(4);
        String collectAddress=comands[7]+comands[8]+comands[9]+comands[10]+comands[11];
        String AFN=comands[12];
        String DTDA=comands[14]+comands[15]+comands[16]+comands[17];
        String AFNNAME=properties.getProperty("AFN."+AFN);
        String AFNTYPENAME=properties.getProperty(AFN+DTDA);
        System.out.println(properties.getProperty("C.DIR."+controlDIR));
        System.out.println(properties.getProperty("C.PRM."+controlPRM));
        System.out.println(properties.getProperty("C.ACD."+controlACD));
        System.out.println(properties.getProperty("A.A1")+":"+comands[8]+comands[7]);
        System.out.print(properties.getProperty("A.A2")+":"+comands[10]+comands[9]+" ");
        String A3D0=convert.convertHexTo2String(comands[11]).substring(7);
        String MSA=convert.convertHexTo2String(comands[11]).substring(0,7);
        System.out.println(properties.getProperty("A.A3.D0."+A3D0));
        System.out.println(properties.getProperty("A.MSA")+":"+convert.convert2To10String(MSA));

        System.out.println("数据长度："+lengthdata);
        System.out.println("命令类型:"+AFNNAME);
        System.out.println("应用："+AFNTYPENAME);
        StringBuffer confirmComand=new StringBuffer();
        confirmComand.append("6832003200680b");
        confirmComand.append(collectAddress);
        confirmComand.append("007000000100");
        String CS_countBefore="0b"+collectAddress+"007000000100";
        String CS_countAfter=convert.toHexString(convert.SumCheck(convert.hexStringToBytes(CS_countBefore),1));
        confirmComand.append(CS_countAfter);
        confirmComand.append("16");
        collectList.add(collectAddress);
        return confirmComand.toString();
    }

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        //String b=new Send376Demo().loginCommand("683200320068C931070100000270000001007516");

        //System.out.println(b);
        /*String b[]=new String[1];
        b[0]="210000119039";
        String[] a = new Send376Demo().getSendCommand("02010300","01", "0417090000",b);
        for (int i = 0; i <a.length ; i++) {
            System.out.println(a[i]);
        }*/
        /*while (count<255){
            String PFC;
            if(count<16){
                PFC = "0"+String.valueOf(Integer.toHexString(count));
            }else PFC=Integer.toHexString(count);
          count++;
          System.out.println(PFC+"++++++++++++++++"+count);
        }
        String allCommand=new Send376Demo().getAllCommand("3107010000");
        System.out.println(allCommand);
        String a[]=new Send376Demo().getSendCommand("02010300","02", "3107010000","020000999999");
        System.out.println(a[0]);*/
       // String all[]=new Send376Demo().getAllCommand("0417090000");
       // System.out.print(all[0]);
        String a=new Send376Demo().getFailCommand("0417090000");
        System.out.println(a);
        try {
            new Accept376Demo().analysisFailCommand(a);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
