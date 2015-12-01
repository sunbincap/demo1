/*
 * @(#)EmailBriefing.java	2011-7-15 上午11:53:49
 *
 * Copyright 2010 LoongTao, Inc. All rights reserved.
 */

package com.loongtao.mediasearch.mediasearchinterface;

import java.io.Serializable;
import java.util.Map;

/**
 * 用于定义简报请求远程索引的结果集
 * 
 * <pre>
 * 	searchField:请求索引的参数 e.q <i>themeId:xxxxxxxx</i>表示请求的是哪个主题
 * 	timeLine:请求索引的参数 e.q <i>ptime:yyyyMMddHHmmss-yyyyMMddHHmmss</i> 表示请求的时间范围
 * 	briefingType:请求索引的参数 e.q <i>briefingType:phour</i> | <i>briefingType:ptime</i> 表示请求的简报的类型，phour日报,ptime周报
 * 远程接口方法:BriefingResult result=searchBriefingResultByThemeId(String searchField,String timeLine,String briefType)
 * 
 * </pre>
 * 
 * @author resolute.chen
 * @reviewer Huang Xiaohong
 * @version 1.0
 * @since JDK1.6
 * @date 2011-7-15 上午11:53:49
 */
public class BriefResult  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4119226790299926401L;
	
	/* 按照请求简报的类型,返回本日或者本周的文章总数 */
	private int currentArticlesCount;
	/* 按照请求简报的类型,返回昨日或者上周周的文章总数 */
	private int lastArticlesCount;
	/* 媒体的关注度，按照当前请求的简报类型计算文章当前和上个时间段文章的总数变化率 */
	private int mediaIncrease;
	
	/* 按照请求简报的类型,返回本日或者本周的独立媒体的个数 */
	private int currentMediasCount;
	/* 按照请求简报的类型,返回昨日或者上周的独立媒体的个数 */
	private int lastMediasCount;
	/* 媒体的扩散量，按照当前请求的简报类型计算当前独立媒体的个数和上个时间段独立媒体个数变化率 */
	private int mediaSpread;
	
	
	/* 主题时间分布图，日报时间节点是小时，周报时间节点是天，key是时间节点，value是该时间节点的值 */
	private Map<String, Integer> themeCountByTime;
	
	/*
	 * 主题媒体的扩散图，按照简报的类型，昨日和上周的数据。value是该媒体类型的值，key就是媒体类型
	 */
	private Map<String, Integer> currentThemeCountByMediaType;
	
	/* 媒体类型图，key是媒体类型，value是该媒体类型的值 */
	private Map<String, Integer> lastThemeCountByMediaType;
	
	
	/* 活跃媒体的图，key是媒体的Id，value是该媒体类型的值 */
	private Map<String, Integer> themeCountByActiveMedia;
	
	/* 活跃媒体的图，key是情感分类，value是该情感的值 */
	private Map<String, Integer> themeCountBySentiment;
	
	
    public Map<String, Integer> getThemeCountBySentiment() {
		return themeCountBySentiment;
	}

	public void setThemeCountBySentiment(Map<String, Integer> themeCountBySentiment) {
		this.themeCountBySentiment = themeCountBySentiment;
	}

	public String toString()
    {
    	StringBuilder value = new StringBuilder();
    	value.append("currentArticlesCount = " + Integer.toString(currentArticlesCount));
    	value.append(" lastArticlesCount = " + Integer.toString(lastArticlesCount));
    	value.append(" mediaIncrease = " + Integer.toString(mediaIncrease));
    	
       	value.append("\n themeCountByTime = " + themeCountByTime.toString());
   	
    	value.append("\n currentThemeCountByMediaType = " + currentThemeCountByMediaType.toString());
       	value.append("\n lastThemeCountByMediaType = " + lastThemeCountByMediaType.toString());
       	value.append("\n themeCountByActiveMedia = " + themeCountByActiveMedia.toString());
   	
    	return value.toString();    	
    }
	
	public int getCurrentArticlesCount() {
		return currentArticlesCount;
	}

	public void setCurrentArticlesCount(int currentArticlesCount) {
		this.currentArticlesCount = currentArticlesCount;
	}

	public int getLastArticlesCount() {
		return lastArticlesCount;
	}

	public void setLastArticlesCount(int lastArticlesCount) {
		this.lastArticlesCount = lastArticlesCount;
	}

	public int getMediaIncrease() {
		return mediaIncrease;
	}

	public void setMediaIncrease(int mediaIncrease) {
		this.mediaIncrease = mediaIncrease;
	}


	public int getCurrentMediasCount() {
		return currentMediasCount;
	}

	public void setCurrentMediasCount(int currentMediasCount) {
		this.currentMediasCount = currentMediasCount;
	}

	public int getLastMediasCount() {
		return lastMediasCount;
	}

	public void setLastMediasCount(int lastMediasCount) {
		this.lastMediasCount = lastMediasCount;
	}

	public int getMediaSpread() {
		return mediaSpread;
	}

	public void setMediaSpread(int mediaSpread) {
		this.mediaSpread = mediaSpread;
	}

	public Map<String, Integer> getThemeCountByTime() {
		return themeCountByTime;
	}

	public void setThemeCountByTime(Map<String, Integer> themeCountByTime) {
		this.themeCountByTime = themeCountByTime;
	}



	public Map<String, Integer> getThemeActiveMedia() {
		return themeCountByActiveMedia;
	}

	public void setThemeActiveMedia(Map<String, Integer> themeCountByActiveMedia) {
		this.themeCountByActiveMedia = themeCountByActiveMedia;
	}

	public void setLastThemeCountByMediaType(
			Map<String, Integer> lastThemeCountByMediaType) {
		this.lastThemeCountByMediaType = lastThemeCountByMediaType;
	}

	public Map<String, Integer> getLastThemeCountByMediaType() {
		return lastThemeCountByMediaType;
	}

	public void setCurrentThemeCountByMediaType(
			Map<String, Integer> currentThemeCountByMediaType) {
		this.currentThemeCountByMediaType = currentThemeCountByMediaType;
	}

	public Map<String, Integer> getCurrentThemeCountByMediaType() {
		return currentThemeCountByMediaType;
	}
}
