package com.visenergy.substation;

import com.flying.jdbc.SqlHelper;
import com.flying.jdbc.data.CommandType;
import com.flying.jdbc.data.Parameter;
import com.flying.jdbc.db.type.BaseTypes;
import com.flying.jdbc.util.DBConnection;
import com.flying.jdbc.util.DBConnectionPool;
import com.visenergy.substation.util.RedisPool;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * 
 * 生成用能管理相关数据
 * @author zhonghuan
 */
public class GenerateData {
	private static Log log = LogFactory.getLog(GenerateData.class);
	public static Jedis jedis=null;
	public static JedisPool jedisPool=null;
	public static List<String> meters=new ArrayList<>();
	public static void init(){
		//初始化数据库连接池
		SqlHelper.connPool = new DBConnectionPool(20);
		jedisPool=RedisPool.getPool();
		jedis=jedisPool.getResource();
	}

	/**
	 * 生成1条采集数据记录
	 */
	public static void generateCollectData(String METER_ID,double USE_TOTAL,Timestamp time){
		String sql = "insert  into T_EMANAGE_COLLECT(ID,METER_ID,UA,UB,UC,IA,IB,IC,PA,PB,PC,U_TOTAL,I_TOTAL,P_TOTAL,USE_TOTAL,TIME) values" +
				"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Map map=new HashMap();
		map.put("METER_ID",METER_ID);
		map.put("USE_TOTAL",USE_TOTAL);
		map.put("time",time);
		//collectList.add(map);
		String collectString="{METER_ID='"+METER_ID+"',USE_TOTAL='"+USE_TOTAL+"',time='"+time+"'}";
		try {
			jedis.lpush("listCollect",collectString);

		}catch (Exception e){
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);

			e.printStackTrace();
		}finally {
			//返还到连接池
			RedisPool.returnResource(jedisPool, jedis);
		}

