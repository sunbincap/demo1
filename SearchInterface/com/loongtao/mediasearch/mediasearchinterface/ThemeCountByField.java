package com.loongtao.mediasearch.mediasearchinterface;

import java.io.Serializable;
import java.util.List;

/**
 * 选定某个主题， 对该主题进行时间分布，媒体等方面的统计
 * themeID 主题ID
 * ThemeCountByField　统计结果
 */

public class ThemeCountByField implements Serializable {
	private static final long serialVersionUID = -4004264144852278687L;
	private String themeID;
	public String getThemeID() {
		return themeID;
	}
	public void setThemeID(String themeID) {
		this.themeID = themeID;
	}
	public void setThemeCountByField(List<CountByField> themeCountByField) {
		this.themeCountByField = themeCountByField;
	}
	public List<CountByField> getThemeCountByField() {
		return themeCountByField;
	}
	
	public String toString()
	{
		return "[" + themeID +"," + themeCountByField.toString() + "]";
	}
	
	private List<CountByField> themeCountByField;
	
}
