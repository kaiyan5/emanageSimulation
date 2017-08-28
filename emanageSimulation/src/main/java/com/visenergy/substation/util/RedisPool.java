package com.visenergy.substation.util;

import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;

import javax.management.remote.rmi.RMIConnector;

/**
 * Created by zhonghuan on 17/7/9.
 */
public class RedisPool {
    private static JedisPool pool = null;
    private static Logger logger = Logger.getLogger(RedisPool.class);

    public static JedisPool getPool() {

        if (pool == null) {

            JedisPoolConfig config = new JedisPoolConfig();

            //控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；

            //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。

            //config.setMaxActive(10000);
            config.setMaxTotal(10000);

            //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。

            config.setMaxIdle(1000);

            //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；

            config.setMaxWaitMillis(10000*100);

            //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；

            config.setTestOnBorrow(true);

            pool = new JedisPool(config, "192.168.100.100", 6379);

        }

        return pool;

    }

    /**
     * Handle jedisException, write log and return whether the connection is broken.
     */
    public static boolean handleJedisException(JedisException jedisException) {
        if (jedisException instanceof JedisConnectionException) {
            logger.error("Redis connection  lost.", jedisException);
        } else if (jedisException instanceof JedisDataException) {
            if ((jedisException.getMessage() != null) && (jedisException.getMessage().indexOf("READONLY") != -1)) {
                logger.error("Redis connection  are read-only slave.", jedisException);
            } else {
                // dataException, isBroken=false
                return false;
            }
        } else {
            logger.error("Jedis exception happen.", jedisException);
        }
        return true;
    }
    /**
     * Return jedis connection to the pool, call different return methods depends on the conectionBroken status.
     */
    public static void closeResource(Jedis jedis, boolean conectionBroken) {
        try {
            if (conectionBroken) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        } catch (Exception e) {
            logger.error("return back jedis failed, will fore close the jedis.", e);
            pool.returnBrokenResource(jedis);
        }
    }
    public static void returnResource(JedisPool pool, Jedis redis) {

        if (redis != null) {

            pool.returnResource(redis);

        }
    }

    public static String get(String key){

        String value = null;

        JedisPool pool = null;

        Jedis jedis = null;

        try {

            pool = getPool();

            jedis = pool.getResource();

            value = jedis.get(key);

        } catch (Exception e) {

            //释放redis对象

            pool.returnBrokenResource(jedis);

            e.printStackTrace();

        } finally {

            //返还到连接池

            returnResource(pool, jedis);
        }
        return value;

    }
}
