package com.loongtao.mediasearch.remotesearchagent;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import java.util.Date;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.loongtao.mediasearch.mediasearchinterface.*;



public class RemoteSearchAgent implements ISearchAgent{

    
    private static boolean logInited = false;
    private static final  Logger log = Logger.getLogger(RemoteSearchAgent.class);

    private boolean inited = false; 
    Registry  registry  = null;
  
    public String  [] ips= {"192.168.18.203","192.168.18.75"};  //"192.168.18.109",,"121.52.217.96" 121.52.217.94
    public String  [] ports= {"8889","8889"};
    
    public List<ISearchAgent> stubs = new ArrayList<ISearchAgent>();
    private int curStub = 0;
    
	//private ExecutorService  connectExec = Executors.newCachedThreadPool();
	
    public void init() throws Exception {  
         try {   
            // 初始化 
        	if (!logInited)
        	{
	        	SimpleLayout layout = new SimpleLayout();
	        	//layout.
	        	FileAppender appender = null;
	        	try {
	        	appender = new FileAppender(layout,"remoteSearchAgent.log",true);
	        	} 
	        	catch(Exception e) {
	        		throw e;
	        	}
	        	log.addAppender(appender);
	        	log.setLevel((Level) Level.DEBUG);
				log.addAppender(appender);
				logInited = true;
        	}
        	//stub = (ISearchAgent) Naming.lookup("rmi://" + ip    + ":" + port + "/comm");  
        	int i = 0;
        	
        	for (i = 0; i< ips.length; i++)
        	{
        		connect(i);
        	}
        	//stub = (ISearchAgent) Naming.lookup(address);  
        	inited = true;
  
        } catch (NotBoundException e) {   
            log.warn(GetCurrentDateStr() + e.getMessage());   
        } catch (MalformedURLException e) {   
            log.warn(GetCurrentDateStr() + e.getMessage());   
        } catch (RemoteException e) {   
            log.warn(GetCurrentDateStr() + e.getMessage());   
        }   
          
    } 
    
    public RemoteSearchAgent() {
    	try
    	{
    		init();
    		log.info("In Constructor  init finished with server = " + ips[0].toString() );
    	}
    	catch (Exception ex)
    	{
    		//throw ex;
    	}
    }
    
    private void connect(int curStub)
    {
		try
		{ 
			String address =  "rmi://" + ips[curStub] + ":" + ports[curStub] + "/comm";
			log.info(GetCurrentDateStr() + "try to connect " + address);
			stubs.add((ISearchAgent) Naming.lookup(address));  
		}
		catch (NotBoundException e) {   
            log.warn(GetCurrentDateStr() + e.getMessage());   
        } catch (MalformedURLException e) {   
            log.warn(GetCurrentDateStr() + e.getMessage());   
        } catch (RemoteException e) {   
            log.warn(GetCurrentDateStr() + e.getMessage());   
        } 
        
   	
    }
    

    private synchronized void reConnect(int curStub)
    {
		try
		{ 
			String address =  "rmi://" + ips[curStub] + ":" + ports[curStub] + "/comm";
			log.info("try to reConnect " + address);
			stubs.set(curStub,(ISearchAgent) Naming.lookup(address));  
		}
		catch (NotBoundException e) {   
            log.warn(GetCurrentDateStr() + e.getMessage());   
        } catch (MalformedURLException e) {   
            log.warn(GetCurrentDateStr() + e.getMessage());   
        } catch (RemoteException e) {   
            log.warn(GetCurrentDateStr() + e.getMessage());   
        }   
     }

      
    private String GetCurrentDateStr()
    {
	    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SS");
	    Date date = new Date();
	    return dateFormat.format(date)+" ";
   	
    }
   

	@Override
	public List<TopTheme> getTopThemesList(String searchFields,
			String filterFields, String sortFields) throws RemoteException {
		List<TopTheme> topThemeList = null;
		// 用于首页的调用
		 try
		 {
			 synchronized(this){
			 if (!inited) init();
			 }
             long start = System.currentTimeMillis();
	            while (curStub<stubs.size())
	            {
		            try
		            {
					     if (stubs.get(curStub) != null)   // 上一次正常的连接
					     {
				             try
				             {
				            	 topThemeList = stubs.get(curStub).getTopThemesList(searchFields, filterFields, sortFields);
				             }
				             catch (ConnectException e) // 上一次正常的连接中断了
				             {
				            	 // 重新尝试一下，是否可以重新连接。如果可以，返回正常结果，否则产生一个连接中断
						    	 reConnect(curStub);
						    	 if (stubs.get(curStub) != null) topThemeList = stubs.get(curStub).getTopThemesList(searchFields, filterFields, sortFields);
				             }
					     }
					     else // 上一次不正常的连接重试一次，有可能已经恢复
					     {
					    	 reConnect(curStub);
					    	 if (stubs.get(curStub) != null) topThemeList = stubs.get(curStub).getTopThemesList(searchFields, filterFields, sortFields);
					     }
		            }
		            catch (ConnectException e) // 上次中断的/新的中断尝试重连后还是中断状态
		            {
		              	 log.warn(GetCurrentDateStr() + e.getMessage());
		              	 stubs.set(curStub,null);
		            }
		            // 正常获得结果的返回
				    if (topThemeList != null) 	break;
				    // 尝试下一个索引服务器
					++curStub;
	            }
             long elapse = System.currentTimeMillis() - start;
			 if (curStub<stubs.size()) log.info(GetCurrentDateStr() + "getTopThemesList  From " + ips[curStub] +  " searchFields = " + searchFields + " filterFields = " 
					 + filterFields + " sortFields = " + sortFields +  " took_time = " + elapse + " ms");
			 else
				 log.info(GetCurrentDateStr() + "getTopThemesList failed searchFields = " + searchFields + " filterFields = " 
						 + filterFields + " sortFields = " + sortFields +  " took_time = " + elapse + " ms");
		 }
		 catch (Exception ex)
		 {
	           log.warn(GetCurrentDateStr() + ex);   
		 }
		curStub = 0;
		return topThemeList;
	}


