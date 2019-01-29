package indi.dipx.qhm.manager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Version 0.5
 */
public class QuestSheetManager {

	// 需求單編號
	private static String questionSheetNo = "CF1080123001";
	
	// 上傳(CVS)日期
	private static String cvsDate = "1080124";
	
	//CF專案目錄	
	private String PROJECT_PATH;	
	
	//GJZZ - G2G專案目錄	
	private String G2G_PROJECT_PATH;
	
	//31 SERVER 正式機檔案備份路徑
	private String RELEASE_CF;// = "\\\\172.21.1.31\\國貿局共用資料\\CF\\boft_cf";
	private String RELEASE_PW;
	private String RELEASE_SCHEDULER;
	private String RELEASE_G2G;

	
	//正式機Tomcat webapps路徑
	private String BOFT_WEB;	
	
	//正式機GJZZ - G2G路徑
	private String BOFT_G2G;

	
	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	private static ArrayList<String> pwSB = new ArrayList<String>();
	private static ArrayList<String> cfSB = new ArrayList<String>();
	private static ArrayList<String> schedulerSB = new ArrayList<String>();
	private static ArrayList<String> g2gSB = new ArrayList<String>();
	
	private static ArrayList<String> daoSB = new ArrayList<String>();

	private static int questionSheetId;
	private static Connection connection;
	private static StringBuilder tempSB = new StringBuilder(3000);

	private final static String ESCAPING_THE_BACKSLASH = "\\";
	private final static String FILE_EXTENSION_JAVA = ".java";
	private final static String FILE_EXTENSION_CLASS = ".class";
	private final static String FILE_EXTENSION_JRXML = ".jrxml";
	private final static String FILE_EXTENSION_JASPER = ".jasper";
	
	
	public QuestSheetManager(){		
		fieldDataBinging();
	}
	