		Parameter[] params = new Parameter[16];
		DBConnection conn = SqlHelper.connPool.getConnection();
		String id = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
		params[0] = new Parameter("ID", BaseTypes.VARCHAR,id);
		params[1] = new Parameter("METER_ID", BaseTypes.VARCHAR,METER_ID);
		params[2] = new Parameter("UA", BaseTypes.DECIMAL,220.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
		
		params[3] = new Parameter("UB", BaseTypes.DECIMAL,210.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
		params[4] = new Parameter("UC", BaseTypes.DECIMAL,200.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
		params[5] = new Parameter("IA", BaseTypes.DECIMAL,10.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
		params[6] = new Parameter("IB", BaseTypes.DECIMAL,9.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
		params[7] = new Parameter("IC", BaseTypes.DECIMAL,11.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
		params[8] = new Parameter("PA", BaseTypes.DECIMAL,2200.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
		
		params[9] = new Parameter("PB", BaseTypes.DECIMAL,1890.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
		params[10] = new Parameter("PC", BaseTypes.DECIMAL,2200.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
		params[11] = new Parameter("U_TOTAL", BaseTypes.DECIMAL,220.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
		params[12] = new Parameter("I_TOTAL", BaseTypes.DECIMAL,30.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
		params[13] = new Parameter("P_TOTAL", BaseTypes.DECIMAL,4400.0 + (new BigDecimal(Math.random()).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
		params[14] = new Parameter("USE_TOTAL", BaseTypes.DECIMAL,USE_TOTAL);
		params[15] = new Parameter("TIME", BaseTypes.TIMESTAMP,time);
		try {
			SqlHelper.executeNonQuery(conn, CommandType.Text, sql, params);
			//SqlHelper.executeBatchInsert(conn,CommandType.Text, sql, params);
			//log.info("插入一条采集数据");
			//waitForSendLog("T_EMANAGE_COLLECT",id);
		} catch (Exception e) {
			conn.Close();
			log.error("采集数据插入异常",e);
		}
		SqlHelper.connPool.releaseConnection(conn);
	}

	public static void waitForSendLog(final String tableName,final String tableId){
		DBConnection conn = SqlHelper.connPool.getConnection();
		try {
			String sql = "INSERT INTO T_EMANAGE_LOG(TABLE_NAME,TABLE_ID) VALUES(?,?)";
			Parameter[] paramsLog = new Parameter[2];
			paramsLog[0] = new Parameter("TABLE_NAME", BaseTypes.VARCHAR,tableName);
			paramsLog[1] = new Parameter("TABLE_ID", BaseTypes.VARCHAR,tableId);
			SqlHelper.executeNonQuery(conn, CommandType.Text, sql, paramsLog);
			//log.info("插入一条日志");

			} catch (Exception e) {
			log.error("["+tableName + " : "+ tableId + "]添加日志出错", e);
		}
		SqlHelper.connPool.releaseConnection(conn);
	}

	/**
	 * 生成所有的采集数据
	 */
	public static void generateAllCollectData(){
		double USE_TOTAL=0;
		Timestamp time=null;
		/*if(collectList.size()>0){
			for (int f=0;f<meterList.size();f++){
				if(collectList.get(f).get("METER_ID").equals(meterList.get(f))){
					USE_TOTAL=new BigDecimal(Math.random()*50+1).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+(double)collectList.get(f).get("USE_TOTAL");
					time=new Timestamp(((Timestamp) collectList.get(f).get("time")).getTime()+20*60*1000);
					generateCollectData(meterList.get(f),USE_TOTAL,time);
				}
			}
			for (int s=0;s<meterList.size();s++){
				int i=0;
				collectList.remove(collectList.get(i));
				i--;
			}
		}
		else{
			USE_TOTAL=1000.00 + (new BigDecimal(Math.random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			time=new Timestamp(System.currentTimeMillis()-186*24*3600*1000L);
			for (int i = 0; i < meterList.size(); i++) {
				generateCollectData(meterList.get(i),USE_TOTAL,time);
			}
		}*/
		jedisPool=RedisPool.getPool();
		jedis=jedisPool.getResource();
		Long length=jedis.llen("listCollect");
		meters=jedis.lrange("meterCollect",0,-1);
		if(jedis.llen("listCollect")>0){
			for(int f=0;f<meters.size();f++){
				JSONObject jsonObject=JSONObject.fromObject(jedis.rpop("listCollect"));
				Timestamp t=Timestamp.valueOf(jsonObject.getString("time"));
				if(jsonObject.get("METER_ID").equals(meters.get(f))){
					USE_TOTAL=new BigDecimal(Math.random()*1+0.8).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+jsonObject.getDouble("USE_TOTAL");
					time=new Timestamp(t.getTime()+1*60*1000L);
					generateCollectData(meters.get(f),USE_TOTAL,time);
				}
			}
			for (int f=0;f<meters.size();f++){
				JSONObject jsonObject=JSONObject.fromObject(jedis.rpop("listCollect"));
				Timestamp t=Timestamp.valueOf(jsonObject.getString("time"));
				if(jsonObject.get("METER_ID").equals(meters.get(f))){
					USE_TOTAL=new BigDecimal(Math.random()*1+3).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+jsonObject.getDouble("USE_TOTAL");
					time=new Timestamp(t.getTime()+59*60*1000L);
					generateCollectData(meters.get(f),USE_TOTAL,time);

					//USE_TOTAL=new BigDecimal(Math.random()*200+200).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+jsonObject.getDouble("USE_TOTAL");
					//time=new Timestamp(t.getTime()+2*24*3600*1000L);
					//USE_TOTAL=new BigDecimal(Math.random()*2+3).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+jsonObject.getDouble("USE_TOTAL");
					//time=new Timestamp(t.getTime()+1*3600*1000L);
					//USE_TOTAL = new BigDecimal(Math.random() * 50 + 50).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + jsonObject.getDouble("USE_TOTAL");
					//time = new Timestamp(t.getTime() + 23 * 3600 * 1000L);

				}
			}

		}
		else{
			USE_TOTAL=1000.00 + (new BigDecimal(Math.random()+1).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			time=new Timestamp(System.currentTimeMillis()-556*24*3600*1000L);
			for (int i = 0; i < meters.size(); i++) {
				generateCollectData(meters.get(i),USE_TOTAL,time);
			}
		}
		RedisPool.returnResource(jedisPool,jedis);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GenerateData.init();
		for(int i=0;i<=5;i++) {
			GenerateData.generateAllCollectData();
		}

		/*Runnable runnable = new Runnable() {
			public void run() {
				System.out.println("iiiiiiii");
			}
		};
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		// 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
		service.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);*/
		/*Timestamp time1 = new Timestamp(System.currentTimeMillis() - 556 * 24 * 3600 * 1000L);
		Timestamp time2 = new Timestamp(time1.getTime()+27*24*3600*1000L);
		System.out.println(time1);
		System.out.println(time2);
		System.out.println(Math.random()*1500+1700);*/
	}
}
