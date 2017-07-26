package com.visenergy.substation.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class JdbcUtil {

	public static void executeSql(String sql) throws Exception
	{
		Connection conn = null;
		Statement statement = null;
		ResultSet rs = null;
 
		//加载properties信息
		InputStream in =  JdbcUtil.class.getClassLoader().getResourceAsStream("src\\database.properties");
		Properties properties = new Properties();
		try {
			properties.load(in);
			String driverName = properties.getProperty("driver");
			String UserName = properties.getProperty("username");
			String UserPwd = properties.getProperty("password");
			String url = properties.getProperty("url");
			
			Class.forName(driverName);
			conn = DriverManager.getConnection(url, UserName, UserPwd);
			String executeSql = sql;
			statement = conn.createStatement();
			statement.execute(executeSql);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception("运行jdbcUtil出错！");
		}  
		finally
		{
			//关闭相关的声明
			try {
				if(rs!=null)
					rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}finally
			{
				try {
					if(conn!=null)
						conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
 			}
		}
	}
}
