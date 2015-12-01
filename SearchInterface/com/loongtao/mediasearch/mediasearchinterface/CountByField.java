package com.loongtao.mediasearch.mediasearchinterface;

import java.io.Serializable;

// 序列化版本号
// 属性设置

public class CountByField implements Serializable {
  
	/**
	 * 
	 */
	private static final long serialVersionUID = 7083749650873282994L;
	public CountByField(String _fieldValue, String _count) {
		// TODO Auto-generated constructor stub
    	setFieldValue(_fieldValue);
    	count = Integer.parseInt(_count);
	}
	public CountByField(String _fieldValue, int count) {
		super();
		this.setFieldValue(_fieldValue);
		this.count = count;
	}
	
	public String toString()
	{
		return fieldValue.toString()+":" + count;
	}
 
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}

	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}
	public String getFieldValue() {
		return fieldValue;
	}

	private String fieldValue;
	private int count;
}
