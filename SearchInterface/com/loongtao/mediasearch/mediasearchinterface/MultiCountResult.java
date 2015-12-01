package com.loongtao.mediasearch.mediasearchinterface;

import java.io.Serializable;
import java.util.List;

public class MultiCountResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2394571151273849016L;
	
	private String mainField;
	public MultiCountResult(String mainField2) {
		mainField = mainField2;
	}


	public String getMainField() {
		return mainField;
	}


	public void setMainField(String mainField) {
		this.mainField = mainField;
	}


	public String getSubField() {
		return subField;
	}


	public void setSubField(String subField) {
		this.subField = subField;
	}


	public List<CountByField> getMultiCountByField() {
		return multiCountByField;
	}


	public void setMultiCountByField(List<CountByField> multiCountByField) {
		this.multiCountByField = multiCountByField;
	}


	private String subField;
	
	private int totalCount = 0;
	private List<CountByField> multiCountByField;
	
	
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}


	public int getTotalCount() {
		return totalCount;
	}


	public String toString()
	{
		return "[" + mainField + " = " + multiCountByField.toString() + "]";
	}
   
}
