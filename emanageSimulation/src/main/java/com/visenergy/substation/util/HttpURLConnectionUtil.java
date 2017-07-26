package com.visenergy.substation.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpURLConnectionUtil {

	 /**
     * 向指定URL发送GET方法的请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String doGet(String url, String param, String charset, boolean pretty) {
    	StringBuffer response = new StringBuffer();
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(),charset));
            String line;
            while ((line = in.readLine()) != null) {
            	 if (pretty) 
                     response.append(line).append(System.getProperty("line.separator"));
            	 else 
                     response.append(line); 
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return response.toString();
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String doPost(String url, Map paramMap, String charset, boolean pretty) {
    	String param = null;
    	if(paramMap != null){
    		StringBuffer paramBuffer = new StringBuffer();
    		Set paramKeySet = paramMap.keySet();
    		Iterator paramKeyIter = paramKeySet.iterator();
    		while(paramKeyIter.hasNext()){//遍历集合
    			String paramKey = (String) paramKeyIter.next();//key名称
    			paramBuffer.append(paramKey+"="+paramMap.get(paramKey));
    			paramBuffer.append("&");
    		}
    		if(paramBuffer.length() > 0){
    			param = paramBuffer.substring(0, paramBuffer.length()-1);
    		}
    	}
    	
        return doPost(url,param,charset,pretty);
    }
    /**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String doPost(String url, String param, String charset, boolean pretty) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuffer response = new StringBuffer();
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            System.out.println(param);
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(),charset));
            String line;
            while ((line = in.readLine()) != null) {
            	if (pretty) 
                    response.append(line).append(System.getProperty("line.separator"));
           	 	else 
                    response.append(line); 
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return response.toString();
    } 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String x = doGet("http://localhost:8080/EngineSecurity/common.action","command=T_SYS_USERINFO.selectAll","UTF-8",false); 
        System.out.println(x); 
	}

}
