package com.sinmn.core.model.core;

import org.springframework.core.annotation.AnnotationUtils;

import com.sinmn.core.model.annotation.Table;
import com.sinmn.core.model.dto.ModelWhere;
import com.sinmn.core.model.emun.ModelCondition;
import com.sinmn.core.model.emun.ModelOperator;
import com.sinmn.core.utils.util.ListUtil;
import com.sinmn.core.utils.util.StringUtil;

public class ModelSelect extends BaseModel{
	
	public ModelSelect(Class<?> clazz){
		super("","");
		Table table = AnnotationUtils.findAnnotation(clazz,Table.class);
		if(table != null){
			this.setTable(table.value());
			this.setPrefix(table.prefix());
		}
		
	}
	
	public ModelSelect(String table){
		super(table,"");
	}

	////////////////////////////////////////////////////////////////////////////////////////
		
	public ModelSelect limit(int size){
		this.getModelWhereExt().limit(size);
		return this;
	}

	public ModelSelect limit(int start,int size){
		this.getModelWhereExt().limit(start,size);
		return this;
	}

	public ModelSelect orderBy(String orderBy){
		this.getModelWhereExt().orderBy(orderBy);
		return this;
	}

	public ModelSelect groupBy(String groupBy){
		this.getModelWhereExt().groupBy(groupBy);
		return this;
	}
	
	public ModelSelect fields(String ...fields){
		this.setFields(ListUtil.join(fields));
		return this;
	}

	public ModelSelect where(ModelWhere mw){
		this.getModelWhere().add(mw);
		return this;
	}

	public ModelSelect where(ModelWhere mw,ModelCondition mc){
		this.getModelWhere().add(mw,mc);
		return this;
	}

	public ModelSelect where(String column,ModelOperator mo){
		return this.where(column, null,mo,null);
	}

	public ModelSelect where(String column,Object value){
		return this.where(column, value,null,null);
	}

	public ModelSelect where(String column,Object value,ModelOperator mo){
		return this.where(column, value,mo,null);
	}

	public ModelSelect where(String column,Object value,ModelCondition mc){
		return this.where(column, value,null,mc);
	}

	public ModelSelect where(String column,Object value,ModelOperator mo,ModelCondition mc){
		if(value == null && !mo.equals(ModelOperator.IS_NULL) && !mo.equals(ModelOperator.IS_NOT_NULL)){
			return this;
		}
		this.getModelWhere().add(column,value,mo,mc);
		return this;
	}
	
	public String sql(){
		return sql("");
	}
	
	public String sql(String prefix){
		if(StringUtil.isNotEmpty(prefix) && StringUtil.isNotEmpty(prefix.trim())){
			prefix = " " + prefix + " ";
		}else{
			prefix = "";
		}
		return prefix + "("+this.listSQL()+")";
	}
	
	public String sql(ModelOperator mo){
		return sql(mo.getOp());
	}

	/////////////////////////////////////////////////////////////////////
}
