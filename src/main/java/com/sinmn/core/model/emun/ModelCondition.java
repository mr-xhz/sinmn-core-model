package com.sinmn.core.model.emun;

public enum ModelCondition {
	/** 与*/
	AND,
	/** 或*/
	OR;
	
	public String getCondition()
	{
		return " "+this.toString()+" ";
	}
}
