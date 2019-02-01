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
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Version 0.5
 */
public class QuestSheetManager {

	//是否只測試檔案copy，不做DB寫入
	private static boolean onlyCopyFileTest = true;
	
	// 需求單編號
	private String questionSheetNo = "CF1080123001";

	// 上傳(CVS)日期
	private String cvsDate = "1080124";	
	
	//檔案複製目的地位置 (磁碟機代號)
	private String DISK_DIRVER_FOR_COPY = "D:";
	
	//CF專案目錄	
	private static String PROJECT_PATH;	
	
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
	
	//檔案複製目的地位置
	private String copyDestination;

	private ArrayList<String> pwArrayList = new ArrayList<String>();
	private ArrayList<String> cfArrayList = new ArrayList<String>();
	private ArrayList<String> schedulerArrayList = new ArrayList<String>();
	private ArrayList<String> g2gArrayList = new ArrayList<String>();
	
	private ArrayList<String> daoArrayList = new ArrayList<String>();

	private int questionSheetId;	
	private StringBuilder tempSB = new StringBuilder(3000);

	private final static String ESCAPING_THE_BACKSLASH = "\\";
	private final static String FILE_EXTENSION_JAVA = ".java";
	private final static String FILE_EXTENSION_CLASS = ".class";
	private final static String FILE_EXTENSION_JRXML = ".jrxml";
	private final static String FILE_EXTENSION_JASPER = ".jasper";
	
	public QuestSheetManager(){}
	
	public static void main(String[] args) throws SQLException,
			ClassNotFoundException {

		try {
			System.out.println("Start...");
			
			QuestSheetManager manager = new QuestSheetManager();
			
			manager.initConfiguration();
			
			manager.initFileCopyDestination(null);
			
			//只測檔案複製
			if (onlyCopyFileTest) {							
				manager.parserPathTxt();
				manager.copyAllFile();				
			}else {
				manager.connNetworkFolder();				
				manager.loadQuestionSheetId();				
				manager.parserPathTxt();				
				manager.insertQuestSheet();				
				manager.copyAllFile();
			}	
			
			System.out.println("Done !!");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	

	/**
	 * 設定檔案複製目的地 ，將在目的地目錄下，建立需求單編號(已重組過)目錄
	 * @param copyDestPath 目的地目錄路徑 , null或空值，則使用預設值
	 */
	private void initFileCopyDestination(String copyDestPath) {
		StringBuilder destDirName = new StringBuilder();
		
		destDirName.append(getQuestionSheetNo().substring(2,9))
				   .append("-")
				   .append(Integer.parseInt(getQuestionSheetNo().substring(9)));
			
		String destPath = DISK_DIRVER_FOR_COPY;
		if (null!=copyDestPath && !"".equals(copyDestPath)) {			
			destPath = copyDestPath;
		}
		
		
		if (destPath.endsWith("\\")) {
			destPath = destPath + destDirName;
		}else {
			destPath = destPath + "\\" +destDirName;
		}
		
		this.setCopyDestination(destPath);
		
	}

	/**
	 * 	初始組態配置
	 */
	private void initConfiguration() {
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
			Runtime.getRuntime().exec(command);
			Thread.sleep(300);
			String command1 = "net.exe use F: \\\\172.21.1.31\\d$\\康大\\國貿局共用資料  /user:kduser !QAZ2wsx!QAZ2wsx";
		    Runtime.getRuntime().exec(command1);
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
	 * 	取得QuestionSheetId
	 * 	@throws SQLException
	 */
	private  void loadQuestionSheetId() throws SQLException {
		Statement stmt = getConnection().createStatement();
		String sql = "select id from questionSheet where questionSheetNo = '"
		+ questionSheetNo + "'";

		ResultSet rs  = stmt.executeQuery(sql);
		while (rs.next()) {
			questionSheetId = rs.getInt("id");
		}
	}
	

	/**
	 *	 分析上傳程式之檔案路徑 - Path.txt
	 * 	@throws IOException
	 */
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
				cfArrayList.add(s2);
			} else if (s2.startsWith("boft_pw")) {
				pwArrayList.add(s2);
			} else if (s2.startsWith("boft_scheduler")) {
				schedulerArrayList.add(s2);
			} else if (s2.startsWith("boft.cf.sql2java.dao")) {
				daoArrayList.add(s2);
			} else if (s2.startsWith("G2G")) {
				g2gArrayList.add(s2);
			}

		}
	}

