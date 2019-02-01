# QuestSheetManager 【專案程式打包工具】
#### 需先於【問題單管理系統】建立需求單，才可使用

1.請依自身本機專案放置位置，修改 <strong>resource/config.properties</strong> 中之 <strong>PROJECT_PATH</strong> <br />

2.依需求單修改<strong>QuestSheetManager</strong> 中的： <br />
<pre><code>// 需求單編號 <br />
private String questionSheetNo = "CF1080123001"; <br /> <br />

// 上傳(CVS)日期 <br />
private String cvsDate = "1080124"; </pre></code>

3.如果只測試檔案copy，不做DB寫入，請修改<strong>QuestSheetManager</strong> 中的： <br />
  <pre><code>private static boolean onlyCopyFileTest = true; </pre></code><br />
	
