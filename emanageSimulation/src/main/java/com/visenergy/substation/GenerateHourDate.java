package com.visenergy.substation;

import com.flying.jdbc.SqlHelper;
import com.flying.jdbc.data.CommandType;
import com.flying.jdbc.data.Parameter;
import com.flying.jdbc.db.type.BaseTypes;
import com.flying.jdbc.util.DBConnection;
import com.flying.jdbc.util.DBConnectionPool;
import com.visenergy.substation.util.DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhonghuan on 2017/8/22.
 */
public class GenerateHourDate {
    private static  Log log = LogFactory.getLog(GenerateHourDate.class);
    public static void init(){
        //初始化数据库连接池
        SqlHelper.connPool = new DBConnectionPool(20);

    }
    public static List<Map> getCollect(String dateStart,String dateEnd){
        String sql="SELECT METER_SERIAL,IFNULL(MAX(USE_TOTAL)-MIN(USE_TOTAL),0) TOTAL,MIN(A.TIME) TIME \n" +
                "FROM T_EMANAGE_COLLECT A \n" +
                "WHERE A.TIME BETWEEN '"+dateStart+"' AND '"+dateEnd+"' AND USE_TOTAL>=0\n" +
                " GROUP BY A.METER_SERIAL";
        DBConnection conn = SqlHelper.connPool.getConnection();
        List<Map> resultList = null;
        try {
            resultList = SqlHelper.executeQuery(conn, CommandType.Text, sql);
        } catch (Exception e) {
            log.error("查询数据出错！", e);
            return null;
        }
        SqlHelper.connPool.releaseConnection(conn);
        return resultList;
    }
    public static void insertDayCollect(String date){
        try {
            String[] ad = date.split("-");
            List hours= DateFormat.getHours(date);
            for(int j=0;j<hours.size()-1;j++){
                String dateStart=(String) hours.get(j);
                String dateEnd=(String) hours.get(j+1);
                List<Map> resultList=GenerateHourDate.getCollect(dateStart,dateEnd);

                log.info(resultList.size());
                List<Parameter[]> list=new ArrayList<Parameter[]>();
                DBConnection conn = SqlHelper.connPool.getConnection();
                String sql = "INSERT INTO T_EMANAGE_COLLECT_HOUROFMONTH_"+ad[1]+"_"+ ad[0]+" (METER_SERIAL,USE_TOTAL,TIME) values"
                        +"(?,?,?)";
                if(resultList.size()>0){
                    for(int i=0;i<resultList.size();i++) {
                        Parameter[] params = new Parameter[3];
                        params[0]=new Parameter("METER_SERIAL", BaseTypes.VARCHAR,resultList.get(i).get("METER_SERIAL"));
                        params[1]=new Parameter("USE_TOTAL",BaseTypes.DECIMAL,resultList.get(i).get("TOTAL"));
                        params[2]=new Parameter("TIME", BaseTypes.VARCHAR,dateStart);
                        list.add(params);
                    }
                    SqlHelper.executeBatchInsert(conn,CommandType.Text,sql,list);
                }else{
                    log.info(dateStart+"：该时辰无数据");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertMonthCollect(String date) throws Exception {
        String ad[]=date.split("-");
        List days=DateFormat.getDays(date);
        for (int i = 0; i <days.size()-1;i++) {
            String dateStart=(String) days.get(i);
            String dateEnd=(String) days.get(i+1);
            List<Map> resultList=GenerateHourDate.getCollect(dateStart,dateEnd);
            log.info(resultList.size());
            List<Parameter[]> list=new ArrayList<Parameter[]>();
            DBConnection conn = SqlHelper.connPool.getConnection();
            String sql = "INSERT INTO T_EMANAGE_COLLECT_DAYOFYEAR_"+ ad[0]+" (METER_SERIAL,USE_TOTAL,TIME) values"
                    +"(?,?,?)";
            if(resultList.size()>0){
                for(int j=0;j<resultList.size();j++) {
                    Parameter[] params = new Parameter[3];
                    params[0]=new Parameter("METER_SERIAL", BaseTypes.VARCHAR,resultList.get(j).get("METER_SERIAL"));
                    params[1]=new Parameter("USE_TOTAL",BaseTypes.DECIMAL,resultList.get(j).get("TOTAL"));
                    params[2]=new Parameter("TIME", BaseTypes.VARCHAR,dateStart);
                    list.add(params);
                }
                SqlHelper.executeBatchInsert(conn,CommandType.Text,sql,list);
            }else{
                log.info(dateStart+"：当日没有数据");
            }

        }

    }
    public static void insertYearCollect(String date) throws Exception {
        List months = DateFormat.getMonths(date);
        for (int i = 0; i < months.size() - 1; i++) {
            String dateStart=(String) months.get(i);
            String dateEnd=(String) months.get(i+1);
            List<Map> resultList=GenerateHourDate.getCollect(dateStart,dateEnd);
            log.info(resultList.size());
            List<Parameter[]> list=new ArrayList<Parameter[]>();
            DBConnection conn = SqlHelper.connPool.getConnection();
            String sql = "INSERT INTO T_EMANAGE_COLLECT_MONTHOFYEAR_"+ date+" (METER_SERIAL,USE_TOTAL,TIME) values"
                    +"(?,?,?)";
            if(resultList.size()>0){
                for(int j=0;j<resultList.size();j++) {
                    Parameter[] params = new Parameter[3];
                    params[0]=new Parameter("METER_SERIAL", BaseTypes.VARCHAR,resultList.get(i).get("METER_SERIAL"));
                    params[1]=new Parameter("USE_TOTAL",BaseTypes.DECIMAL,resultList.get(i).get("TOTAL"));
                    params[2]=new Parameter("TIME", BaseTypes.VARCHAR,dateStart);
                    list.add(params);
                }
                SqlHelper.executeBatchInsert(conn,CommandType.Text,sql,list);
            }else{
                log.info(dateStart+"：当月没有数据");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        init();

        insertDayCollect("2017-8-27");
        //insertMonthCollect("2017-8");
        //insertYearCollect("2017");
    }

}