	/***
	 *  获取文章列表
	 */
	public ThemeArticleList getArticleList(String searchFields,String filterFields,String sortFields,PageInfo pageInfo)
	{
		ThemeArticleList themeArtibleList = null;
		 try
		 {
			    synchronized(this){
				 if (!inited) init();
				 }
	            long start = System.currentTimeMillis();
	            while (curStub<stubs.size())
	            {
		            try
		            {
					     if (stubs.get(curStub) != null)   
					     {
				             try
				             {
				            	 themeArtibleList = stubs.get(curStub).getArticleList(searchFields, filterFields, sortFields,pageInfo);
				             }
				             catch (ConnectException e) // 上一次正常的连接中断了
				             {
				            	 // 重新尝试一下，是否可以重新连接。如果可以，返回正常结果，否则产生一个连接中断
						    	 reConnect(curStub);
						    	 if (stubs.get(curStub) != null) themeArtibleList = stubs.get(curStub).getArticleList(searchFields, filterFields, sortFields,pageInfo);
				             }
					     }
					     else // 上一次不正常的连接重试一次，有可能已经恢复
					     {
					    	 reConnect(curStub);
					    	 if (stubs.get(curStub) != null) themeArtibleList = stubs.get(curStub).getArticleList(searchFields, filterFields, sortFields,pageInfo);
					     }
		            }
		            catch (ConnectException e) // 上次中断的/新的中断尝试重连后还是中断状态
		            {
		              	 log.warn(GetCurrentDateStr() + e.getMessage());
		              	 stubs.set(curStub,null);
		            }
		            // 正常获得结果的返回
				    if (themeArtibleList != null)	break;
				    // 尝试下一个索引服务器
					++curStub;
	            }
	            long elapse = System.currentTimeMillis() - start;
				if (curStub<stubs.size()) log.info(GetCurrentDateStr() + "getArticleList From " + ips[curStub] +  " searchFields = " + searchFields + " filterFields = " 
						 + filterFields + " sortFields = " + sortFields +  " took_time = " + elapse + " ms");
				else 
					 log.info(GetCurrentDateStr() + "getArticleList failed searchFields = " + searchFields + " filterFields = " 
							 + filterFields + " sortFields = " + sortFields +  " took_time = " + elapse + " ms");
		 }
		 catch (Exception ex)
		 {
	           log.warn(GetCurrentDateStr() + ex);   
		 }
		curStub = 0;
		return themeArtibleList;
	}
	
	
	public ThemeCountByField getThemeCount(String searchFields,String filterFields,String groupFields)
	{
		ThemeCountByField themeCountByTime = null;
		// 针对单个主题统计，如时间分布，媒体分布
		 try
		 {
			 synchronized(this){
				 if (!inited) init();
			 }
	         long start = System.currentTimeMillis();
	            while (curStub<stubs.size())
	            {
		            try
		            {
					     if (stubs.get(curStub) != null)   
					     {
				             try
				             {
				            	 themeCountByTime = stubs.get(curStub).getThemeCount(searchFields, filterFields, groupFields);
				             }
				             catch (ConnectException e) // 上一次正常的连接中断了
				             {
				            	 // 重新尝试一下，是否可以重新连接。如果可以，返回正常结果，否则产生一个连接中断
						    	 reConnect(curStub);
						    	 if (stubs.get(curStub) != null) themeCountByTime = stubs.get(curStub).getThemeCount(searchFields, filterFields, groupFields);
				             }
					     }
					     else // 上一次不正常的连接重试一次，有可能已经恢复
					     {
					    	 reConnect(curStub);
					    	 if (stubs.get(curStub) != null) themeCountByTime = stubs.get(curStub).getThemeCount(searchFields, filterFields, groupFields);
					     }
		            }
		            catch (ConnectException e) // 上次中断的/新的中断尝试重连后还是中断状态
		            {
		              	 log.warn(GetCurrentDateStr() + e.getMessage());
		              	 stubs.set(curStub,null);
		            }
		            // 正常获得结果的返回
				    if (themeCountByTime != null)	break;
				    // 尝试下一个索引服务器
					++curStub;
	            }
             long elapse = System.currentTimeMillis() - start;
			 if (curStub<stubs.size()) log.info(GetCurrentDateStr() + "getThemeCount From " + ips[curStub] +  " searchFields = " + searchFields + " filterFields = " 
					 + filterFields + " sortFields = " + groupFields +  " took_time = " + elapse + " ms");
			    else
			    	log.info(GetCurrentDateStr() + "getThemeCount failed with " + " searchFields = " + searchFields + " filterFields = " 
							 + filterFields + " sortFields = " + groupFields +  " took_time = " + elapse + " ms");
		 }
		 catch (Exception ex)
		 {
	           log.warn(GetCurrentDateStr() + ex);   
		 }
		 curStub = 0;
		return themeCountByTime;
	}
	
