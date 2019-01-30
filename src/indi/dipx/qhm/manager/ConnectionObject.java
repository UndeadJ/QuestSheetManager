package indi.dipx.qhm.manager;
import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionObject {
	private final static String JDBC_CLASS = "net.sourceforge.jtds.jdbc.Driver";
	
	private  static Connection conn = null;
	
	public  Connection getConnection() {		
		if (null == conn){
			initConnection();
		}		
		return conn;
	}
	
	private  static void initConnection() {
		try {
			Class.forName(JDBC_CLASS);
			String url = PropertiesManager.getSingletonInstance().getDBUrl();
			String userName = PropertiesManager.getSingletonInstance().getDBUser();
			String password = PropertiesManager.getSingletonInstance().getDBPassword();			
			conn = DriverManager.getConnection(url, userName, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialization-on-demand holder
	 */
    private static class Holder {
         final static ConnectionObject instance = new ConnectionObject();
    }
    public static ConnectionObject getInstance() {
         return Holder.instance;
    }
}
