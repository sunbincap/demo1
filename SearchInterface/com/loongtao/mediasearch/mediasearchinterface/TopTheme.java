package com.loongtao.mediasearch.mediasearchinterface;

import java.io.Serializable;
import java.util.List;

/** 
 * themeID 主题ID
 * ArticleIDList 该主题前N篇文档
 * Last24HoursCount 最近24小时 文章数
 * last48HoursCount 最近48小时~最近24小时文章数
 * 
 * ratio 48 小时变化率
 * @author Huang Xiaohong
 *
 */
public class TopTheme implements Serializable {
	public String getThemeID() {
		return themeID;
	}
	public void setThemeID(String themeID) {
		this.themeID = themeID;
	}
	public List<String> getArticleIDList() {
		return articleIDList;
	}
	public void setArticleIDList(List<String> articleIDList) {
		this.articleIDList = articleIDList;
	}
	public List<CountByField> getThemeCount() {
		return themeCount;
	}
	public void setThemeCount(List<CountByField> themeCount) {
		this.themeCount = themeCount;
	}
	public void setLast48HoursCount(int last48HoursCount) {
		Last48HoursCount = last48HoursCount;
	}
	public int getLast48HoursCount() {
		return Last48HoursCount;
	}
	public void setLast24HoursCount(int last24HoursCount) {
		Last24HoursCount = last24HoursCount;
	}
	public int getLast24HoursCount() {
		return Last24HoursCount;
	}
	public void setRatio(int ratio) {
		this.ratio = ratio;
	}
	public int getRatio() {
		return ratio;
	}
	
	public String toString()
	{
		return themeID + ",Last48HoursCount:" + Last48HoursCount
		+ ",Last24HoursCount:" + Last24HoursCount+","+
		themeCount.toString();
	
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -332162680200011589L;
	private String themeID;
	private int Last48HoursCount;
	private int Last24HoursCount;
	private int ratio;
	private List<String> articleIDList;
	private List<CountByField> themeCount;
}