	@Override
	public List<MultiCountResult> getMulitMediaCount(String searchFields,
			String filterFields, String groupFields) throws RemoteException {
		// TODO Auto-generated method stub
		List<MultiCountResult> multiMediaCount = null;
		// 
		 try
		 {
			 synchronized(this){
				 if (!inited) init();
				 }
	         long start = System.currentTimeMillis();
	            while (curStub<stubs.size())
	            {
		            try
		            {
					     if (stubs.get(curStub) != null)   
					     {
				             try
				             {
				            	 multiMediaCount = stubs.get(curStub).getMulitMediaCount(searchFields, filterFields, groupFields);
				             }
				             catch (ConnectException e) // 上一次正常的连接中断了
				             {
				            	 // 重新尝试一下，是否可以重新连接。如果可以，返回正常结果，否则产生一个连接中断
						    	 reConnect(curStub);
						    	 if (stubs.get(curStub) != null) multiMediaCount = stubs.get(curStub).getMulitMediaCount(searchFields, filterFields, groupFields);
				             }
					     }
					     else // 上一次不正常的连接重试一次，有可能已经恢复
					     {
					    	 reConnect(curStub);
					    	 if (stubs.get(curStub) != null) multiMediaCount = stubs.get(curStub).getMulitMediaCount(searchFields, filterFields, groupFields);
					     }
		            }
		            catch (ConnectException e) // 上次中断的/新的中断尝试重连后还是中断状态
		            {
		              	 log.warn(GetCurrentDateStr() + e.getMessage());
		              	 stubs.set(curStub,null);
		            }
		            // 正常获得结果的返回
				    if (multiMediaCount != null)	break;
				    // 尝试下一个索引服务器
					++curStub;
	            }
             long elapse = System.currentTimeMillis() - start;
			 if (curStub<stubs.size()) log.info(GetCurrentDateStr() + "getFocusedMediaCount From " + ips[curStub] +  "  searchFields = %s" + searchFields + " filterFields = " 
					 + filterFields + " sortFields = " + groupFields +  " took_time = " + elapse + " ms");
			 else
				 log.info(GetCurrentDateStr() + "getFocusedMediaCount failed searchFields = %s" + searchFields + " filterFields = " 
						 + filterFields + " sortFields = " + groupFields +  " took_time = " + elapse + " ms"); 
		 }
		 catch (Exception ex)
		 {
	           log.warn(GetCurrentDateStr() + ex);   
		 }
		 curStub = 0;
		return multiMediaCount;
	}
	
	
	//private 

	
	@Deprecated
	public List<ArticleCountByTheme> getFocusedMediaCount(String searchFields,String filterFields,String groupFields)
	{
		List<ArticleCountByTheme> articleCountByThemeList = null;
		// 
		 try
		 {
			 synchronized(this){
				 if (!inited) init();
				 }
	         long start = System.currentTimeMillis();
	            while (curStub<stubs.size())
	            {
		            try
		            {
					     if (stubs.get(curStub) != null)   
					     {
				             try
				             {
				            	 articleCountByThemeList = stubs.get(curStub).getFocusedMediaCount(searchFields, filterFields, groupFields);
				             }
				             catch (ConnectException e) // 上一次正常的连接中断了
				             {
				            	 // 重新尝试一下，是否可以重新连接。如果可以，返回正常结果，否则产生一个连接中断
						    	 reConnect(curStub);
						    	 if (stubs.get(curStub) != null) articleCountByThemeList = stubs.get(curStub).getFocusedMediaCount(searchFields, filterFields, groupFields);
				             }
					     }
					     else // 上一次不正常的连接重试一次，有可能已经恢复
					     {
					    	 reConnect(curStub);
					    	 if (stubs.get(curStub) != null) articleCountByThemeList = stubs.get(curStub).getFocusedMediaCount(searchFields, filterFields, groupFields);
					     }
		            }
		            catch (ConnectException e) // 上次中断的/新的中断尝试重连后还是中断状态
		            {
		              	 log.warn(GetCurrentDateStr() + e.getMessage());
		              	 stubs.set(curStub,null);
		            }
		            // 正常获得结果的返回
				    if (articleCountByThemeList != null)	break;
				    // 尝试下一个索引服务器
					++curStub;
	            }
             long elapse = System.currentTimeMillis() - start;
			 if (curStub<stubs.size()) log.info(GetCurrentDateStr() + "getFocusedMediaCount From " + ips[curStub] +  "  searchFields = %s" + searchFields + " filterFields = " 
					 + filterFields + " sortFields = " + groupFields +  " took_time = " + elapse + " ms");
			 else
				 log.info(GetCurrentDateStr() + "getFocusedMediaCount failed searchFields = %s" + searchFields + " filterFields = " 
						 + filterFields + " sortFields = " + groupFields +  " took_time = " + elapse + " ms"); 
		 }
		 catch (Exception ex)
		 {
	           log.warn(GetCurrentDateStr() + ex);   
		 }
		 curStub = 0;
		return articleCountByThemeList;
	}

	
	public List<ThemeCountByField> getMultiThemeCount(
			String searchFields, String filterFields, String groupFields)
	 {
		List<ThemeCountByField> articleCountByThemeList = null;
		// 可以对多个主题，多个情感，进行时间分布方面的统计
		 try
		 {
			 synchronized(this){
				 if (!inited) init();
				 }
	         long start = System.currentTimeMillis();
	            while (curStub<stubs.size())
	            {
		            try
		            {
					     if (stubs.get(curStub) != null)   
					     {
				             try
				             {
				            	 articleCountByThemeList = stubs.get(curStub).getMultiThemeCount(searchFields, filterFields, groupFields);
				             }
				             catch (ConnectException e) // 上一次正常的连接中断了
				             {
				            	 // 重新尝试一下，是否可以重新连接。如果可以，返回正常结果，否则产生一个连接中断
						    	 reConnect(curStub);
						    	 if (stubs.get(curStub) != null) articleCountByThemeList = stubs.get(curStub).getMultiThemeCount(searchFields, filterFields, groupFields);
				             }
					     }
					     else  // 上一次不正常的连接重试一次，有可能已经恢复
					     {
					    	 reConnect(curStub);
					    	 if (stubs.get(curStub) != null) articleCountByThemeList = stubs.get(curStub).getMultiThemeCount(searchFields, filterFields, groupFields);
					     }
		            }
		            catch (ConnectException e) // 上次中断的/新的中断尝试重连后还是中断状态
		            {
		              	 log.warn(GetCurrentDateStr() + e.getMessage());
		              	 stubs.set(curStub,null);
		            }
		            // 正常获得结果的返回
				    if (articleCountByThemeList != null)	break;
				    // 尝试下一个索引服务器
					++curStub;
	            }
             long elapse = System.currentTimeMillis() - start;
			 if (curStub<stubs.size()) log.info(GetCurrentDateStr() + "getMultiThemeCount From " + ips[curStub] +  "  searchFields = " + searchFields + " filterFields = " 
					 + filterFields + " groupFields = " + groupFields +  " took_time = " + elapse + " ms");
			 else 
				 log.info(GetCurrentDateStr() + "getMultiThemeCount failed searchFields = " + searchFields + " filterFields = " 
					 + filterFields + " groupFields = " + groupFields +  " took_time = " + elapse + " ms");
				 
			 //curStub = (++curStub)%stubs.size();
		 }
		 catch (Exception ex)
		 {
	           log.warn(GetCurrentDateStr() + ex);   
		 }
		 curStub = 0;
		return articleCountByThemeList;
	}

	/**
	 * 对主题进行增加，修改，删除 
	 * 
	 */
	public int themeOperate(String themeID, String opType)
	{
		// 对主题进行增加，修改，删除 
		int nResult = 0;
		 try
		 {
			 synchronized(this){
				 if (!inited) init();
				 }
	         long start = System.currentTimeMillis();
	         for (int i = 0; i <stubs.size(); i++)
	         {
		            try
		            {
					     if (stubs.get(i) != null)   
					     {
				             try
				             {
				            	 nResult = stubs.get(i).themeOperate(themeID, opType);
				             }
				             catch (ConnectException e) // 上一次正常的连接中断了
				             {
				            	 // 重新尝试一下，是否可以重新连接。如果可以，返回正常结果，否则产生一个连接中断
						    	 reConnect(i);
						    	 if (stubs.get(i) != null) nResult = stubs.get(i).themeOperate(themeID, opType);
				             }
					     }
					     else  // 上一次不正常的连接重试一次，有可能已经恢复
					     {
					    	 reConnect(i);
					    	 if (stubs.get(i) != null) nResult = stubs.get(i).themeOperate(themeID, opType);
					     }
		            }
		            catch (ConnectException e) // 上次中断的/新的中断尝试重连后还是中断状态
		            {
		              	 log.warn(GetCurrentDateStr() + e.getMessage());
		              	 stubs.set(i,null);
		            }

	         }
             long elapse = System.currentTimeMillis() - start;
			 log.info(GetCurrentDateStr() + "ThemeOperate themeID = " + themeID  
					 + " opType = " + opType +  " took_time = " + elapse + " ms");
		 }
		 catch (Exception ex)
		 {
	           log.warn(GetCurrentDateStr() + ex);   
		 }
		
		//
		return nResult;
	}
	

