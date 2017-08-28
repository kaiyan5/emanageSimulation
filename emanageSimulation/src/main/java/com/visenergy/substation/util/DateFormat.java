package com.visenergy.substation.util;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 日期转换
 *
 * @author zhoubo
 * @create 2017-02-07 12:11
 **/
public class DateFormat {

    /**
     * 获取一个月的天数，以月初零点为起点
     * @param a
     * @return
     * @throws Exception
     */

    public static List getDays(String a) throws Exception{

        String b = String.valueOf(DateFormat.getNextMonth(a));
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = sdf1.parse(a);//提取格式中的日期
        Date date2 = sdf1.parse(b);//提取格式中的日期
        String newStra = sdf2.format(date1); //改变格式
        String newStrb = sdf2.format(date2); //改变格式

        List days = new ArrayList();// 日期集合
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            long end = sdf.parse(newStrb).getTime();
            long begin = sdf.parse(newStra).getTime();
            while (begin <= end) {
                Date day = new Date();
                day.setTime(begin);
                begin += 3600 * 24 * 1000;
                days.add(sdf.format(day));
            }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return days;
    }

    /**
     * 获取一天的24小时，以00:00:00为起点，第二天的零点为终点
     * @param a
     * @return
     * @throws Exception
     */
    public static List getHours(String a) throws Exception{
        //将前台传来的某一月份，如2016-2-1，转换成20016-02-01 00:00:00
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = sdf1.parse(a);//提取格式中的日期
        String newStra = sdf2.format(date1); //改变格式

        List hours = new ArrayList();// 日期集合
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            long end = sdf.parse(newStra).getTime();
            end+=3600*1000*24;
            long begin = sdf.parse(newStra).getTime();
            while (begin <= end) {
                Date hour = new Date();
                hour.setTime(begin);
                begin += 3600  * 1000;
                hours.add(sdf.format(hour));
           }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return hours;
    }



    /**
     *
     * @param a
     * @return 返回月份集合
     * @throws Exception
     */
    public static List getMonths(String a) throws Exception{


        List months = new ArrayList();// 日期集合
        for (int i=1;i<=12;i++){
            months.add(DateFormat.getString(a,i));
        }

        months.add(DateFormat.getString(String.valueOf(Integer.parseInt(a)+1),1));
        return months;

    }

    /**
     *
     * @param i
     * @param j
     * @return 2016-1-01 00:00:00这类的字符串
     */
    public static String getString(String i, int j){
        StringBuffer stringBuffer = new StringBuffer(i);
        stringBuffer.append("-");
        stringBuffer.append(j);
        stringBuffer.append("-01");
        stringBuffer.append(" 00:00:00");

        return String.valueOf(stringBuffer);

    }
    /**
     * 获取下一月的字符串，例如2017-2，返回结果为2017-3
     * @param a
     * @return
     * @throws Exception
     */
    public static StringBuffer getNextMonth(String a) throws Exception{
        String[] ad = a.split("-");
        int a1 = Integer.parseInt(ad[1])+1;
        StringBuffer sb = new StringBuffer(ad[0]);
        sb.append("-");
        sb.append(a1);
        return sb;
    }

    /**
     * 输入“2017-3-10”返回“2017-3-9”
     * @param today
     * @return 昨天
     */
      public static String getYesterday(String today) throws ParseException {
          SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
          Date date = f.parse(today);
          Calendar c = Calendar.getInstance();
          c.setTime(date);
          c.add(Calendar.DAY_OF_MONTH, -1);
          return f.format(c.getTime());
      }

    /**
     * 输入“2017-3-10”返回“2017-3-11”
     * @param tomorrow
     * @return 明天
     * @throws ParseException
     */
     public static String getTomorrow(String tomorrow) throws ParseException {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Date date = f.parse(tomorrow);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, 1);
        return f.format(c.getTime());
    }

    /**
     *获取当前日期七天内日期
     * 输入“2017-3-10”，返回“2017-3-10”、“2017-3-09”
     * “2017-3-08”、“2017-3-07”、“2017-3-06”
     * “2017-3-05”、“2017-3-04”
     * @param a 传入日期
     * @return 近七天日期
     * @throws ParseException
     */
    public static List getLastSevenDays(String a) throws ParseException {
        List days = new ArrayList();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Date date = f.parse(a);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        for (int i=0;i<7;i++){
            days.add(f.format(c.getTime()));
            c.add(Calendar.DAY_OF_MONTH, -1);
        }
        return days;
    }


    /**
     *获取当前日期三天内日期
     * 输入“2017-3-10”，返回“2017-3-10”、“2017-3-09”、“2017-3-08”
     * @param a 传入日期
     * @return 近三天日期
     * @throws ParseException
     */
    public static List getLastThreeDays(String a) throws ParseException {
        List days = new ArrayList();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Date date = f.parse(a);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        for (int i=0;i<3;i++){
            days.add(f.format(c.getTime()));
            c.add(Calendar.DAY_OF_MONTH, -1);
        }
        return days;
    }


    /**
     *
     * @param today
     * @return 返回当前时间的整点，例如“2017-03-14 15:00:00”
     * @throws ParseException
     */
    public static String getNowHour(String today){
        Date date = new Date();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(today+" ");
        stringBuffer.append(date.getHours()+":00:00");
        return String.valueOf(stringBuffer);
    }
    /**
     *
     * @param today
     * @return 得到过去某个小时的整点
     */
    public static String getBeforeHour(String today,int i) throws ParseException {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = f.parse(today);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.HOUR_OF_DAY, -i);
        return f.format(c.getTime());
    }
    /**
     * 返回过去24小时的整点集合
     * @param today
     * @return
     * @throws ParseException
     */
    public static List getBeforeAllHours(String today) throws ParseException {
        today = DateFormat.getNowHour(today);
        List list = new ArrayList();
        for (int i = 0; i <25; i++) {
            list.add(i,DateFormat.getBeforeHour(today,i));
        }
        return list;
    }

    /**
     * 返回两个日期之间的所有日期(包含起始时间)
     * @param dateStart
     * @param dateEnd
     * @return
     */
    public static List getBetweenDates(String dateStart, String dateEnd) throws ParseException {
        List days = new ArrayList();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = f.parse(dateStart);
        Date date2 = f.parse(dateEnd);
        Calendar start = Calendar.getInstance();
        start.setTime(date1);


        Calendar end = Calendar.getInstance();
        end.setTime(date2);
        end.add(Calendar.DAY_OF_YEAR,2);

        while(start.before(end)){
            days.add(f.format(start.getTime()));
            start.add(Calendar.DAY_OF_YEAR, 1);
        }
        return days;
    }


    public static void main(String[] args) throws Exception{
        List list = getBetweenDates("2017-8-1","2017-8-6");
        for (Object o : list){
            System.out.println("日期：" + o);
        }
        List list1 = getMonths("2017");
        for (Object o : list1){
            System.out.println("yuefen：" + o);
        }


    }
}