	/**
	 * 	寫入codeList
	 * 	@throws SQLException
	 * 	@throws ClassNotFoundException
	 */
	private  void insertQuestSheet() throws SQLException,
			ClassNotFoundException {
		PreparedStatement pstmt = null;

		System.out.println("寫入DB...");
		
		StringBuffer sql = null;

		try {

			sql = new StringBuffer("INSERT INTO codeList ");
			sql.append(" VALUES (?,?,?,?,?,?,?,?,?)");

			pstmt = getConnection().prepareStatement(sql.toString());
			
			insertDaoPath(pstmt);			
			insertPWPath(pstmt);
			insertCFPath(pstmt);
			insertSchedulerPath(pstmt);
			//insertG2GPath(pstmt);
		
			pstmt.executeBatch();

		} catch (InterruptedException e) {			
			e.printStackTrace();
		} finally {
			getConnection().close();
		}

	}

	private  void insertG2GPath(PreparedStatement pstmt) 
			throws SQLException, InterruptedException {
		String fileName = "";
		String path = "";
		String cvsNo = "";
		for (String s : g2gArrayList) {
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
		for (String s : daoArrayList) {
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
		for (String s : schedulerArrayList) {
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
		for (String s : pwArrayList) {
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
		for (String s : cfArrayList) {
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

	/**
	 * 複製檔案
	 */
	private  void copyAllFile() {
		copyPwFile();
		copyCfFile();
		copySchedulerFile();
		//copyG2GFile();
	}	

	/**
	 * copy boft_scheduler File(need Refactor)
	 */
	private void copySchedulerFile() {
		StringBuilder fileName = new StringBuilder();
		StringBuilder srcPath = new StringBuilder();
		StringBuilder classPath = new StringBuilder();

		StringBuilder tempSb = new StringBuilder();

		for (String s : schedulerArrayList) {
			fileName.delete(0, fileName.length());
			srcPath.delete(0, srcPath.length());
			classPath.delete(0, classPath.length());

			int index = s.lastIndexOf("/");
			int spIndex = s.lastIndexOf(" ");

			fileName.append(s.substring(index + 1, spIndex).trim());
			srcPath.append(s.substring(0, index).replace('/', '\\'));

			try {
				File source = new File(PROJECT_PATH + ESCAPING_THE_BACKSLASH + srcPath.toString()
						+ ESCAPING_THE_BACKSLASH + fileName.toString());

				// 目地的
				File dest = new File(this.getCopyDestination() + ESCAPING_THE_BACKSLASH + srcPath.toString()
						+ ESCAPING_THE_BACKSLASH + fileName.toString());

				FileUtils.copyFile(source, dest);

				if ((fileName.indexOf(".jrxml") > 0)) {
					tempSb.delete(0, tempSb.length());
					tempSb.append(fileName);
					tempSb.replace(0, tempSb.length(),
							StringUtils.replace(tempSb.toString(), FILE_EXTENSION_JRXML, FILE_EXTENSION_JASPER));

					source = new File(PROJECT_PATH + ESCAPING_THE_BACKSLASH + srcPath.toString()
							+ ESCAPING_THE_BACKSLASH + tempSb.toString());
					dest = new File(this.getCopyDestination() + ESCAPING_THE_BACKSLASH + srcPath.toString()
							+ ESCAPING_THE_BACKSLASH + tempSb.toString());

					FileUtils.copyFile(source, dest);
				}

				if ((fileName.indexOf(".java") > 0) || (fileName.indexOf(".jrxml") > 0)) {
					// 換到classes目錄
					classPath.append(srcPath.toString().replaceAll("src\\\\java", "classes"));

					if (fileName.indexOf(".java") > 0) {
						fileName.replace(0, fileName.length(),
								StringUtils.replace(fileName.toString(), FILE_EXTENSION_JAVA, FILE_EXTENSION_CLASS));
					} else if (fileName.indexOf(".jrxml") > 0) {
						source = new File(PROJECT_PATH + ESCAPING_THE_BACKSLASH + classPath.toString()
								+ ESCAPING_THE_BACKSLASH + fileName.toString());
						dest = new File(this.getCopyDestination() + ESCAPING_THE_BACKSLASH + classPath.toString()
								+ ESCAPING_THE_BACKSLASH + fileName.toString());
						FileUtils.copyFile(source, dest);

						fileName.replace(0, fileName.length(),
								StringUtils.replace(fileName.toString(), FILE_EXTENSION_JRXML, FILE_EXTENSION_JASPER));
					}

					source = new File(PROJECT_PATH + ESCAPING_THE_BACKSLASH + classPath.toString()
							+ ESCAPING_THE_BACKSLASH + fileName.toString());
					dest = new File(this.getCopyDestination() + ESCAPING_THE_BACKSLASH + classPath.toString()
							+ ESCAPING_THE_BACKSLASH + fileName.toString());
					FileUtils.copyFile(source, dest);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * copy boft_cf File(need Refactor)
	 */
	private void copyCfFile() {
		StringBuilder fileName = new StringBuilder();
		StringBuilder srcPath = new StringBuilder();
		StringBuilder classPath = new StringBuilder();

		StringBuilder tempSb = new StringBuilder();

		for (String s : cfArrayList) {
			fileName.delete(0, fileName.length());
			srcPath.delete(0, srcPath.length());
			classPath.delete(0, classPath.length());

			int index = s.lastIndexOf("/");
			int spIndex = s.lastIndexOf(" ");

			fileName.append(s.substring(index + 1, spIndex).trim());
			srcPath.append(s.substring(0, index).replace('/', '\\'));

			try {
				File source = new File(PROJECT_PATH + ESCAPING_THE_BACKSLASH + srcPath.toString()
						+ ESCAPING_THE_BACKSLASH + fileName.toString());

				// 目地的
				File dest = new File(this.getCopyDestination() + ESCAPING_THE_BACKSLASH + srcPath.toString()
						+ ESCAPING_THE_BACKSLASH + fileName.toString());

				FileUtils.copyFile(source, dest);

				if ((fileName.indexOf(".jrxml") > 0)) {
					tempSb.delete(0, tempSb.length());
					tempSb.append(fileName);
					tempSb.replace(0, tempSb.length(),
							StringUtils.replace(tempSb.toString(), FILE_EXTENSION_JRXML, FILE_EXTENSION_JASPER));

					source = new File(PROJECT_PATH + ESCAPING_THE_BACKSLASH + srcPath.toString()
							+ ESCAPING_THE_BACKSLASH + tempSb.toString());
					dest = new File(this.getCopyDestination() + ESCAPING_THE_BACKSLASH + srcPath.toString()
							+ ESCAPING_THE_BACKSLASH + tempSb.toString());

					FileUtils.copyFile(source, dest);
				}

				if ((fileName.indexOf(".java") > 0) || (fileName.indexOf(".jrxml") > 0)) {
					// 換到classes目錄
					classPath.append(srcPath.toString().replaceAll("src\\\\java", "classes"));

					if (fileName.indexOf(".java") > 0) {
						fileName.replace(0, fileName.length(),
								StringUtils.replace(fileName.toString(), FILE_EXTENSION_JAVA, FILE_EXTENSION_CLASS));
					} else if (fileName.indexOf(".jrxml") > 0) {
						source = new File(PROJECT_PATH + ESCAPING_THE_BACKSLASH + classPath.toString()
								+ ESCAPING_THE_BACKSLASH + fileName.toString());
						dest = new File(this.getCopyDestination() + ESCAPING_THE_BACKSLASH + classPath.toString()
								+ ESCAPING_THE_BACKSLASH + fileName.toString());
						FileUtils.copyFile(source, dest);

						fileName.replace(0, fileName.length(),
								StringUtils.replace(fileName.toString(), FILE_EXTENSION_JRXML, FILE_EXTENSION_JASPER));
					}

					source = new File(PROJECT_PATH + ESCAPING_THE_BACKSLASH + classPath.toString()
							+ ESCAPING_THE_BACKSLASH + fileName.toString());
					dest = new File(this.getCopyDestination() + ESCAPING_THE_BACKSLASH + classPath.toString()
							+ ESCAPING_THE_BACKSLASH + fileName.toString());
					FileUtils.copyFile(source, dest);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * copy boft_pw File(need Refactor)
	 */
	private void copyPwFile() {
		StringBuilder fileName = new StringBuilder();
		StringBuilder srcPath = new StringBuilder();
		StringBuilder classPath = new StringBuilder();

		StringBuilder tempSb = new StringBuilder();

		for (String s : pwArrayList) {
			fileName.delete(0, fileName.length());
			srcPath.delete(0, srcPath.length());
			classPath.delete(0, classPath.length());

			int index = s.lastIndexOf("/");
			int spIndex = s.lastIndexOf(" ");

			fileName.append(s.substring(index + 1, spIndex).trim());
			srcPath.append(s.substring(0, index).replace('/', '\\'));

			try {
				File source = new File(PROJECT_PATH + ESCAPING_THE_BACKSLASH + srcPath.toString()
						+ ESCAPING_THE_BACKSLASH + fileName.toString());

				// 目地的
				File dest = new File(this.getCopyDestination() + ESCAPING_THE_BACKSLASH + srcPath.toString()
						+ ESCAPING_THE_BACKSLASH + fileName.toString());

				FileUtils.copyFile(source, dest);

				if ((fileName.indexOf(".jrxml") > 0)) {
					tempSb.delete(0, tempSb.length());
					tempSb.append(fileName);
					tempSb.replace(0, tempSb.length(),
							StringUtils.replace(tempSb.toString(), FILE_EXTENSION_JRXML, FILE_EXTENSION_JASPER));

					source = new File(PROJECT_PATH + ESCAPING_THE_BACKSLASH + srcPath.toString()
							+ ESCAPING_THE_BACKSLASH + tempSb.toString());
					dest = new File(this.getCopyDestination() + ESCAPING_THE_BACKSLASH + srcPath.toString()
							+ ESCAPING_THE_BACKSLASH + tempSb.toString());

					FileUtils.copyFile(source, dest);
				}

				if ((fileName.indexOf(".java") > 0) || (fileName.indexOf(".jrxml") > 0)) {
					// 換到classes目錄
					classPath.append(srcPath.toString().replaceAll("src\\\\java", "classes"));

					if (fileName.indexOf(".java") > 0) {
						fileName.replace(0, fileName.length(),
								StringUtils.replace(fileName.toString(), FILE_EXTENSION_JAVA, FILE_EXTENSION_CLASS));
					} else if (fileName.indexOf(".jrxml") > 0) {
						source = new File(PROJECT_PATH + ESCAPING_THE_BACKSLASH + classPath.toString()
								+ ESCAPING_THE_BACKSLASH + fileName.toString());
						dest = new File(this.getCopyDestination() + ESCAPING_THE_BACKSLASH + classPath.toString()
								+ ESCAPING_THE_BACKSLASH + fileName.toString());
						FileUtils.copyFile(source, dest);

						fileName.replace(0, fileName.length(),
								StringUtils.replace(fileName.toString(), FILE_EXTENSION_JRXML, FILE_EXTENSION_JASPER));
					}

					source = new File(PROJECT_PATH + ESCAPING_THE_BACKSLASH + classPath.toString()
							+ ESCAPING_THE_BACKSLASH + fileName.toString());
					dest = new File(this.getCopyDestination() + ESCAPING_THE_BACKSLASH + classPath.toString()
							+ ESCAPING_THE_BACKSLASH + fileName.toString());
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
		File file = new File(local+ESCAPING_THE_BACKSLASH+path+ESCAPING_THE_BACKSLASH+fileName);
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
		int index = path.indexOf(ESCAPING_THE_BACKSLASH);		
		String fixPath = path.substring(index);
		
		if (fixPath.indexOf("docs\\definition")!=-1 && server.indexOf("G2G") != -1){
			fixPath = fixPath.substring(5);
		}
		File file = new File(server+getServerDirectoryDate()+ESCAPING_THE_BACKSLASH+fixPath +ESCAPING_THE_BACKSLASH+ fileName);
		
		/*
		 * Calendar c = Calendar.getInstance(); c.setTimeInMillis(file.lastModified());
		 * System.out.println(" 上次修改時間為：" + new
		 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime()));
		 */
		
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
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
		int index = path.indexOf(ESCAPING_THE_BACKSLASH);		
		String fixPath = path.substring(index);
		
		if (fixPath.indexOf("docs\\definition")!=-1 && server.indexOf("G2G") != -1){
			fixPath = fixPath.substring(5);
		}
		
		File file = new File(server+getServerDirectoryDate()+ESCAPING_THE_BACKSLASH+fixPath +ESCAPING_THE_BACKSLASH+ fileName);
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
	
	
	/**
	 * Connection
	 * @return
	 */
	private Connection getConnection() {		
		return ConnectionObject.getInstance().getConnection();
	}	
	
	private String getQuestionSheetNo() {
		return questionSheetNo;
	}
	
	private String getCopyDestination() {
		return copyDestination;
	}

	private void setCopyDestination(String copyDestination) {
		this.copyDestination = copyDestination;
	}
}
