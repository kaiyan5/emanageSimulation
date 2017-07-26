package com.visenergy.substation.po;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StaticVariable {
	
	public static String SERVER_IP = "";
	
	public static String SERVER_PORT = "";
	
	public static String SERVER_NAME = "";
	
	public static String SERVER_PROTOCOL = "";
	
	public static int SERVER_UPLOADTIME = 0;
	
	public static String CLIENT_CODE = "";
	
	public static String CLIENT_ACCESSADDR = "";
	
	public static String CLIENT_DATATEMPLATEADDR = "";
	
	public static String CLIENT_TIMESTAMPADDR = "";
	static{
		InputStream in = null;// 文件输入流
		Properties pp = new Properties();// 数据库属性

		try{
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream("init.properties");
			pp.load(in);// 将输入流编程属性文件
		}catch(IOException e){
			
		}
		
		StaticVariable.SERVER_IP = pp.getProperty("server.ip") == null ? "" : pp
				.getProperty("server.ip");
		StaticVariable.SERVER_PORT = pp.getProperty("server.port") == null ? "" : pp
				.getProperty("server.port");
		StaticVariable.SERVER_NAME = pp.getProperty("server.name") == null ? "" : pp
				.getProperty("server.name");
		StaticVariable.SERVER_PROTOCOL = pp.getProperty("server.protocol") == null ? "" : pp
				.getProperty("server.protocol");
		StaticVariable.SERVER_UPLOADTIME = pp.getProperty("server.uploadTime") == null ? 0 : Integer.parseInt(pp
				.getProperty("server.uploadTime"));
		StaticVariable.CLIENT_CODE = pp.getProperty("client.code") == null ? "" : pp
				.getProperty("client.code");

		StaticVariable.CLIENT_ACCESSADDR = pp.getProperty("client.accessAddr") == null ? "" : pp.getProperty("client.accessAddr");

		StaticVariable.CLIENT_DATATEMPLATEADDR = pp.getProperty("client.dataTemplateAddr") == null ? "" : pp.getProperty("client.dataTemplateAddr");

		StaticVariable.CLIENT_TIMESTAMPADDR = pp.getProperty("client.timestampAddr") == null ? "" : pp.getProperty("client.timestampAddr");

		//TODO 异常处理，当配置文件不正确时，做出不同容错处理
	}
}
