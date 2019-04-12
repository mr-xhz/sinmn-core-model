package com.sinmn.core.model.interfaces;

import java.util.List;
import java.util.Map;

import com.sinmn.core.model.core.ModelSelect;
import com.sinmn.core.model.dto.ModelSet;
import com.sinmn.core.model.dto.ModelWhere;
import com.sinmn.core.model.emun.ModelCondition;
import com.sinmn.core.model.emun.ModelOperator;

public interface IModel<T> {

	void init();
	
	List<T> list();
	
	List<T> list(Object key);
	
	<T2> List<T2> list(Class<T2> clazz);
	
	T get();
	
	T get(Object key);
	
	<T2> T2 get(Class<T2> clazz);
	
	boolean isExists();
	
	boolean isNotExists();
	
	boolean isExists(Object value);
	
	boolean isNotExists(Object value);
	
	long count(Object key);
	
	long count();
	
	<T2> List<T2> listSingle(Class<T2> clazz);
	
	<T2> T2 getSingle(Class<T2> clazz);
	
	String getString(Object value);
	
	String getString();
	
	Integer getInteger(Object value);
	
	Integer getInteger();
	
	Long getLong(Object value);
	
	Long getLong();
	
	List<String> listString();
	
	List<Integer> listInteger();
	
	List<Long> listLong();
	
	IModel<T> ignoreOnce(boolean ignoreOnce);
	
	////////////////////////////////////////////////////////////////////////////////////////
	IModel<T> limit(int size);
	
	IModel<T> limit(int start,int size);
	
	IModel<T> distinct();
	
	IModel<T> orderBy(String orderBy);
	
	IModel<T> orderBy(String...args);
	
	IModel<T> groupBy(String groupBy);
	
	IModel<T> fields(String fields);
	
	IModel<T> include(String fields);
	
	IModel<T> include(String ...args);
	
	IModel<T> include(String[] arrArgs,String ...args);
	
	IModel<T> exclude(String fields);
	
	IModel<T> exclude(String ...args);
	
	IModel<T> exclude(String[] arrArgs,String ...args);
	
	//////////////////////////////////////////////////////////////////////////////////
	// where start
	//////////////////////////////////////////////////////////////////////////////////
	
	IModel<T> or(String column,Object value);
	
	IModel<T> and(String column,Object value);
	
	
	
	IModel<T> where(String column,Object value);
	
	IModel<T> where(String column,Object value,ModelOperator mo);
	
	IModel<T> where(String column,Object value,ModelCondition mc);
	
	IModel<T> where(ModelWhere mw);
	
	IModel<T> where(Object value);
	
	IModel<T> where(ModelWhere mw,ModelCondition mc);
	
	IModel<T> where(String column,ModelOperator mo);
	
	IModel<T> where(String column,Object value,ModelOperator mo,ModelCondition mc);
	
	IModel<T> whereFormat(String whereStr,Object ...args);
	
	IModel<T> whereFormat(ModelCondition mc,String whereStr,Object ...args);
	
	IModel<T> whereSQL(String sql);
	
	/////////////////////////////////////////////////////////////////////
	// where end
	/////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////////
	// join start
	/////////////////////////////////////////////////////////////////////
	
	IModel<T> join(Object source,String sourceColumn,String sourceColumnAlias,String ...targetColumn);
	
	/////////////////////////////////////////////////////////////////////
	// join end
	/////////////////////////////////////////////////////////////////////
	
	
	/////////////////////////////////////////////////////////////////////
	// insert
	/////////////////////////////////////////////////////////////////////
	
	public String insertSQL(Object insertModel,boolean ignorePriKey);
	
	long insert(Object insertModel,boolean ignorePriKey);
	
	long insert(Object insertModel);
	
	IModel<T> sub(String column,Object value);
	
	IModel<T> add(String column,Object value);
	
	String updateSQL(Object updateModel,String fields,boolean cleanWhere,boolean ignorePriKey);
	
	String updateSQL(Object updateModel);
	
	String updateSQL(Object updateModel,boolean ignorePriKey);
	
	long update();
	
	long update(String column,Object value);
	
	long update(ModelSet updateModel);
	
	long update(Object updateModel);
	
	//保存
	long save(Object saveModel);
	
	String deleteSQL();
	
	long delete(Object value);
	
	long delete();

	
	////////////////////////////////////////////////////////
	List<Map<String,Object>> query(String sql);
	
	ModelSelect createSelect();

	void setModelDao(BaseModelDao modelDao);
}
