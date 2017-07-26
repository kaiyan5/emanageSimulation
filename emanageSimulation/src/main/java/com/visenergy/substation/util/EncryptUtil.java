package com.visenergy.substation.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

public class EncryptUtil {
	/**
	 * 加密类型
	 */
	public static final int SHA1 = 1;
	public static final int MD5 = 2;
	 /**
	  * 用私匙对字符串进行加密
	  * @param src 字符串
	  * @param key 密钥
	  * @param encryptNum 加密类型（1:sha1,2:MD5）
	  * @return
	  */
	 public static String encrypt(String src, String key,int encryptNum)
	 {
		 //判断加密类型，1：sha1,2:md5,其他值，默认用sha1
		 String encryptType = "";
		 if(encryptNum == 1){
			 encryptType = "SHA";
		 }else if(encryptNum == 2){
			 encryptType = "MD5";
		 }else{
			 encryptType = "SHA";
		 }
		 //随即数
		 Random random = new Random();
		 //设置种子，默认是当前时间
		 random.setSeed(System.currentTimeMillis()+random.nextInt());
		 //产生一个随机整数的字符串
		 String rand = "" + random.nextInt() % 32000;
		 //进行加密()
		 String encKey = generateKey(rand, encryptType);
		 int ctr = 0;
		 String tmp = "";
		 //加密字符与随机数进行处理加密
		 for (int i = 0; i < src.length(); i++) 
		 {
			 ctr = (ctr == encKey.length() ? 0 : ctr);
			 tmp += encKey.charAt(ctr);
			 char c = (char) (src.charAt(i) ^ encKey.charAt(ctr));
			 tmp += c;
			 ctr++;
		 }
		 //处理的字符与密钥进行加密
		 String passportKey = passportKey(tmp, key,encryptNum);
		 //将加密的内容转化成base64编码
		 return new BASE64Encoder().encode(passportKey.getBytes());
	 }

	 /**
	  * 用私匙对字符串进行解密
	  * @param src 字符串
	  * @param key 密码
	  * @param decryptNum 解密类型
	  * @return
	  */
	 public static String decrypt(String src, String key,int decryptNum) 
	 {
		 byte[] bytes = null;
		 
		 try 
		 {
			 //将字符的base64编码变成字节。
			 //主要：java -127-127 net 0-255
			 bytes = new BASE64Decoder().decodeBuffer(src);
			 src = new String(bytes);
		 }
		 catch (Exception e) 
		 {
			 return null;
		 }
		 //字符串与密钥进行运算
		 src = passportKey(src, key,decryptNum);
		 String tmp = "";
		 
		 //运算结果进行解密
		 for (int i = 0; i < src.length(); ++i) 
		 {
			 char c = (char) (src.charAt(i) ^ src.charAt(++i));
			 tmp += c;
		 }
		 return tmp;
	 }
	 /**
	  * 进行加密
	  * @param src 字符串
	  * @param key 密钥
	  * @param encryptNum 加密类型
	  * @return
	  */
	 public static String passportKey(String src, String key,int encryptNum) 
	 {
		 //判断加密类型，1：sha1,2:md5,其他值，默认用sha1
		 String encryptType = "";
		 if(encryptNum == 1){
			 encryptType = "SHA";
		 }else if(encryptNum == 2){
			 encryptType = "MD5";
		 }else{
			 encryptType = "SHA";
		 }
		 //密钥加密
		 String encKey = generateKey(key, encryptType);
		 int ctr = 0;
		 String tmp = "";
		 //加密字符与密钥进行运算
		 for (int i = 0; i < src.length(); ++i) 
		 {
			 ctr = (ctr == encKey.length() ? 0 : ctr);
			 char c = (char) (src.charAt(i) ^ encKey.charAt(ctr));
			 tmp += c;
			 ctr++;
		 }
		 return tmp;
	 }
	 /**
	  * 加密
	  * @param src 字符串
	  * @param algorithm 加密类型
	  * @return
	  */
	 public static String generateKey(String src, String algorithm)
		{
		 	//加密类
			MessageDigest m = null;

			try
			{
				//产生实例
				m = MessageDigest.getInstance(algorithm);
				//将字符串变成UTF8之后，进行加密
				m.update(src.getBytes("UTF8"));
			}
			catch (NoSuchAlgorithmException e)
			{
				e.printStackTrace();
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
			//加密之后产生的字节
			byte s[] = m.digest();
			String result = "";
			//遍历字节
			for (int i = 0; i < s.length; i++)
			{
				//字符偏移-加密
				result += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
			}
			//返回
			return result;

		}
	 /**
	  * 
	  * @param args
	 * @throws Exception 
	  */
	 public static void main(String[] args) throws Exception{
		 //uuid字符串
		 String uid = UUID.randomUUID().toString();
		 String uid1 = UUID.randomUUID().toString(); 
		 String uid2 = UUID.randomUUID().toString();
		 String key = "dsideal4r5t6y7u";
		 //————UUID字符串————
		 System.out.println("————UUID字符串————");
		 System.out.println("UUID:	"+uid);
		 System.out.println("UUID1:	"+uid1);
		 System.out.println("UUID2:	"+uid2);
		 System.out.println();
		 //加密
		 System.out.println("————加密————");
		 System.out.println("UUID UUID1:\n"+encrypt(uid+" "+uid2,key, EncryptUtil.SHA1));
		 System.out.println();
		 System.out.println("UUID UUID2:\n"+encrypt(uid+" "+uid2,key, EncryptUtil.SHA1));
		 System.out.println();
		 //解密
		 System.out.println("————解密————");
		 System.out.println("UUID UUID1:\n"+decrypt(encrypt(uid+" "+uid1,key, EncryptUtil.SHA1), key, EncryptUtil.SHA1));
		 
		//uncode编码
		System.out.println();
		String strText = "123456";
     	strText = strText.toLowerCase();
     	byte[] clearBytes = strText.getBytes("UTF-16LE");//new byte[]{49,0, 50,0, 51,0, 52,0, 53,0, 54,0};//strText.getBytes("UTF8");
     	MessageDigest m = null;
     	//产生实例
		m = MessageDigest.getInstance("MD5");
		//将字符串变成UTF8之后，进行加密
		m.update(clearBytes);
		
     	byte[] hashedBytes = m.digest();
     	String result11 = "";
     	for(int i = 0;i<hashedBytes.length;i++){
     		result11 += Integer.toHexString((hashedBytes[i] & 0x000000FF) | 0xFFFFFF00).substring(6);
     		result11 += "-";
     	}
     	result11 = result11.substring(0,result11.length()-1);
     	System.out.println(result11);
	 }

}
