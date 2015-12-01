package com.loongtao.mediasearch.mediasearchinterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/***
 * 主题远程查询接口，采用rmi协议
 * 
 * @author Huang Xiaohong
 *
 */
public interface ISearchAgent extends Remote {

	public List<TopTheme> getTopThemesList(String searchFields,String filterFields,String sortFields)
	throws RemoteException;
	
	/**
	 * 获取文章的列表
	 * @param searchFields，指定查询的条件，一般是主题ID，
	 *      格式为：themeId:f9b459e32aea219b012aeacba1720003
	 * 
	 * 如果要获取已读和未读数，需要标明用户ID
	 *      格式一般为： themeId:f9b459e32aea219b012aeacba1720003 AND userId:9b459e52a6a83c9012a6abc0beb0077
	 *                    
	 * @param filterFields 指定统计的时间范围，限定的过滤条件如媒体类型，情感类型
	 *        2013/1/23 增加 已读/未读过滤选项   readtag:1  (已读） readtag:0  （未读）
	 * @param sortFields  排序字段
	 * 
	 * 发布时间:ptime 升序(ptime:a)/降序(ptime:d)
	 * 话题参与度:activeness      使用转发数和评论数之和   升序(activeness:a)/降序(activeness:d)
	 * 作者影响力:authorFans      根据作者粉丝数排列   升序(authorFans:a)/降序(authorFans:d)
	 * 作者权威度:authorInfluence  首先根据作者是否加V,然后判断作者粉丝数多少排列。升序(authorInfluence:a)/降序(authorInfluence:d)
	 *   暂不支持    负面情绪指数: articleGrade     根据article_grade 字段值排列      升序(articleGrade:a)/降序(articleGrade:d)
	 * 情感字段排序： sentimentType
	 * 
	 * @param pageInfo 分页信息，参见PageInfo的定义
	 * @return
	 * @throws RemoteException
	 */
	public ThemeArticleList getArticleList(String userID,String searchFields,String filterFields,String sortFields,PageInfo pageInfo)
	throws RemoteException;
	
    
    /**
     * 获取某个用户已经屏蔽的文章列表
     * @param userID
     * @param searchFields
     * @param filterFields
     * @param sortFields
     * @param pageInfo
     * @return
     * @throws RemoteException
     */
	public ThemeArticleList getMaskedArticleList(String userID,String searchFields,String filterFields,String sortFields,PageInfo pageInfo)
			throws RemoteException;
	
	
	
	@Deprecated
	public ThemeArticleList getArticleList(String searchFields,String filterFields,String sortFields,PageInfo pageInfo)
	throws RemoteException;
	
	/** 
	 * 针对单个主题统计   
	 * 
	 *    或者某个企业组，统计该企业在某段时间内的主题数量的变化率，e.g. groupId:f9b459e52a6a83c9012a6abc0beb0077
	 * 
	 *  4.1 增加了某个主题的月增长率 调用格式如下：
	 *  		    searchFields = "themeId:f9b459e32aea219b012aeacba1720003";
		    filterFields = "ptime:20110202000000-20110402000000";
		    groupFields = "monthRatio";

	 * @param searchFields  
	 * @param filterFields 指定统计的时间范围，限定的过滤条件如媒体类型，情感类型
	 * @param groupFields
	 * @return 返回的结果是 键值对的列表
	 * @throws RemoteException
	 */
	public ThemeCountByField getThemeCount(String searchFields,String filterFields,String groupFields)
	throws RemoteException;
	
	@Deprecated
	/**
	 * 关注媒体统计
	 * @param searchFields
	 * @param filterFields
	 * @param groupFields   
	 * @return
	 * @throws RemoteException
	 */
	public List<ArticleCountByTheme> getFocusedMediaCount(String searchFields,String filterFields,String groupFields)
	throws RemoteException;
	
	/**
	 *一次统计多个主题
	 * @param searchFields
	 * @param filterFields
	 * @param groupFields   字段ptime 按天统计，字段phour按小时统计，其他按照字段的取值统计
	 * @return
	 * @throws RemoteException
	 */
	public List<ThemeCountByField> getMultiThemeCount(String searchFields,String filterFields,String groupFields)
	throws RemoteException;
	
 	
	/**
	 * 针对主题进行的操作，包括增加，
	 * @param themeID 主题ID 
	 * @param opType  操作类型，包括 插入："i",修改："u",删除："d"
	 * @return
	 * @throws RemoteException
	 */
	
