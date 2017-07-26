package com.visenergy.socketCommunication;

/**
 * @author WuSong
 * @create 2017-05-25 10:36
 */
public abstract class SocketFormatTools {

    static final char[] HEX_CHAR_TABLE = {
            '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    public static String toHexString(byte[] data){
        if(data == null || data.length == 0)
            return null;
        byte[] hex = new byte[data.length * 2];
        int index = 0;
        for(byte b : data){
            int v = b & 0xFF;
            hex[index++] = (byte) HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = (byte) HEX_CHAR_TABLE[v & 0xF];
        }
        return new String(hex);
    }

    public static byte charToByte(char c){
        return (byte)"0123456789ABCDEF".indexOf(c);
    }

    public static byte[] hexStringToBytes(String data){
        if(data == null || "".equals(data))
            return null;
        data = data.toUpperCase();
        int length = data.length()/2;
        char[] dataChars = data.toCharArray();
        byte[] byteData = new byte[length];
        for (int i = 0;i<length;i++){
            int pos = i * 2;
            byteData[i] = (byte)(charToByte(dataChars[pos]) << 4 | charToByte(dataChars[pos + 1]));
        }
        return byteData;
    }
}