	public static void main(String[] args) throws SQLException,
			ClassNotFoundException {

		try {
			System.out.println("Start...");
			
			connection = ConnectionObject.getInstance().getConnection();
			
			QuestSheetManager manager = new QuestSheetManager();
			
			manager.connNetworkFolder();
			manager.initQuestionSheetId();
			
			manager.parserPathTxt();
			manager.insertQuestSheet();			
			//manager.copyFile();
			
			System.out.println("Done !!");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	private void fieldDataBinging() {
		PROJECT_PATH = PropertiesManager.getSingletonInstance().getProjectPath();
		G2G_PROJECT_PATH = PropertiesManager.getSingletonInstance().getG2GProjectPath();
		
		RELEASE_CF = PropertiesManager.getSingletonInstance().getReleaseCF();
		RELEASE_PW = PropertiesManager.getSingletonInstance().getReleasePW();
		RELEASE_SCHEDULER = PropertiesManager.getSingletonInstance().getReleaseScheduler();
		RELEASE_G2G = PropertiesManager.getSingletonInstance().getReleaseG2G();

		BOFT_WEB = PropertiesManager.getSingletonInstance().getBoftWEB();		
		BOFT_G2G = PropertiesManager.getSingletonInstance().getBoftG2G();
	}


	/**
	 * 連線到31
	 */
	private void connNetworkFolder() {
		try {
			System.out.println("連線到31...");
			//String userName = PropertiesManager.getSingletonInstance().get31User();
			//String password = PropertiesManager.getSingletonInstance().get31Password();
			
			String command = "net use */delete /y";
			Process child = Runtime.getRuntime().exec(command);
			Thread.sleep(300);
			String command1 = "net.exe use F: \\\\172.21.1.31\\d$\\康大\\國貿局共用資料  /user:kduser !QAZ2wsx!QAZ2wsx";
		    Process child1 = Runtime.getRuntime().exec(command1);
		    Thread.sleep(500);
			
	        //String command = "C:\\Windows\\system32\\net.exe use " +RELEASE_CF+getServerDirectoryDate()+ " /user:" + userName + " " + password;
			//String command = "C:\\Windows\\system32\\net.exe use " +RELEASE_CF+ "/user:" + "kduser !QAZ2wsx!QAZ2wsx";
			//String command = "C:\\Windows\\system32\\net.exe use \\\\172.21.1.31\\國貿局共用資料(2018-03-21)\\CF\\boft_cf  /user:kduser !QAZ2wsx!QAZ2wsx";
	        //Process child = Runtime.getRuntime().exec(command);
	        
	       
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } catch (InterruptedException e) {			
			e.printStackTrace();
		}
		
	}

	/**
	 * 取消連線到31
	 */
	private void disConnNetworkFolder() {
		try {
			System.out.println("DisConnection Network Folder (31)...");
			String command = "net use */delete /y";
			Process child = Runtime.getRuntime().exec(command);
			
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } 		
	}
	
	private  void initQuestionSheetId() throws SQLException {
		//for icp
		//ICPconnection();
		
		Statement stmt = connection.createStatement();
		String sql = "select id from questionSheet where questionSheetNo = '"
		+ questionSheetNo + "'";

		ResultSet rs  = stmt.executeQuery(sql);
		while (rs.next()) {
			questionSheetId = rs.getInt("id");
		}
	}

	private  void parserPathTxt() throws IOException {
		System.out.println("分析上傳程式之檔案路徑(Path.txt)");
		File file = new File("path.txt");
		tempSB.append(FileUtils.readFileToString(file));

		ArrayList<String> tempCol = new ArrayList<String>();
		String temp[] = tempSB.toString().split("\\n");

		for (String s : temp) {
			tempCol.add(s);
		}

		for (String s2 : tempCol) {
			if (s2.startsWith("boft_cf")) {
				cfSB.add(s2);
			} else if (s2.startsWith("boft_pw")) {
				pwSB.add(s2);
			} else if (s2.startsWith("boft_scheduler")) {
				schedulerSB.add(s2);
			} else if (s2.startsWith("boft.cf.sql2java.dao")) {
				daoSB.add(s2);
			} else if (s2.startsWith("G2G")) {
				g2gSB.add(s2);
			}

		}
	}

	private  void insertQuestSheet() throws SQLException,
			ClassNotFoundException {
		PreparedStatement pstmt = null;

		System.out.println("寫入DB...");
		
		StringBuffer sql = null;

		try {

			sql = new StringBuffer("INSERT INTO codeList ");
			sql.append(" VALUES (?,?,?,?,?,?,?,?,?)");

			pstmt = connection.prepareStatement(sql.toString());
			
			insertDaoPath(pstmt);			
			insertPWPath(pstmt);
			insertCFPath(pstmt);
			insertSchedulerPath(pstmt);
			//insertG2GPath(pstmt);
		
			pstmt.executeBatch();

		} catch (InterruptedException e) {			
			e.printStackTrace();
		} finally {
			connection.close();
		}

	}

	private  void insertG2GPath(PreparedStatement pstmt) 
			throws SQLException, InterruptedException {
		String fileName = "";
		String path = "";
		String cvsNo = "";
		for (String s : g2gSB) {
			int index = s.lastIndexOf("/");
			int spIndex = s.lastIndexOf(" ");

			fileName = s.substring(index + 1, spIndex).trim();
			path = s.substring(0, index).replace('/', '\\');

			cvsNo = s.substring(spIndex + 1);

			pstmt.setInt(1, questionSheetId);
			pstmt.setString(2, fileName);
			pstmt.setString(3, cvsNo.trim().equals("1.1")? "":getServerFileLastModified(RELEASE_G2G, path ,fileName));
			pstmt.setString(4, cvsNo.trim().equals("1.1")? "":getServerFileLastModFileSize(RELEASE_G2G, path ,fileName));
			pstmt.setString(5, cvsNo.trim());
			pstmt.setString(6, getCommitFileSize(G2G_PROJECT_PATH, path ,fileName));
			pstmt.setString(7, cvsNo.trim().equals("1.1")? "0":"1");
			pstmt.setString(8, BOFT_G2G + path);
			pstmt.setString(9, cvsDate);
			pstmt.addBatch();
		}
	}

	private  void insertDaoPath(PreparedStatement pstmt)
			throws SQLException {
		String fileName = "";
		String path = "";
		String cvsNo = "";
		for (String s : daoSB) {
			int index = s.lastIndexOf("/");
			int spIndex = s.lastIndexOf(" ");

			fileName = s.substring(index + 1, spIndex).trim();
			path = s.substring(0, index).replace('/', '\\');

			cvsNo = s.substring(spIndex + 1);

			pstmt.setInt(1, questionSheetId);
			pstmt.setString(2, fileName);
			pstmt.setString(3, "");
			pstmt.setString(4, cvsNo.trim().equals("1.1")? "":getCommitFileSize(PROJECT_PATH, path ,fileName));
			pstmt.setString(5, cvsNo.trim());
			pstmt.setString(6, getCommitFileSize(PROJECT_PATH, path ,fileName));
			pstmt.setString(7, cvsNo.trim().equals("1.1")? "0":"1");
			pstmt.setString(8, BOFT_WEB + path);
			pstmt.setString(9, cvsDate);
			pstmt.addBatch();
		}

	}

	private  void insertSchedulerPath(PreparedStatement pstmt)
			throws SQLException, InterruptedException {
		String fileName = "";
		String path = "";
		String cvsNo = "";
		for (String s : schedulerSB) {
			int index = s.lastIndexOf("/");
			int spIndex = s.lastIndexOf(" ");

			fileName = s.substring(index + 1, spIndex).trim();
			path = s.substring(0, index).replace('/', '\\');

			cvsNo = s.substring(spIndex + 1);

			pstmt.setInt(1, questionSheetId);
			pstmt.setString(2, fileName);
			pstmt.setString(3, cvsNo.trim().equals("1.1")? "":getServerFileLastModified(RELEASE_SCHEDULER, path ,fileName));
			pstmt.setString(4, cvsNo.trim().equals("1.1")? "":getServerFileLastModFileSize(RELEASE_SCHEDULER, path ,fileName));
			pstmt.setString(5, cvsNo.trim());
			pstmt.setString(6, getCommitFileSize(PROJECT_PATH, path ,fileName));
			pstmt.setString(7, cvsNo.trim().equals("1.1")? "0":"1");
			pstmt.setString(8, BOFT_WEB + path);
			pstmt.setString(9, cvsDate);
			pstmt.addBatch();
		}

	}

	private  void insertPWPath(PreparedStatement pstmt)
			throws SQLException, InterruptedException {
		String fileName = "";
		String path = "";
		String cvsNo = "";
		for (String s : pwSB) {
			int index = s.lastIndexOf("/");
			int spIndex = s.lastIndexOf(" ");

			fileName = s.substring(index + 1, spIndex).trim();
			path = s.substring(0, index).replace('/', '\\');

			cvsNo = s.substring(spIndex + 1);

			pstmt.setInt(1, questionSheetId);
			pstmt.setString(2, fileName);
			pstmt.setString(3, cvsNo.trim().equals("1.1")? "":getServerFileLastModified(RELEASE_PW, path ,fileName));
			pstmt.setString(4, cvsNo.trim().equals("1.1")? "":getServerFileLastModFileSize(RELEASE_PW, path ,fileName));;
			pstmt.setString(5, cvsNo.trim());
			pstmt.setString(6, getCommitFileSize(PROJECT_PATH, path ,fileName));
			pstmt.setString(7, cvsNo.trim().equals("1.1")? "0":"1");
			pstmt.setString(8, BOFT_WEB + path);
			pstmt.setString(9, cvsDate);
			pstmt.addBatch();
		}

	}

	private  void insertCFPath(PreparedStatement pstmt)
			throws SQLException, InterruptedException {
		String fileName = "";
		String path = "";
		String cvsNo = "";
		for (String s : cfSB) {
			int index = s.lastIndexOf("/");
			int spIndex = s.lastIndexOf(" ");

			fileName = s.substring(index + 1, spIndex).trim();
			path = s.substring(0, index).replace('/', '\\');

			cvsNo = s.substring(spIndex + 1);

			pstmt.setInt(1, questionSheetId);
			pstmt.setString(2, fileName);
			pstmt.setString(3, cvsNo.trim().equals("1.1")? "":getServerFileLastModified(RELEASE_CF, path ,fileName));
			pstmt.setString(4, cvsNo.trim().equals("1.1")? "":getServerFileLastModFileSize(RELEASE_CF, path ,fileName));
			pstmt.setString(5, cvsNo.trim());
			pstmt.setString(6, getCommitFileSize(PROJECT_PATH, path ,fileName));
			pstmt.setString(7, cvsNo.trim().equals("1.1")? "0":"1");
			pstmt.setString(8, BOFT_WEB + path);
			pstmt.setString(9, cvsDate);
			pstmt.addBatch();
		}

	}

	private  void copyFile() {
		//copyPW
		StringBuilder fileName =  new StringBuilder();
		StringBuilder srcPath = new StringBuilder();
		StringBuilder classPath = new StringBuilder();
		StringBuilder cvsNo = new StringBuilder();
		for (String s : cfSB) {
			int index = s.lastIndexOf("/");
			int spIndex = s.lastIndexOf(" ");
			fileName.delete(0, fileName.length());
			srcPath.delete(0,srcPath.length());
			cvsNo.delete(0,cvsNo.length());
			classPath.delete(0,classPath.length());
			
			fileName.append(s.substring(index + 1, spIndex).trim());
			srcPath.append(s.substring(0, index).replace('/', '\\'));

			cvsNo.append(s.substring(spIndex + 1));
			try {
				// Project位置 ,  project之後
				// ex. boft_cf\WEB-INF\src\java\boft\cf\domain\cf3\pt316
				File source = new File("D:\\" + srcPath.toString() + ESCAPING_THE_BACKSLASH + fileName.toString()); // 
				
				//目地的
				File dest = new File("E:\\" + srcPath.toString() + ESCAPING_THE_BACKSLASH + fileName.toString());
				
				FileUtils.copyFile(source, dest);
				
				if (fileName.indexOf(".java") > 0) {

					fileName.replace(0, fileName.length(), StringUtils.replace(fileName.toString(), 
							FILE_EXTENSION_JAVA, FILE_EXTENSION_CLASS));
					
					classPath.append(srcPath.toString().replaceAll("src\\\\java", "classes"));				
					source = new File("D:\\" + classPath.toString() + ESCAPING_THE_BACKSLASH + fileName.toString()); // project之後
					dest = new File("E:\\" + classPath.toString() + ESCAPING_THE_BACKSLASH + fileName.toString());
					FileUtils.copyFile(source, dest);
				}else if (fileName.indexOf(".jrxml") > 0) {
					
					fileName.replace(0, fileName.length(), StringUtils.replace(fileName.toString(), 
							FILE_EXTENSION_JRXML, FILE_EXTENSION_JASPER));					
					classPath.append(srcPath.toString().replaceAll("src\\\\java", "classes"));				
					source = new File("D:\\" + classPath.toString() + ESCAPING_THE_BACKSLASH + fileName.toString()); // project之後
					dest = new File("E:\\" + classPath.toString() + ESCAPING_THE_BACKSLASH + fileName.toString());
					FileUtils.copyFile(source, dest);
				}
			
			  
			} catch (IOException e) {
			    e.printStackTrace();
			}
		}
	}	
	
	/**
	 * 上傳的檔案大小
	 * @param local
	 * @param path
	 * @param fileName
	 * @return
	 */
	private  String getCommitFileSize(String local, String path, String fileName) {		
		File file = new File(local+"\\"+path +"\\"+ fileName);
		DecimalFormat df=new DecimalFormat("#.##");
		return df.format(((float)file.length())/1000)+"kb";
	}
	

	/**
	 * 取得31 SERVER上檔案日期(修改前檔案日期)
	 * @param server
	 * @param path
	 * @param fileName
	 * @return
	 * @throws InterruptedException 
	 */
	private  String getServerFileLastModified(String server, String path, String fileName) 
			throws InterruptedException{
		Thread.sleep(50);
		int index = path.indexOf("\\");		
		String fixPath = path.substring(index);
		
		if (fixPath.indexOf("docs\\definition")!=-1 && server.indexOf("G2G") != -1){
			fixPath = fixPath.substring(5);
		}
	
		
		File file = new File(server+getServerDirectoryDate()+"\\"+fixPath +"\\"+ fileName);
		
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(file.lastModified());
		
		/*     
		System.out.println(" 上次修改時間為："
		            + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime()));
		*/
		
		StringBuilder sb = new StringBuilder();
		sb.append(sdf.format(file.lastModified()));
		
		String year = String.valueOf(Integer.parseInt(sb.substring(0,4))-1911);
		String month = sb.substring(4,6);
		String date = sb.substring(6,8);
		
		if (year.length() == 2){
			//year = "0"+year;
		}
	    return year+month+date;
	}
	

	/**
	 * 取得31 SERVER上檔案大小(修改前檔案大小)
	 * @param server	
	 * @param path
	 * @param fileName
	 * @return
	 * @throws InterruptedException 
	 */
	private  String getServerFileLastModFileSize(String server, String path, String fileName) 
			throws InterruptedException{
		Thread.sleep(50);
		int index = path.indexOf("\\");		
		String fixPath = path.substring(index);
		
		if (fixPath.indexOf("docs\\definition")!=-1 && server.indexOf("G2G") != -1){
			fixPath = fixPath.substring(5);
		}
		
		File file = new File(server+getServerDirectoryDate()+"\\"+fixPath +"\\"+ fileName);
		DecimalFormat df=new DecimalFormat("#.##");
		return df.format(((float)file.length())/1000)+"kb";
		
	}
	

	/**
	 * 取得31 SERVER上目錄名稱中的日期
	 */
	private  String getServerDirectoryDate(){
		Calendar cal  = Calendar.getInstance();
	    cal.add(Calendar.DATE, -1);

	    SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
	    return "("+s.format(new Date(cal.getTimeInMillis()))+")";
	}
	
}