	public int themeOperate(String userID,String themeID,String opType) throws RemoteException;
	
	@Deprecated
	public int themeOperate(String themeID,String opType) throws RemoteException;

	/**
	 * 根据输入的关键词，查询索引，返回相关的文档的ID的列表，时间范围三天
	 * @param query
	 * @return
	 */
	public List<String> getArticleIDsFromIndex(List<String> includeKeywords,List<String> excludeKeywords)throws RemoteException;

	/**
	 * 根据输入的关键词(增加了可选关键词，查询索引，返回相关的文档的ID的列表，时间范围三天
	 * @param query
	 * @return
	 */
	public List<String> getArticleIDsFromIndex(List<String> mustKeywords,List<String> shouldKeyWords,List<String> excludeKeywords)throws RemoteException;

	@Deprecated
	/**
	 * 获取简报所需的结果(指定时间范围内）
	 * @param searchField 查询的字段一般是主题ID，例如 themeID:fffff
	 * @param timeLine 时间范围  例如：ptime:20110717000000-20110718000000 （按小时） ,ptime:20110711000000-20110718000000 （按天）
	 * @param briefingType 简报统计类型，按小时统计:"phour"，还是按天统计 "ptime" 
	 * @return
	 */
	public BriefResult getBriefResult(String searchField,String timeLine,String briefType) throws RemoteException;
	
	/**
	 * 关注媒体统计新版本
	 * @param searchFields
	 * @param filterFields
	 * @param groupFields
	 * @return
	 * @throws RemoteException
	 */
	public List<MultiCountResult> getMulitMediaCount(String searchFields,String filterFields,String groupFields)
	throws RemoteException;
    
	/**
	 * 设置过滤的文章或取消以前过滤的文章
	 * @param userID  用户ID
	 * @param themeId  主题ID
	 * @param articleIDs  文章ID列表
	 * @param isMask   是否屏蔽  “1”  表示屏蔽 ，“0” 表示取消屏蔽 
	 * @return 操作的状态 0 表示成功， 其他表示失败，数字表示相应的错误号
	 * @throws RemoteException
	 */
	@Deprecated
	public int filterArticles(String userID,String themeId,List<String> articleIDs,String isMask)
	throws RemoteException;


	/**
	 * 根据指定字段屏蔽某些文章
	 * @param userID 
	 * @param themeId
	 * @param maskField 屏蔽的字段名称，目前支持 "id"，"authorId" 参见WeiboField.java
	 * 
	 * @param maskIDs  屏蔽ID的列表
	 * @param isMask  ”1" 表示屏蔽  "0" 表示取消屏蔽 
	 * @return
	 * @throws RemoteException
	 */
	
	public int maskArticles(String userID,String themeId,String maskField,List<String> maskIDs,String isMask)
	throws RemoteException;	

	
	
	
	
/**
 * 对某个用户的某个主题的部分文章进行指定的操作，由deal指定操作内容	
 * @param userID 用户ID 
 * @param deal 操作类型
 *       通用格式：操作名称：操作内容  （1： 表示生效，0：表示取消，不是所有的操作都支持取消）
 *       目前支持的操作包括：
 *           1  deal 参数  "readMark:1"  表示对某些文章标记已读    
 *           2  deal 参数  "modify:fieldName{,fieldName}",
 *              
 * @param themeId 主题ID
 * @param filterFields 文章过滤条件，预留
 * @param articleIDs 
 *           1  文章ID列表 ， 如果id列表的第一项是all,对当前月的全部数据标记为已读
 *           
 *           2  deal = "modify:sentimentType"时， articleIDs中的String格式为："article_id:ptime:fieldValue{,fieldValue}",
 *                                                                      e.g. "3355:20121218151020:1";
 *            表示对情感字段进行修改  ，要修改的文章ID为3355 发布时间为20121218151020，修改的值为1（负面）
              如果修改别的字段，格式类似（保留）, 可以分别在deal和ids中增加字段名和字段值，用逗号隔开
              
                     这样做的好处是可以一次修改多篇文章的情感，减少了传输，
 * @return 
 * 
 * @throws RemoteException
 */
	public int dealArticles(String userID,String deal,String themeId,String filterFields,List<String> articleIDs)
	throws RemoteException;

	
}
