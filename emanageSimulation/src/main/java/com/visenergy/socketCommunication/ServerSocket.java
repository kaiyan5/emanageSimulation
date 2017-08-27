package com.visenergy.socketCommunication;

import com.visenergy.prototalAnalysis.Accept376Demo;
import com.visenergy.prototalAnalysis.Send376Demo;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author WuSong
 * @create 2017-05-25 10:36
 */
public class ServerSocket {
	private int port = 6001;
	/*发送数据缓冲区*/
	private ByteBuffer sBuffer = ByteBuffer.allocate(1024*1024*5);
	/*接受数据缓冲区*/
	private ByteBuffer rBuffer = ByteBuffer.allocate(1024*1024*5);
	/*映射客户端channel */
	private String[] sendText;
	private String sendText1;
	private Map<String, SocketChannel> clientsMap = new HashMap<String, SocketChannel>();
	private Selector selector;
	private SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss", Locale.US);
	private final Logger log = Logger.getLogger(ServerSocket.class);
	private List list=new ArrayList();

	Accept376Demo accept376 = new Accept376Demo();
	Send376Demo send376 = new Send376Demo();
	public ServerSocket(){
		try {
			init();
			writeThread();
			listen();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void init() throws Exception{
		/**
		 *启动服务器端，配置为非阻塞，绑定端口，注册accept事件
		 *ACCEPT事件：当服务端收到客户端连接请求时，触发该事件
		 */
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		java.net.ServerSocket serverSocket = serverSocketChannel.socket();
		serverSocket.bind(new InetSocketAddress(port));
		selector = Selector.open();
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		log.debug("server start on port:"+port);
	}

	/**
	 * 服务器端轮询监听，select方法会一直阻塞直到有相关事件发生或超时
	 */
	private void listen(){
		while (true) {
			try {
				selector.select();//返回值为本次触发的事件数
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				for(SelectionKey key : selectionKeys){
					log.debug("处理事件");
					handle(key);
				}
				selectionKeys.clear();//清除处理过的事件
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
	/**
	 * 处理不同的事件
	 */
	private void handle(SelectionKey selectionKey) throws IOException {

		ServerSocketChannel server = null;
		SocketChannel client = null;
		String receiveText=null;
		int count=0;
		if (selectionKey.isAcceptable()) {
			/**
			 * 客户端请求连接事件
			 * serversocket为该客户端建立socket连接，将此socket注册READ事件，监听客户端输入
			 * READ事件：当客户端发来数据，并已被服务器控制线程正确读取时，触发该事件
			 */
			server = (ServerSocketChannel) selectionKey.channel();
			client = server.accept();
			client.configureBlocking(false);
			clientsMap.put(client.socket().getInetAddress().toString().substring(1),client);
			log.debug("有客户端注册");
			client.register(selector, SelectionKey.OP_READ);
			//client.register(selector, SelectionKey.OP_WRITE);
		} else if (selectionKey.isReadable()) {
	            /*
	             * READ事件，收到客户端发送数据，读取数据后继续注册监听客户端
	             */
			client = (SocketChannel) selectionKey.channel();
			count = client.read(rBuffer);
			if (count>0) {
				rBuffer.flip();
				receiveText = SocketFormatTools.toHexString(this.dataConver(rBuffer));
				log.debug("收到"+client.socket().getInetAddress().toString().substring(1)+"的消息"+":"+receiveText);
				rBuffer.clear();

				try {
					sendText1 = accept376.acceptCommandType(receiveText);
					if(!Pattern.compile("(?i)[a-z]").matcher(sendText1).find()){
						if (sendText1 != null) {
							//循坏发送命令，根据三个端口来生成命令，先根据端口在数据库查出所有端口为01的电表地址，再查02、03
							try {
								if (!clientsMap.isEmpty()) {
									for (Map.Entry<String, SocketChannel> entry : clientsMap.entrySet()) {
										SocketChannel temp = entry.getValue();
										sBuffer.clear();

										for (int j = 0; j < sendText.length; j++) {
											sBuffer.put(SocketFormatTools.hexStringToBytes(sendText[j]));
											sBuffer.flip();
											temp.write(sBuffer);
											log.debug("发送的命令：" + sendText1);
											log.debug(sBuffer);
										}
									}
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				client = (SocketChannel) selectionKey.channel();
				client.register(selector, SelectionKey.OP_READ);
			}
		}
	}

	public void writeThread(){
		/**
		 *另起一个线程对除了登录和心跳以外的命令进行操作
		 */
		new Thread(){
			public void run(){
				while(true){
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					log.debug("2分钟过去了！");
					sendText = null;
					/*List<String[]> list = new Test().init();
						try {
							for (int i = 0; i < list.size(); i++) {
								sendText = list.get(i);
								if(!clientsMap.isEmpty()){
									for(Map.Entry<String, SocketChannel> entry : clientsMap.entrySet()){
										SocketChannel temp = entry.getValue();

										for (int j = 0; j < sendText.length; j++) {
											sBuffer.clear();
											sBuffer.put(SocketFormatTools.hexStringToBytes(sendText[j]));
											sBuffer.flip();
											temp.write(sBuffer);
											sleep(500);
											log.debug("发送的命令是："+sendText[j]);
											log.debug(sBuffer);
										}
									}
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/
					try {
						String[] b = new String[]{"210000119039"};
						sendText=new Send376Demo().getSendCommand("02010300","01","0417090000",b);
						//sendText=new Send376Demo().getAllCommand("0417090000");
						if (!clientsMap.isEmpty()){
							for (Map.Entry<String, SocketChannel> entry : clientsMap.entrySet()) {
								SocketChannel temp = entry.getValue();
								for (int j = 0; j < sendText.length; j++) {
									sBuffer.clear();
									sBuffer.put(SocketFormatTools.hexStringToBytes(sendText[j]));
									sBuffer.flip();
									temp.write(sBuffer);
									//sleep(500);
									log.debug("发送的命令是：" + sendText[j]);
									log.debug(sBuffer);
								}
							}
						}
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			}
		}.start();

	}

	public byte[] dataConver(ByteBuffer rBuffer) throws IOException {
		byte[] msg=new byte[3];
		for (int i = 0; i < msg.length; i++) {
			msg[i]=rBuffer.get();
		}
		int ms2=msg[2];
		if(ms2<0){
			ms2 += 256;
		}
		int ms1=msg[1];
		if(ms1<0){
			ms1 += 256;
		}
		int len= (ms2 << 8) + ms1;
		int length=len >> 2;
		byte[] msg2 = new byte[length+5];
		for (int i = 0; i < msg2.length; i++) {
			msg2[i]=rBuffer.get();
		}
		byte[] msgBack = new byte[msg.length+msg2.length];
		System.arraycopy(msg, 0, msgBack, 0, msg.length);
		System.arraycopy(msg2, 0, msgBack, msg.length, msg2.length);
		return msgBack;
	}
	public static void main(String[] args) throws IOException {
		new ServerSocket();
	}
}
