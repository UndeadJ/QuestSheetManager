package indi.dipx.qhm.util;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;


public class DecodeUtil {
	private final static Base64 base64 = new Base64();
	
    private static class SingletonHolder{
         public static DecodeUtil singletonInstance = new DecodeUtil();
    }
    
    private DecodeUtil() {}
    
    public static DecodeUtil getSingletonInstance() {
        return SingletonHolder.singletonInstance;
    }
	
	
	
	public  String getDecodeText(String encodeString) throws UnsupportedEncodingException{
		return new String(base64.decode(encodeString), "UTF-8");
	}
	
	
	
	public  String getEncodedText(String text) throws UnsupportedEncodingException{
		final byte[] textByte = text.getBytes("UTF-8");
		return base64.encodeToString(textByte);
	}
	

	
	public static void main(String[] args) throws UnsupportedEncodingException {
		
		final String text = "!QAZ2wsx!QAZ2wsx"; //for test
				
		System.out.println(DecodeUtil.getSingletonInstance().getEncodedText(text));		
		
		System.out.println(DecodeUtil.getSingletonInstance().
				getDecodeText(DecodeUtil.getSingletonInstance().getEncodedText(text)));	

	}

	
}
