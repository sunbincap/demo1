package com.loongtao.mediasearch.mediasearchinterface;

import java.io.Serializable;

public class ArticleCountByTheme implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8400164807835541395L;
	public String getThemeID() {
		return themeID;
	}
	public void setThemeID(String themeID) {
		this.themeID = themeID;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	private String themeID;
	private int count;

}