	@Override
	public List<String> getArticleIDsFromIndex(List<String> includeKeywords,List<String> excludeKeywords) {
		List<String> articleIDsList = null;
		 try
		 {
			 synchronized(this){
				 if (!inited) init();
				 }
	         long start = System.currentTimeMillis();
	            while (curStub<stubs.size())
	            {
		            try
		            {
					     if (stubs.get(curStub) != null)   
					     {
				             try
				             {
				            	 articleIDsList = stubs.get(curStub).getArticleIDsFromIndex(includeKeywords,excludeKeywords);
				             }
				             catch (ConnectException e) // 上一次正常的连接中断了
				             {
				            	 // 重新尝试一下，是否可以重新连接。如果可以，返回正常结果，否则产生一个连接中断
						    	 reConnect(curStub);
						    	 if (stubs.get(curStub) != null) articleIDsList = stubs.get(curStub).getArticleIDsFromIndex(includeKeywords,excludeKeywords);
				             }
					     }
					     else  // 上一次不正常的连接重试一次，有可能已经恢复
					     {
					    	 reConnect(curStub);
					    	 if (stubs.get(curStub) != null) articleIDsList = stubs.get(curStub).getArticleIDsFromIndex(includeKeywords,excludeKeywords);
					     }
		            }
		            catch (ConnectException e) // 上次中断的/新的中断尝试重连后还是中断状态
		            {
		              	 log.warn(GetCurrentDateStr() + e.getMessage());
		              	 stubs.set(curStub,null);
		            }
		            // 正常获得结果的返回
				    if (articleIDsList != null)	break;
				    // 尝试下一个索引服务器
					++curStub;
	            }
             long elapse = System.currentTimeMillis() - start;
			 if (curStub<stubs.size()) log.info(GetCurrentDateStr() + "getArticleIDsFromIndex From " + ips[curStub] +  "  query = " + includeKeywords 
					 +  " took_time = " + elapse + " ms");
			 else 
				 log.info(GetCurrentDateStr() + "getArticleIDsFromIndex failed query = " + includeKeywords 
					 +  " took_time = " + elapse + " ms");
				 
			 //curStub = (++curStub)%stubs.size();
		 }
		 catch (Exception ex)
		 {
	           log.warn(GetCurrentDateStr() + ex);   
		 }
		 curStub = 0;
		return articleIDsList;
	}


	@Override
	public List<String> getArticleIDsFromIndex(List<String> mustKeywords,
			List<String> shouldKeyWords, List<String> excludeKeywords)
			throws RemoteException {
		List<String> articleIDsList = null;
		 try
		 {
			 synchronized(this){
				 if (!inited) init();
				 }
	         long start = System.currentTimeMillis();
	            while (curStub<stubs.size())
	            {
		            try
		            {
					     if (stubs.get(curStub) != null)   
					     {
				             try
				             {
				            	 articleIDsList = stubs.get(curStub).getArticleIDsFromIndex(mustKeywords,shouldKeyWords,excludeKeywords);
				             }
				             catch (ConnectException e) // 上一次正常的连接中断了
				             {
				            	 // 重新尝试一下，是否可以重新连接。如果可以，返回正常结果，否则产生一个连接中断
						    	 reConnect(curStub);
						    	 if (stubs.get(curStub) != null) articleIDsList = stubs.get(curStub).getArticleIDsFromIndex(mustKeywords,shouldKeyWords,excludeKeywords);
				             }
					     }
					     else  // 上一次不正常的连接重试一次，有可能已经恢复
					     {
					    	 reConnect(curStub);
					    	 if (stubs.get(curStub) != null) articleIDsList = stubs.get(curStub).getArticleIDsFromIndex(mustKeywords,shouldKeyWords,excludeKeywords);
					     }
		            }
		            catch (ConnectException e) // 上次中断的/新的中断尝试重连后还是中断状态
		            {
		              	 log.warn(GetCurrentDateStr() + e.getMessage());
		              	 stubs.set(curStub,null);
		            }
		            // 正常获得结果的返回
				    if (articleIDsList != null)	break;
				    // 尝试下一个索引服务器
					++curStub;
	            }
            long elapse = System.currentTimeMillis() - start;
			 if (curStub<stubs.size()) log.info(GetCurrentDateStr() + "getArticleIDsFromIndex From " 
            + ips[curStub] +  "  query = " + mustKeywords 
					 +  " took_time = " + elapse + " ms");
			 else 
				 log.info(GetCurrentDateStr() + "getArticleIDsFromIndex failed query = " + mustKeywords 
					 +  " took_time = " + elapse + " ms");
				 
			 //curStub = (++curStub)%stubs.size();
		 }
		 catch (Exception ex)
		 {
	           log.warn(GetCurrentDateStr() + ex);   
		 }
		 curStub = 0;
		return articleIDsList;

	
	
	}
	
	public BriefResult getBriefResult(String searchField, String timeLine,String briefType)
	{
		// TODO Auto-generated method stub
		BriefResult briefResult = null;
		// 可以对多个主题，多个情感，进行时间分布方面的统计
		 try
		 {
			 synchronized(this){
				 if (!inited) init();
				 }
	         long start = System.currentTimeMillis();
	            while (curStub<stubs.size())
	            {
		            try
		            {
					     if (stubs.get(curStub) != null)   
					     {
				             try
				             {
				            	 briefResult = stubs.get(curStub).getBriefResult(searchField, timeLine, briefType);
				             }
				             catch (ConnectException e) // 上一次正常的连接中断了
				             {
				            	 // 重新尝试一下，是否可以重新连接。如果可以，返回正常结果，否则产生一个连接中断
						    	 reConnect(curStub);
						    	 if (stubs.get(curStub) != null) briefResult = stubs.get(curStub).getBriefResult(searchField, timeLine, briefType);
				             }
					     }
					     else  // 上一次不正常的连接重试一次，有可能已经恢复
					     {
					    	 reConnect(curStub);
					    	 if (stubs.get(curStub) != null) briefResult = stubs.get(curStub).getBriefResult(searchField, timeLine, briefType);
					     }
		            }
		            catch (ConnectException e) // 上次中断的/新的中断尝试重连后还是中断状态
		            {
		              	 log.warn(GetCurrentDateStr() + e.getMessage());
		              	 stubs.set(curStub,null);
		            }
		            // 正常获得结果的返回
				    if (briefResult != null)	break;
				    // 尝试下一个索引服务器
					++curStub;
	            }
             long elapse = System.currentTimeMillis() - start;
			 if (curStub<stubs.size()) log.info(GetCurrentDateStr() + "getBriefResult From " + ips[curStub] +  "  query = " + searchField 
					 +  " took_time = " + elapse + " ms");
			 else 
				 log.info(GetCurrentDateStr() + "getBriefResult failed query = " + searchField 
					 +  " took_time = " + elapse + " ms");
				 
			 //curStub = (++curStub)%stubs.size();
		 }
		 catch (Exception ex)
		 {
	           log.warn(GetCurrentDateStr() + ex);   
		 }
		 curStub = 0;
		return briefResult;
	}
    

