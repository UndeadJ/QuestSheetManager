package indi.dipx.qhm.manager;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import indi.dipx.qhm.util.DecodeUtil;


/*
 * Properties Manager (resource/config.properties)
 * 
 *  need rebuild(clean...) project if "config.properties" have change !!
 *  
 */
public class PropertiesManager {
	
	private final static Properties prop = new Properties();

	private PropertiesManager() {
		try {
			prop.load(PropertiesManager.class
					.getResourceAsStream("..\\..\\..\\..\\resources\\config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	public String getProjectPath()  {
		return 	prop.getProperty("PROJECT_PATH");
	}
	
	public String getG2GProjectPath()  {
		return 	prop.getProperty("G2G_PROJECT_PATH");
	}
	
	public String getReleaseCF()  {
		return 	prop.getProperty("RELEASE_CF");
	}
	
	public String getReleasePW()  {
		return 	prop.getProperty("RELEASE_PW");
	}
	
	public String getReleaseScheduler()  {
		return 	prop.getProperty("RELEASE_SCHEDULER");
	}
	
	public String getReleaseG2G()  {
		return 	prop.getProperty("RELEASE_G2G");
	}
	
	public String getBoftWEB()  {
		return 	prop.getProperty("BOFT_WEB");
	}
	
	public String getBoftG2G()  {
		return 	prop.getProperty("BOFT_G2G");
	}
	
	public String getDBUser()  {
		return 	prop.getProperty("DB_USER");
	}

	public String getDBPassword() throws UnsupportedEncodingException {
		return DecodeUtil.getSingletonInstance().getDecodeText(
				prop.getProperty("DB_PASSWORD"));
	}

	public  String getDBUrl()  {
		return 	prop.getProperty("DB_URL");
	}

	public  String get31User() {
		return prop.getProperty("31_USER");
	}

	public  String get31Password() throws UnsupportedEncodingException {
		return DecodeUtil.getSingletonInstance().getDecodeText(
				prop.getProperty("31_PASSWORD"));
	}
	

	
    private static class SingletonHolder{
         public static PropertiesManager singletonInstance = new PropertiesManager();
    }
    
    public static PropertiesManager getSingletonInstance() {
        return SingletonHolder.singletonInstance;
    }
    
	public static void main(String[] args) {
		try {	
			prop.load(PropertiesManager.class
					.getResourceAsStream("..\\..\\..\\..\\resources\\config.properties"));
			/// get the property value and print it out
			/*
			 * System.out.println(prop.getProperty("DB_URL"));
			 * System.out.println(prop.getProperty("DB_USER"));
			 * System.out.println(prop.getProperty("31_PASSWORD"));
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
}
