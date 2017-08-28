package com.visenergy.substation;

import com.flying.jdbc.SqlHelper;
import com.flying.jdbc.data.CommandType;
import com.flying.jdbc.data.Parameter;
import com.flying.jdbc.db.type.BaseTypes;
import com.flying.jdbc.util.DBConnection;
import com.flying.jdbc.util.DBConnectionPool;
import com.visenergy.substation.util.RedisPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by zhonghuan on 17/7/10.
 */
public class GenerateDevice {
    private static Log log = LogFactory.getLog(GenerateData.class);
    public static List<Map> collectorList=new ArrayList<>();
    public static List<String> meterList=new ArrayList<>();
    public static Jedis jedis=null;
    public static JedisPool jedisPool=null;
    public static void init(){
        //初始化数据库连接池
        SqlHelper.connPool = new DBConnectionPool(20);
        jedisPool= RedisPool.getPool();
        Jedis jedis = null;

    }
    public static List<Map> getBuilding() {
        String sql = "SELECT * FROM T_EMANAGE_BUILDING";
        //log.info("查询楼宇表中的数据");
        List<Map> resultList = null;
        DBConnection conn = SqlHelper.connPool.getConnection();
        try {
            resultList = SqlHelper.executeQuery(conn, CommandType.Text, sql);
        } catch (Exception e) {
            log.error("查询数据出错！", e);
            return null;
        }
        SqlHelper.connPool.releaseConnection(conn);
        return resultList;
    }

