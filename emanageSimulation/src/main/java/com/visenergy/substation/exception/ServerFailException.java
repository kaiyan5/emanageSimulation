package com.visenergy.substation.exception;

/* 
 * <B>描述：</B>服务器器返回错误<br/>
 * <B>版本：</B>v1.0<br/>
 * <B>创建时间：</B>2015-10-10<br/>
 * @author zdf
 *
 */
public class ServerFailException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ServerFailException(String msg){
		super(msg);
	}
	
	public ServerFailException(String msg,Throwable e){
		super(msg,e);
	}
}