	/** 设置或取消主题的文章ID
	 * 
	 * 
	 */
	@Override
	public int filterArticles(String userID, String themeId,
			List<String> articleIDs, String isFilter) throws RemoteException 
	{
		int nResult = 0;
		 try
		 {
			 synchronized(this){
				 if (!inited) init();
				 }
	         long start = System.currentTimeMillis();
	         for (int i = 0; i <stubs.size(); i++)
	         {
		            try
		            {
					     if (stubs.get(i) != null)   
					     {
				             try
				             {
				            	 nResult = stubs.get(i).filterArticles(userID, themeId, articleIDs, isFilter);
				             }
				             catch (ConnectException e) // 上一次正常的连接中断了
				             {
				            	 // 重新尝试一下，是否可以重新连接。如果可以，返回正常结果，否则产生一个连接中断
						    	 reConnect(i);
						    	 if (stubs.get(i) != null) nResult = stubs.get(i).filterArticles(userID, themeId, articleIDs, isFilter);
				             }
					     }
					     else  // 上一次不正常的连接重试一次，有可能已经恢复
					     {
					    	 reConnect(i);
					    	 if (stubs.get(i) != null) nResult = stubs.get(i).filterArticles(userID, themeId, articleIDs, isFilter);
					     }
		            }
		            catch (ConnectException e) // 上次中断的/新的中断尝试重连后还是中断状态
		            {
		              	 log.warn(GetCurrentDateStr() + e.getMessage());
		              	 stubs.set(i,null);
		            }

	         }
             long elapse = System.currentTimeMillis() - start;
			 log.info(GetCurrentDateStr() + "filterArticles userID = " + userID  
					 + " themeId = " + themeId +  " took_time = " + elapse + " ms");
		 }
		 catch (Exception ex)
		 {
	           log.warn(GetCurrentDateStr() + ex);   
		 }
		
		//
		return nResult;
	}
	


