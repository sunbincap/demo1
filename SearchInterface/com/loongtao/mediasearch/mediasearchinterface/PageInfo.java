package com.loongtao.mediasearch.mediasearchinterface;

import java.io.Serializable;

public class PageInfo implements Serializable  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1274690669841785265L;
	private int totalPages = 0; // 总页数
	private int curPageNo = 0;  //   当前页数
	private int totalRecords = 0; 
	// 文章总数  默认为0表示排重    -1 表示不排重
	// -2  表示列表返回Score ，格式为 article_id-score[-dupCount]
	//
	private int pageSize = 0;    //      页面文章条数
	private float timeConsum = (float) 0.0;    //耗时
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
	public int getTotalPages() {
		return totalPages;
	}
	public void setCurPageNo(int curPageNo) {
		this.curPageNo = curPageNo;
	}
	public int getCurPageNo() {
		return curPageNo;
	}
	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}
	public int getTotalRecords() {
		return totalRecords;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setTimeConsum(float timeConsum) {
		this.timeConsum = timeConsum;
	}
	public float getTimeConsum() {
		return (float)this.timeConsum;
	}
	public PageInfo()
	{
		
	}
	public PageInfo(int pageSize)
	{
		this.pageSize=pageSize;	
	}
	
}