    public static List<Map> getCollect(){
        String sql="SELECT A.METER_ID,A.USE_TOTAL,A.TIME FROM T_EMANAGE_COLLECT A LEFT JOIN T_EMANAGE_METER B ON A.METER_ID=B.ID WHERE A.TIME='2017-07-05 00:01:59' ORDER BY A.METER_ID DESC";
        List<Map> resultList = null;
        DBConnection conn = SqlHelper.connPool.getConnection();
        try {
            resultList = SqlHelper.executeQuery(conn, CommandType.Text, sql);
        } catch (Exception e) {
            log.error("查询数据出错！", e);
            return null;
        }
        SqlHelper.connPool.releaseConnection(conn);
        return resultList;
    }
    public static List<Map> getMeter(){
        String sql="SELECT ID FROM T_EMANAGE_METER ORDER BY ID DESC";
        List<Map> resultList = null;
        DBConnection conn = SqlHelper.connPool.getConnection();
        try {
            resultList = SqlHelper.executeQuery(conn, CommandType.Text, sql);
        } catch (Exception e) {
            log.error("查询数据出错！", e);
            return null;
        }
        SqlHelper.connPool.releaseConnection(conn);
        return resultList;
    }
    /**
     * 生成1条集中器数据
     */
    public static void generateCollectorData(String buildingID,int buildingFloor){
        String sql="insert into T_EMANAGE_COLLECTOR(ID,STATUS,BUILDING_ID,FLOOR,PRODUCTION_DATE,INSTALLATION_DATE) values(?,1,?,?,?,?)";
        Map map=new HashMap();
        Parameter[]parameters=new Parameter[5];
        String id = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        parameters[0] = new Parameter("ID", BaseTypes.VARCHAR,id);
        DBConnection conn = SqlHelper.connPool.getConnection();
        parameters[1] = new Parameter("BUILDING_ID", BaseTypes.VARCHAR,buildingID);
        parameters[2] = new Parameter("FLOOR", BaseTypes.INTEGER,buildingFloor);
        parameters[3] =new Parameter("PRODUCTION_DATE", BaseTypes.TIMESTAMP,"2011-01-20");
        parameters[4] =new Parameter("INSTALLATION_DATE", BaseTypes.TIMESTAMP,"2015-12-01 09:57:08");

        try {
            SqlHelper.executeNonQuery(conn, CommandType.Text, sql, parameters);
            //log.info("插入一条集中器数据");
            //waitForSendLog("T_EMANAGE_COLLECTOR",id);
        } catch (Exception e) {
            log.error("集中器数据插入异常",e);
        }
        map.put("ID",id);
        map.put("BUILDING_ID",buildingID);
        map.put("FLOOR",buildingFloor);
        SqlHelper.connPool.releaseConnection(conn);
        collectorList.add(map);

    }
    /**
     * 生成1条电能表数据
     */
    public static void generateMeterData(String COLLECTOR_ID,String BUILDING_ID,int FLOOR,String type){
        String sql="insert into T_EMANAGE_METER(ID,COLLECTOR_ID,BUILDING_ID,FLOOR,CONNECT,U_VOLTAGE,MAX_VOLTAGE,I_CURRENT,MAX_CURRENT," +
                "TYPE,STATUS,INSTALL_TIME) values (?,?,?,?,'3X4X',?,?,?,?,?,1,'2015-12-04 09:57:08')";
        Parameter[] params = new Parameter[9];
        DBConnection conn = SqlHelper.connPool.getConnection();
        String id = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        params[0] = new Parameter("ID", BaseTypes.VARCHAR,id);
        params[1] = new Parameter("COLLECTOR_ID", BaseTypes.VARCHAR,COLLECTOR_ID);
        params[2] = new Parameter("BUILDING_ID", BaseTypes.VARCHAR,BUILDING_ID);
        params[3] = new Parameter("FLOOR", BaseTypes.INTEGER,FLOOR);
        params[4] = new Parameter("U_VOLTAGE", BaseTypes.DECIMAL,220.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
        params[5] = new Parameter("MAX_VOLTAGE", BaseTypes.DECIMAL,230.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
        params[6] = new Parameter("I_CURRENT", BaseTypes.DECIMAL,10.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
        params[7] = new Parameter("MAX_CURRENT", BaseTypes.DECIMAL,12.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
        params[8] = new Parameter("TYPE", BaseTypes.VARCHAR,type);
        try {
            SqlHelper.executeNonQuery(conn, CommandType.Text, sql, params);
            //log.info("插入一条电能表数据");
            //waitForSendLog("T_EMANAGE_METER",id);
        } catch (Exception e) {
            log.error("电能表数据插入异常",e);
        }

        SqlHelper.connPool.releaseConnection(conn);
        try {
            jedis.lpush("meterCollect",id);

        }catch (Exception e){
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);

            e.printStackTrace();
        }

       // meterList.add(id);
    }
    /**
     *生成集中器和电能表所有的数据
     **/
    public static void generateAllDeviceData(){
        List<Map> buildingList=getBuilding();
        for (int i = 0; i <buildingList.size() ; i++) {
            int FLOOR=(int)buildingList.get(i).get("FLOOR");
            String BUILDING_ID=buildingList.get(i).get("ID").toString();
            //有16、17层的按两个集中器来考虑
            if(FLOOR==16||FLOOR==17){
                generateCollectorData(BUILDING_ID,12);
                generateCollectorData(BUILDING_ID,13);
                List<String> collectors=new ArrayList<>();
                for(int a=0;a<collectorList.size();a++){
                    if(collectorList.get(a).get("BUILDING_ID").equals(BUILDING_ID)){
                        collectors.add(collectorList.get(a).get("ID").toString());
                    }
                }
                String COLLECTOR_ID1=collectors.get(0);
                String COLLECTOR_ID2=collectors.get(1);
                for(int j=1;j<=7;j++) {
                    generateMeterData(COLLECTOR_ID1, BUILDING_ID, j, "KTYN");
                    generateMeterData(COLLECTOR_ID1, BUILDING_ID, j, "QTYN");
                    generateMeterData(COLLECTOR_ID1, BUILDING_ID, j, "ZMYN");
                    generateMeterData(COLLECTOR_ID1, BUILDING_ID, j, "DLYN");
                }
                for(int k=8;k<=FLOOR;k++){
                    generateMeterData(COLLECTOR_ID2, BUILDING_ID, k,"KTYN");
                    generateMeterData(COLLECTOR_ID2, BUILDING_ID, k,"QTYN");
                    generateMeterData(COLLECTOR_ID2, BUILDING_ID, k,"ZMYN");
                    generateMeterData(COLLECTOR_ID2, BUILDING_ID, k,"DLYN");
                }

            }
            //其余的按1个集中器来考虑
            else{
                generateCollectorData(BUILDING_ID,1);
                String collectorId = null;
                for(int a=0;a<collectorList.size();a++){
                    if(collectorList.get(a).get("BUILDING_ID").equals(BUILDING_ID)){
                        collectorId=collectorList.get(a).get("ID").toString();
                    }
                }
                for (int b=1;b<=FLOOR;b++){
                    generateMeterData(collectorId, BUILDING_ID, b,"KTYN");
                    generateMeterData(collectorId, BUILDING_ID, b,"QTYN");
                    generateMeterData(collectorId, BUILDING_ID, b,"ZMYN");
                    generateMeterData(collectorId, BUILDING_ID, b,"DLYN");
                }
            }
            //返还到连接池
            RedisPool.returnResource(jedisPool, jedis);
        }
    }

    public static void main(String[] args) {
        GenerateDevice.init();
        List<Map> listCollect=getCollect();
        List<Map> listMeter=getMeter();
        boolean broken = false;
        /*生成采集数据最新事件的一批数据保存在redis*/
        try {
            jedis = jedisPool.getResource();
            for (int i = 0; i <listCollect.size() ; i++) {
                String METER_ID=listCollect.get(i).get("METER_ID").toString();
                String USE_TOTAL=listCollect.get(i).get("USE_TOTAL").toString();
                String time=listCollect.get(i).get("TIME").toString();
                String collectString="{METER_ID='"+METER_ID+"',USE_TOTAL='"+USE_TOTAL+"',time='"+time+"'}";
                jedis.lpush("listCollect",collectString);
            }
        /*电能表的表号放在redis里*/
            /*for (int j=0;j<listMeter.size();j++){
                jedis.rpush("meterCollect",listMeter.get(j).get("ID").toString());
            }*/
        } catch (JedisException e) {
            broken = RedisPool.handleJedisException(e);
            throw e;
        } finally {
            RedisPool.closeResource(jedis, broken);
        }

        //GenerateDevice.generateAllDeviceData();
    }
}
