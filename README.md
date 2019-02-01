# QuestSheetManager
QuestSheetManager

【專案程式打包工具】

1.請依自身本機專案放置位置，修改 resource/config.properties 中之 PROJECT_PATH

2.依需求單修改QuestSheetManager.java 中的：
  // 需求單編號
  private String questionSheetNo = "CF1080123001";

  // 上傳(CVS)日期
  private String cvsDate = "1080124";	

3.如果只測試檔案copy，不做DB寫入，修改QuestSheetManager.java 中的：
  private static boolean onlyCopyFileTest = true;
	
