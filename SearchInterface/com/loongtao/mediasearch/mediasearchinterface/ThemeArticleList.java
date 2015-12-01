package com.loongtao.mediasearch.mediasearchinterface;

import java.io.Serializable;
import java.util.List;

public class ThemeArticleList implements Serializable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7497712272920180769L;
	private String themeID;
	private int readCount;
	private int unreadCount;
	private PageInfo pageInfo;
	private List<String> articleIDList;
	
	/***
	 * 
	 * @return 主题ID
	 */
	public String getThemeID() {
		return themeID;
	}
	public void setThemeID(String themeID) {
		this.themeID = themeID;
	}
	
	/**
	 * 
	 * @return 文章ID列表
	 */
	public List<String> getArticleIDList() {
		return articleIDList;
	}
	public void setArticleIDList(List<String> articleIDList) {
		this.articleIDList = articleIDList;
	}
	
	
	public void setPageInfo(PageInfo pageInfo) {
		this.pageInfo = pageInfo;
	}
	
	/**
	 * 
	 * @return 分页信息
	 */
	public PageInfo getPageInfo() {
		return pageInfo;
	}
	public int getUnreadCount() {
		return unreadCount;
	}
	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}
	public int getReadCount() {
		return readCount;
	}
	public void setReadCount(int readCount) {
		this.readCount = readCount;
	}
}
