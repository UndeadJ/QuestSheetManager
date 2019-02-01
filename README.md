# QuestSheetManager 【專案程式打包工具】
#### 請需先於【問題單管理系統】建立需求單

1.請依自身本機專案放置位置，修改 <strong>resource/config.properties</strong> 中之 <strong>PROJECT_PATH</strong> <br />

2.依需求單修改<strong>QuestSheetManager</strong> 中的： <br />
<pre><code>// 需求單編號 <br />
private String questionSheetNo = "CF1080123001"; <br /> <br />

// 上傳(CVS)日期 <br />
private String cvsDate = "1080124"; </pre></code>

3.如果只測試檔案copy，不做DB寫入，請修改<strong>QuestSheetManager</strong> 中的： <br />
  <pre><code>private static boolean onlyCopyFileTest = true; </pre></code><br />
	
***
##### 運作方式
程式<strong>commint</strong>後，利用<strong>change-log</strong>取得檔案路徑及版號<br/>
貼到<strong>path.txt</strong>檔後，確認好<strong>QuestSheetManager</strong>及<strong>config.properties</strong>內之設定<br/>
執行即可