	private List<String> GetHostIPs()
	{
		Enumeration allNetInterfaces = null;
		try {
			allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InetAddress ip = null;
		while (allNetInterfaces.hasMoreElements())
		{
			NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
			System.out.println(netInterface.getName());
			Enumeration addresses = netInterface.getInetAddresses();
			while (addresses.hasMoreElements())
			{
				ip = (InetAddress) addresses.nextElement();
				if (ip != null )
				{
					System.out.println("本机的IP = " + ip.getHostAddress());
				} 
			}
		}
		return null;
		
	}

	
	@Override
	public int dealArticles(String userID, String deal, String themeId,
			String filterFields, List<String> articleIDs)
			throws RemoteException {
		int nResult = 0;
		 try
		 {
			 synchronized(this){
				 if (!inited) init();
				 }
	         long start = System.currentTimeMillis();
	         for (int i = 0; i <stubs.size(); i++)
	         {
		            try
		            {
					     if (stubs.get(i) != null)   
					     {
				             try
				             {
				            	 nResult = stubs.get(i).dealArticles(userID, deal,themeId, filterFields, articleIDs);
				             }
				             catch (ConnectException e) // 上一次正常的连接中断了
				             {
				            	 // 重新尝试一下，是否可以重新连接。如果可以，返回正常结果，否则产生一个连接中断
						    	 reConnect(i);
						    	 if (stubs.get(i) != null) nResult = stubs.get(i).dealArticles(userID, deal,themeId, filterFields, articleIDs);
				             }
					     }
					     else  // 上一次不正常的连接重试一次，有可能已经恢复
					     {
					    	 reConnect(i);
					    	 if (stubs.get(i) != null) nResult = stubs.get(i).dealArticles(userID, deal,themeId, filterFields, articleIDs);
					     }
		            }
		            catch (ConnectException e) // 上次中断的/新的中断尝试重连后还是中断状态
		            {
		              	 log.warn(GetCurrentDateStr() + e.getMessage());
		              	 stubs.set(i,null);
		            }

	         }
            long elapse = System.currentTimeMillis() - start;
			 log.info(GetCurrentDateStr() + "filterArticles userID = " + userID  
					 + " themeId = " + themeId +  " took_time = " + elapse + " ms");
		 }
		 catch (Exception ex)
		 {
	           log.warn(GetCurrentDateStr() + ex);   
		 }
		
		//
		return nResult;
	}


	
    public static void main(String[] args) {
    
	    RemoteSearchAgent remoteSearchAgent = new RemoteSearchAgent();  	
		try {
		    String searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    String filterFields = "ptime:20100801000000-20100817000000";
		    String sortFields = ""; //"ptime:a";
		    String groupFields = "";
		    PageInfo pageinfo = new PageInfo();
		    ThemeArticleList articleList = null;
		    ThemeCountByField themeCountByField = null;
		    List<ThemeCountByField> countResult = null;
//getArticleList searchFields = themeId:f9b459e32da26670012da27d02bd0001 
//filterFields = ptime:20110625000000-20110702000000 sortFields = articleGrade:a
		    //groupId:f9b459e52a6a83c9012a6abc0beb0077 filterFields = ptime:20110702160000-20110704160000
		    //简报调用方式
		    //getArticleList searchFields = themeId:f9b459e32fc4818d012fc4ae3c930001 
		    //filterFields = ptime:20110725000000-20110802000000 
		    //sortFields = duplicateId:d 
		    String maskField = "articleId";	    
		    List<String> maskIDs= new ArrayList<String>();
		    maskIDs.add("10162133");
		    maskIDs.add("11765637");
		    maskIDs.add("10162133");
		    
		    remoteSearchAgent.maskArticles("", "4028b8813e68da79013e6918a16e0002", maskField, maskIDs, "0");
		    		//(searchFields, filterFields, sortFields,pageinfo);

		    
		    searchFields = "themeId:4028b8813e68da79013e6918a16e0002";
		    
	        pageinfo.setCurPageNo(1);
	        pageinfo.setPageSize(5000);
	        articleList = remoteSearchAgent.getMaskedArticleList("", searchFields, filterFields, sortFields, pageinfo);
		    		

	 
	        remoteSearchAgent.themeOperate("4028b8813b49afe4013b49b45a4e0001","4028b8813cffdf6d013cffe2166c0001","d");
		    
		    remoteSearchAgent.themeOperate("4028b8813b2c4e41013b2c4ec2b80000", "u");
		    remoteSearchAgent.themeOperate("4028b8813b2c5127013b2c5254040000", "u");

		    searchFields = "themeId:402892bd3758b823013758b825e60008";
		    //pageinfo.setTotalRecords(-3);
		    filterFields = "ptime:20121015000000-20121114000000";
		    
	        sortFields = "ptime:d";
		    //pageinfo.setCurPageNo(5);
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);

		    searchFields = "themeId:f9b459e3314bf4c60131507c9075000d"; 
		    String timeLine = "ptime:20110731000000-20110808000000"; 
		    String briefType = "ptime";
		    BriefResult briefResult = remoteSearchAgent.getBriefResult(searchFields, timeLine, briefType);
		    
		    searchFields = "themeId:f9b459e331f5fa140131f9bfe5570006"; 
		    filterFields = "ptime:20111018000000-20111019000000"; 
		    sortFields = "ptime:d";

		    pageinfo.setTotalRecords(-3);
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    pageinfo.setTotalRecords(-2);
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    pageinfo.setTotalRecords(-1);
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    
		    searchFields = "userId:f9b459e32bf5a3e2012c065a636e0004"; 
		    filterFields = "phour:20110719000000-20110720000000"; 
		    groupFields = "phour AND mediaId:1501,32289,39,32284,282,32295,32573,32476,586,1240,32278,32602,32287,32457,32303,32315,32299,32313";
		    List<MultiCountResult> multiMediaCountResult = remoteSearchAgent.getMulitMediaCount(searchFields, filterFields, groupFields);

		    searchFields = "userId:f9b459e32bf5a3e2012c065a636e0004"; 
		    filterFields = "ptime:20110713000000-20110720000000"; 
		    groupFields = "ptime AND mediaId:1501,32289,39,32284,282,32295,32573,32476,586,1240,32278,32602,32287,32457,32303,32315,32299,32313";
		    multiMediaCountResult = remoteSearchAgent.getMulitMediaCount(searchFields, filterFields, groupFields);

		    searchFields = "";
		    filterFields = "phour:20110718000000-20110719000000";
		    groupFields = "phour AND themeId:402892b530e012220130e018d97e0001"; 
		    
		    countResult = remoteSearchAgent.getMultiThemeCount(searchFields, filterFields, groupFields);
		    
		    searchFields = "themeId:402892b530e012220130e018d97e0001";
		    filterFields = "phour:20110718000000-20110719000000";
		    groupFields = "phour AND mediaType:1,2,3,4,5,6"; 
		    countResult = remoteSearchAgent.getMultiThemeCount(searchFields, filterFields, groupFields);

		    searchFields = "themeId:402892b530e012220130e018d97e0001";
		    filterFields = "phour:20110718000000-20110719000000";
		    groupFields = "phour AND sentimentType:1,2,3"; 
		    countResult = remoteSearchAgent.getMultiThemeCount(searchFields, filterFields, groupFields);
		    
		    searchFields = "groupId:f9b459e52a6a83c9012a6abc0beb0077";
		    filterFields = "phour:20110717000000-20110719000000";
		    //topTheme = remoteSearchAgent.getTopThemesList(searchFields, filterFields, sortFields);
		    
		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003"; 
		    timeLine = "phour:20110717000000-20110718000000"; 
		    briefType = "phour";
		    briefResult = remoteSearchAgent.getBriefResult(searchFields, timeLine, briefType);
		    
		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003"; 
		    timeLine = "ptime:20110711000000-20110718000000"; 
		    briefType = "ptime";
		    briefResult = remoteSearchAgent.getBriefResult(searchFields, timeLine, briefType);
		    


		    List<String> includeKeywords = new ArrayList<String>();
		    includeKeywords.add("天气");
		    List<String> excludeKeywords = new ArrayList<String>();
			List<String> articleIDsList =remoteSearchAgent.getArticleIDsFromIndex(includeKeywords,excludeKeywords);


			 //getThemeCount searchFields = groupId:f9b459e52a6a83c9012a6abc0beb0077 
			//filterFields = ptime:20110806150000-20110808150000 
	
		    //getThemeCount searchFields = groupId:f9b459e330cf0fda0130dfa6e0500039 filterFields = ptime:20110629190000-20110701190000
		    //getMultiThemeCount searchFields =  filterFields = ptime:20110515170000-20110516170000 groupFields = ptime AND themeId:f9b459e32ff7d395012ff7dc55f00003 
		    searchFields = "groupId:f9b459e52a6a83c9012a6abc0beb0077";
		    filterFields = "ptime:ptime:20110806150000-20110808150000";
		    //groupFields = "mediaType";
		    themeCountByField = remoteSearchAgent.getThemeCount(searchFields, filterFields, groupFields);


		    searchFields = "themeId:402892b52a824f65012a8251a53d0001";
		    filterFields = "ptime:20110511000000-20110518000000";
		    groupFields = "mediaType";
		    themeCountByField = remoteSearchAgent.getThemeCount(searchFields, filterFields, groupFields);

		    List<ThemeCountByField> themeCountResults = remoteSearchAgent.getMultiThemeCount(searchFields, filterFields, groupFields);
		    
		    searchFields = "themeId:f9b459e32ff7d395012ff7dc55f00003"; 
		    filterFields = "ptime:20110515170000-20110516170000";
		    groupFields = "mediaType";
		    
		    searchFields = "";
		    filterFields = "ptime:20110515120000-20110516120000";
		    groupFields = "ptime AND themeId:f9b459e32aea219b012aeacba1720003";
		    //sortFields = "duplicateId:d";
		    //pageinfo.setTotalRecords(-1);
		    themeCountResults = null; //remoteSearchAgent.getMultiThemeCount(searchFields, filterFields, groupFields);
		  
		    //
		    //getArticleList

		    searchFields = "themeId:f9b459e32f58c4c8012f619749830008";
		    filterFields = "ptime:20110416110000-20110418110000";
		    sortFields = "duplicateId:d";
		    //pageinfo.setTotalRecords(-1);
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);

		    

		    searchFields = "themeId:f9b459e32e2790c9012e2d47e5950033";
		    filterFields = "ptime:20110401180000-20110402180000";
		    groupFields = "ptime AND mediaType:1,2,3,4,5,6";
		    themeCountResults = remoteSearchAgent.getMultiThemeCount(searchFields, filterFields, groupFields);

		    

		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    filterFields = "ptime:20110202000000-20110402000000";
		    groupFields = "monthRatio";
		    //groupFields = "mediaType";
		    themeCountByField = remoteSearchAgent.getThemeCount(searchFields, filterFields, groupFields);
		    


		    
		    searchFields = "";
		    filterFields = "ptime:20110328160000-20110329160000";
		    groupFields = "ptime AND themeId:f9b459e32aea219b012aeacba1720003";
		    themeCountResults = remoteSearchAgent.getMultiThemeCount(searchFields, filterFields, groupFields);

	

		    searchFields = "themeId:f9b459e32e31d479012e396b4afe000f";
		    filterFields = "ptime:20110327170000-20110328170000";
		    sortFields = "";
		    //pageinfo.setTotalRecords(-1);
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
	    //remoteSearchAgent.themeOperate("f9b459e52c3dca6b012c3de61c8a0002", "u");
		    //themeId:f9b459e32aea219b012aeacba1720003 filterFields = ptime:20110215000000-20110222000000 groupFields = mediaId
		    
		    //getTopThemesList searchFields = groupId:f9b459e52a6a83c9012a6abc0beb0077 filterFields = ptime:20110317000000-20110324000000

		    
		    
		    searchFields = "";
		    filterFields = "ptime:20110227000000-20110228000000";
		    groupFields = "ptime AND themeId:f9b459e32aea219b012aeacba1720003";
		    themeCountResults = remoteSearchAgent.getMultiThemeCount(searchFields, filterFields, groupFields);

	    
		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    filterFields = "ptime:20110227000000-20110228000000";
		    groupFields = "mediaType";
		    themeCountByField = remoteSearchAgent.getThemeCount(searchFields, filterFields, groupFields);
		    

		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    filterFields = "Multi-mediaType:1,2,3,4,5,6 AND ptime:20110227000000-20110228000000";
		    sortFields = "";
		    //pageinfo.setTotalRecords(-1);
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);

		    
		    searchFields = "themeId:f9b459e52c636286012c7cb25ca20005";
		    filterFields = "Multi-mediaType:1,2,3,4,5,6 AND ptime:20110221000000-20110228000000";
		    sortFields = "";
		    //pageinfo.setTotalRecords(-1);
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    
		    

		    themeCountByField = remoteSearchAgent.getThemeCount(searchFields, filterFields, groupFields);
		    
		    //remoteSearchAgent.themeOperate("f9b459e32aea219b012aeacba1720003", "u");

		    
		    //remoteSearchAgent.themeOperate("f9b459e32aea219b012aeacba1720003", "u");

		    searchFields = "themeId:f9b459e32e17b216012e17e517000001";
		    filterFields = "ptime:20110210000000-20110217000000";
		    sortFields = "duplicateId:d";
		    //pageinfo.setTotalRecords(-1);
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);

		    searchFields = "themeId:f9b459e32e17b216012e17e517000001";
		    filterFields = "Multi-mediaType:1,2,3,4,5,6 AND ptime:20110214000000-20110215000000";
		    sortFields = "";
		    //pageinfo.setTotalRecords(-1);
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    
		    
		    //duplicateId:38843429 AND themeId:f9b459e52af5e14d012af9d934410002 filterFields = ptime:20110118000000-20110125000000 sortFields =
		    searchFields = "themeId:f9b459e32e23669d012e26dd47dd001d";
		    filterFields = "ptime:20110209000000-20110216000000";
		    sortFields = "";
		    //pageinfo.setTotalRecords(-1);
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);


		    groupFields = "ptime";
		    
		    
		    groupFields = "mediaType";
		    themeCountByField = remoteSearchAgent.getThemeCount(searchFields, filterFields, groupFields);

		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    filterFields = "ptime:20110131000000-20110201000000";
		    groupFields = "ptime AND mediaType:1,2,3,4,5,6";
		    themeCountResults = remoteSearchAgent.getMultiThemeCount(searchFields, filterFields, groupFields);
		    
		    //remoteSearchAgent.themeOperate("f9b459e52c3dca6b012c3de61c8a0002", "u");

		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    filterFields = "ptime:20110114000000-20110121000000";
		    sortFields = "";
		    //pageinfo.setTotalRecords(-1);
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    
		    
		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    filterFields = "ptime:20110110000000-20110117000000 AND mediaId:32303";
		    sortFields = "";
		    pageinfo.setTotalRecords(-1);
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);

		    searchFields = "mediaId:32303 AND groupId:f9b459e52a6a83c9012a6abc0beb0077";
		    filterFields = "ptime:20110110000000-20110117000000";
		    sortFields = "";
		    List<ArticleCountByTheme>  articleCountList = remoteSearchAgent.getFocusedMediaCount(searchFields,filterFields,sortFields);
		    
		    
		    //groupId:f9b459e52a6a83c9012a6abc0beb0077 filterFields = ptime:20110110000000-20110117000000 AND mediaId:32303		    
		    searchFields = "groupId:f9b459e52a6a83c9012a6abc0beb0077";
		    filterFields = "ptime:20110110000000-20110117000000";
		    List<TopTheme> topTheme = remoteSearchAgent.getTopThemesList(searchFields, filterFields, sortFields);
		    
		    
		    searchFields = "groupId:f9b459e52a6a83c9012a6abc0beb0077";
		    filterFields = "ptime:20110108000000-20110115000000 AND mediaId:32315";
		    sortFields = "ptime:d";
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);

		    searchFields = "groupId:f9b459e52a6a83c9012a6abc0beb0077";
		    filterFields = "ptime:20110108000000-20110115000000 AND mediaId:32315";
		    sortFields = "ptime:d";
		    pageinfo.setTotalPages(-1);
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    
		    
		    searchFields = "mediaId:32278 AND groupId:f9b459e52a6a83c9012a6abc0beb0077";
		    filterFields = "ptime:20110108000000-20110115000000 AND mediaType:3";
		    articleCountList = remoteSearchAgent.getFocusedMediaCount(searchFields, filterFields, sortFields);
		    
		    searchFields = "themeId:ff8080812d821c30012d821e5ad50001";
		    filterFields = "ptime:20100708000000-20100714000000";
		    sortFields = "ptime:d";
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    
		    
		    
		    searchFields = "groupId:f9b459e52a6a83c9012a6abc0beb0077";
		    filterFields = "ptime:20110108000000-20110115000000 AND mediaId:32315";
		    sortFields = "viewCount:d";
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);

		    searchFields = "groupId:f9b459e52a6a83c9012a6abc0beb0077";
		    filterFields = "ptime:20110108000000-20110115000000 AND mediaId:32315";
		    sortFields = "";
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    //remoteSearchAgent.themeOperate("f9b459e32aea219b012aeacba1720003", "u");

		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    filterFields = "ptime:20110104120000-2011010512000000";
		    sortFields = "ptime:d";
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);

		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    filterFields = "ptime:20110106000000-20110113000000";
		    groupFields = "ptime AND sentimentType:1,2,3";
		    themeCountResults = remoteSearchAgent.getMultiThemeCount(searchFields, filterFields, groupFields);

		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    filterFields = "ptime:20110106000000-20110113000000";
		    groupFields = "mediaType";
		    themeCountByField = remoteSearchAgent.getThemeCount(searchFields, filterFields, groupFields);

		    searchFields = "groupId:4028928628fd1dc30128fd1dc3ef0002";
		    filterFields = "ptime:20110105000000-20110112000000 AND mediaId:32295";
		    sortFields = "viewCount:d";
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);

		    
		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    filterFields = "ptime:20110104000000-20110111000000";
		    sortFields = "viewCount:d";
		    
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    filterFields = "ptime:20110104000000-20110111000000";
		    sortFields = "relative:a";
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    
		    
		    
		    searchFields = "mediaId:32313 AND themeId:f9b459e02b14e0bd012b14e331b70001";
		    filterFields = "ptime:20100811000000-20100818000000";
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    
		    

		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    filterFields = "ptime:20100810000000-20100817000000";
		    groupFields = "ptime AND mediaType:1,2,3,4,5,6";
		    themeCountResults = remoteSearchAgent.getMultiThemeCount(searchFields, filterFields, groupFields);
		    
		    
		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    filterFields = "ptime:20100810000000-20100817000000";
		    groupFields = "ptime AND sentimentType:1,2,3";
		    themeCountResults = remoteSearchAgent.getMultiThemeCount(searchFields, filterFields, groupFields);

		    
		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    filterFields = "ptime:20100810000000-20100817000000 AND mediaType:6";
		    //filterFields = "ptime:20100801000000-20100817000000 AND mediaType:6";
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    
		    
		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";	    
		    filterFields = "ptime:20100801000000-20100817000000";
		    sortFields = "relative:a";
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);

		    /*searchFields = "themeId:f9b459e32aea219b012aeacba1720003";	    
		    filterFields = "ptime:20100801000000-20100817000000";
		    sortFields = "";
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    
            
		    filterFields = "ptime:20100801000000-20100817000000 AND mediaType:2";
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    
		    filterFields = "ptime:20100801000000-20100817000000 AND mediaType:3";
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
		    
		    filterFields = "ptime:20100801000000-20100817000000 AND mediaType:4";
		    articleList = remoteSearchAgent.getArticleList(searchFields, filterFields, sortFields,pageinfo);
            */
		    
		    searchFields = "groupId:4028928628fd1dc30128fd1dc3ef0002 AND mediaId:32284";
		    articleCountList = remoteSearchAgent.getFocusedMediaCount(searchFields, filterFields, sortFields);
		    
		    groupFields = "ptime";
		    
		    themeCountByField = remoteSearchAgent.getThemeCount(searchFields, filterFields, groupFields);

		    groupFields = "mediaId";
		    themeCountByField = remoteSearchAgent.getThemeCount(searchFields, filterFields, groupFields);
		    
		    groupFields = "mediaType";
		    themeCountByField = remoteSearchAgent.getThemeCount(searchFields, filterFields, groupFields);

		    searchFields ="";
		    filterFields = "ptime:20100810000000-20100817000000";
		    groupFields = "ptime AND themeId:f9b459e32aea219b012aeacba1720003";
		    themeCountResults = remoteSearchAgent.getMultiThemeCount(searchFields, filterFields, groupFields);
		    
		    searchFields = "groupId:4028928628fd1dc30128fd1dc3ef0002";
		    filterFields = "ptime:20100801000000-20100817000000";
		    topTheme = remoteSearchAgent.getTopThemesList(searchFields, filterFields, sortFields);
		    
		    log.info(remoteSearchAgent.GetCurrentDateStr() + " List: " + articleList);
		    log.info(remoteSearchAgent.GetCurrentDateStr() + " List: " + topTheme);
		    log.info(remoteSearchAgent.GetCurrentDateStr() + " themeCountByField: " + themeCountByField);
		    log.info(remoteSearchAgent.GetCurrentDateStr() + " themeCountResults: " + themeCountResults);
			}
		catch (Exception e) {
				log.warn(remoteSearchAgent.GetCurrentDateStr() + e.getMessage());   
			}
	    }

	@Override
	public int maskArticles(String userID, String themeId, String maskField,
			List<String> maskIDs, String isMask) throws RemoteException {
		// TODO Auto-generated method stub
		int nResult = 0;
		 try
		 {
			 synchronized(this){
				 if (!inited) init();
				 }
	         long start = System.currentTimeMillis();
	         for (int i = 0; i <stubs.size(); i++)
	         {
		            try
		            {
					     if (stubs.get(i) != null)   
					     {
				             try
				             {
				            	 nResult = stubs.get(i).maskArticles(userID, themeId,maskField, maskIDs, isMask);
				             }
				             catch (ConnectException e) // 上一次正常的连接中断了
				             {
				            	 // 重新尝试一下，是否可以重新连接。如果可以，返回正常结果，否则产生一个连接中断
						    	 reConnect(i);
						    	 if (stubs.get(i) != null) nResult = stubs.get(i).maskArticles(userID, themeId,maskField, maskIDs, isMask);
				             }
					     }
					     else  // 上一次不正常的连接重试一次，有可能已经恢复
					     {
					    	 reConnect(i);
					    	 if (stubs.get(i) != null) nResult = stubs.get(i).maskArticles(userID, themeId,maskField, maskIDs, isMask);
					     }
		            }
		            catch (ConnectException e) // 上次中断的/新的中断尝试重连后还是中断状态
		            {
		              	 log.warn(GetCurrentDateStr() + e.getMessage());
		              	 stubs.set(i,null);
		            }

	         }
           long elapse = System.currentTimeMillis() - start;
			 log.info(GetCurrentDateStr() + "maskArticles userID = " + userID  
					 + " themeId = " + themeId +  " took_time = " + elapse + " ms");
		 }
		 catch (Exception ex)
		 {
	           log.warn(GetCurrentDateStr() + ex);   
		 }
		
		//
		return nResult;
	}

	@Override
	public ThemeArticleList getArticleList(String userID, String searchFields,
			String filterFields, String sortFields, PageInfo pageInfo)
			throws RemoteException {
		// TODO Auto-generated method stub
		return getArticleList(searchFields,filterFields,sortFields,pageInfo);
	}

	@Override
	public int themeOperate(String userID, String themeID, String opType)
			throws RemoteException {
		// TODO Auto-generated method stub
		return themeOperate(themeID,opType);
	}

	@Override
	public ThemeArticleList getMaskedArticleList(String userID,
			String searchFields, String filterFields, String sortFields,
			PageInfo pageInfo) throws RemoteException {
		// TODO Auto-generated method stub
		ThemeArticleList themeArtibleList = null;
		 try
		 {
			    synchronized(this){
				 if (!inited) init();
				 }
	            long start = System.currentTimeMillis();
	            while (curStub<stubs.size())
	            {
		            try
		            {
					     if (stubs.get(curStub) != null)   
					     {
				             try
				             {
				            	 themeArtibleList = stubs.get(curStub).getMaskedArticleList(userID, searchFields, filterFields, sortFields, pageInfo);
				             }
				             catch (ConnectException e) // 上一次正常的连接中断了
				             {
				            	 // 重新尝试一下，是否可以重新连接。如果可以，返回正常结果，否则产生一个连接中断
						    	 reConnect(curStub);
						    	 if (stubs.get(curStub) != null) themeArtibleList = stubs.get(curStub).getMaskedArticleList(userID, searchFields, filterFields, sortFields, pageInfo);
				             }
					     }
					     else // 上一次不正常的连接重试一次，有可能已经恢复
					     {
					    	 reConnect(curStub);
					    	 if (stubs.get(curStub) != null) themeArtibleList = stubs.get(curStub).getMaskedArticleList(userID, searchFields, filterFields, sortFields, pageInfo);
					     }
		            }
		            catch (ConnectException e) // 上次中断的/新的中断尝试重连后还是中断状态
		            {
		              	 log.warn(GetCurrentDateStr() + e.getMessage());
		              	 stubs.set(curStub,null);
		            }
		            // 正常获得结果的返回
				    if (themeArtibleList != null)	break;
				    // 尝试下一个索引服务器
					++curStub;
	            }
	            long elapse = System.currentTimeMillis() - start;
				if (curStub<stubs.size()) log.info(GetCurrentDateStr() + "getMaskedArticleList From " + ips[curStub] +  " searchFields = " + searchFields + " filterFields = " 
						 + filterFields + " sortFields = " + sortFields +  " took_time = " + elapse + " ms");
				else 
					 log.info(GetCurrentDateStr() + "getMaskedArticleList failed searchFields = " + searchFields + " filterFields = " 
							 + filterFields + " sortFields = " + sortFields +  " took_time = " + elapse + " ms");
		 }
		 catch (Exception ex)
		 {
	           log.warn(GetCurrentDateStr() + ex);   
		 }
		curStub = 0;
		return themeArtibleList;
	}


    
}
