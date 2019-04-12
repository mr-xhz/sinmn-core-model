package com.sinmn.core.model.core;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinmn.core.model.dto.ModelSet;
import com.sinmn.core.model.dto.ModelWhere;
import com.sinmn.core.model.emun.ModelCondition;
import com.sinmn.core.model.emun.ModelOperator;
import com.sinmn.core.model.interfaces.BaseModelDao;
import com.sinmn.core.model.interfaces.IModel;

public abstract class AbstractModelRepository<T> implements IModel<T>{
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	protected abstract Model<T> getModel();
	
	@Override
	public List<T> list(){
		return getModel().list();
	}
	
	@Override
	public List<T> list(Object key){
		return getModel().list(key);
	}
	
	@Override
	public <T2> List<T2> list(Class<T2> clazz){
		return getModel().list(clazz);
	}
	
	@Override
	public T get(){
		return getModel().get();
	}
	
	@Override
	public T get(Object key){
		return getModel().get(key);
	}
	
	@Override
	public <T2> T2 get(Class<T2> clazz){
		return getModel().get(clazz);
	}
	
	@Override
	public boolean isExists(){
		return getModel().isExists();
	}
	
	@Override
	public boolean isNotExists(){
		return getModel().isNotExists();
	}
	
	@Override
	public boolean isExists(Object value){
		return getModel().isExists(value);
	}
	
	@Override
	public boolean isNotExists(Object value){
		return getModel().isNotExists(value);
	}
	
	@Override
	public long count(){
		return getModel().count();
	}
	
	@Override
	public <T2> List<T2> listSingle(Class<T2> clazz){
		return getModel().listSingle(clazz);
	}
	
	@Override
	public <T2> T2 getSingle(Class<T2> clazz){
		return getModel().getSingle(clazz);
	}
	
	@Override
	public String getString(Object value){
		return getModel().getString(value);
	}
	
	@Override
	public String getString(){
		return getModel().getString();
	}
	
	@Override
	public Integer getInteger(Object value){
		return getModel().getInteger(value);
	}
	
	@Override
	public Integer getInteger(){
		return getModel().getInteger();
	}
	
	@Override
	public List<String> listString(){
		return getModel().listString();
	}
	
	@Override
	public List<Integer> listInteger(){
		return getModel().listInteger();
	}
	
	@Override
	public Model<T> ignoreOnce(boolean ignoreOnce){
		return getModel().ignoreOnce(ignoreOnce);
	}
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Model<T> limit(int size){
		return getModel().limit(size);
	}

	@Override
	public Model<T> limit(int start,int size){
		return getModel().limit(start,size);
	}

	@Override
	public Model<T> distinct(){
		return getModel().distinct();
	}

	@Override
	public Model<T> orderBy(String orderBy){
		return getModel().orderBy(orderBy);
	}

	@Override
	public Model<T> orderBy(String...args){
		return getModel().orderBy(args);
	}

	@Override
	public Model<T> groupBy(String groupBy){
		return getModel().groupBy(groupBy);
	}

	@Override
	public Model<T> fields(String fields){
		return getModel().fields(fields);
	}

	@Override
	public Model<T> include(String fields){
		return getModel().include(fields);
	}

	@Override
	public Model<T> include(String ...args){
		return getModel().include(args);
	}

	@Override
	public Model<T> include(String[] arrArgs,String ...args){
		return getModel().include(arrArgs,args);
	}

	@Override
	public Model<T> exclude(String fields){
		return getModel().exclude(fields);
	}

	@Override
	public Model<T> exclude(String ...args){
		return getModel().exclude(args);
	}

	@Override
	public Model<T> exclude(String[] arrArgs,String ...args){
		return getModel().exclude(arrArgs,args);
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// where start
	//////////////////////////////////////////////////////////////////////////////////

	@Override
	public Model<T> or(String column,Object value){
		return getModel().or(column,value);
	}

	@Override
	public Model<T> and(String column,Object value){
		return getModel().and(column,value);
	}
	
	

	@Override
	public Model<T> where(String column,Object value){
		return getModel().where(column,value);
	}

	@Override
	public Model<T> where(String column,Object value,ModelOperator mo){
		return getModel().where(column,value,mo);
	}

	@Override
	public Model<T> where(String column,Object value,ModelCondition mc){
		return getModel().where(column,value,mc);
	}

	@Override
	public Model<T> where(ModelWhere mw){
		return getModel().where(mw);
	}

	@Override
	public Model<T> where(Object value){
		return getModel().where(value);
	}

	@Override
	public Model<T> where(ModelWhere mw,ModelCondition mc){
		return getModel().where(mw,mc);
	}

	@Override
	public Model<T> where(String column,ModelOperator mo){
		return getModel().where(column,mo);
	}

	@Override
	public Model<T> where(String column,Object value,ModelOperator mo,ModelCondition mc){
		return getModel().where(column,value,mo,mc);
	}

	@Override
	public Model<T> whereFormat(String whereStr,Object ...args){
		return getModel().whereFormat(whereStr,args);
	}

	@Override
	public Model<T> whereFormat(ModelCondition mc,String whereStr,Object ...args){
		return getModel().whereFormat(mc,whereStr,args);
	}

	@Override
	public Model<T> whereSQL(String sql){
		return getModel().whereSQL(sql);
	}
	
	/////////////////////////////////////////////////////////////////////
	// where end
	/////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////////
	// join start
	/////////////////////////////////////////////////////////////////////

	@Override
	public Model<T> join(Object source,String sourceColumn,String sourceColumnAlias,String ...targetColumn){
		return getModel().join(source,sourceColumn,sourceColumnAlias,targetColumn);
	}
	
	/////////////////////////////////////////////////////////////////////
	// join end
	/////////////////////////////////////////////////////////////////////
	
	
	/////////////////////////////////////////////////////////////////////
	// insert
	/////////////////////////////////////////////////////////////////////

	@Override
	public long insert(Object insertModel,boolean ignorePriKey){
		return getModel().insert(insertModel,ignorePriKey);
	}

	@Override
	public long insert(Object insertModel){
		return getModel().insert(insertModel);
	}

	@Override
	public Model<T> sub(String column,Object value){
		return getModel().sub(column,value);
	}

	@Override
	public Model<T> add(String column,Object value){
		return getModel().add(column,value);
	}

	@Override
	public long update(){
		return getModel().update();
	}

	@Override
	public long update(String column,Object value){
		return getModel().update(column,value);
	}

	@Override
	public long update(ModelSet updateModel){
		return getModel().update(updateModel);
	}

	@Override
	public long update(Object updateModel){
		return getModel().update(updateModel);
	}
	
	//保存
	@Override
	public long save(Object saveModel){
		return getModel().save(saveModel);
	}

	@Override
	public long delete(Object value){
		return getModel().delete(value);
	}

	@Override
	public long delete(){
		return getModel().delete();
	}

	
	////////////////////////////////////////////////////////
	@Override
	public List<Map<String,Object>> query(String sql) {
		return getModel().query(sql);
	}

	@Override
	public ModelSelect createSelect(){
		return getModel().createSelect();
	}

	@Override
	public void init(){
		getModel().init();
	}

	@Override
	public long count(Object key) {
		return getModel().count(key);
	}

	@Override
	public Long getLong(Object value) {
		return getModel().getLong(value);
	}

	@Override
	public Long getLong() {
		return getModel().getLong();
	}

	@Override
	public List<Long> listLong() {
		return getModel().listLong();
	}

	@Override
	public String insertSQL(Object insertModel, boolean ignorePriKey) {
		return getModel().insertSQL(insertModel,ignorePriKey);
	}

	@Override
	public String updateSQL(Object updateModel, String fields, boolean cleanWhere, boolean ignorePriKey) {
		return getModel().updateSQL(updateModel,fields,cleanWhere,ignorePriKey);
	}

	@Override
	public String updateSQL(Object updateModel) {
		return getModel().updateSQL(updateModel);
	}

	@Override
	public String updateSQL(Object updateModel, boolean ignorePriKey) {
		return getModel().updateSQL(updateModel,ignorePriKey);
	}

	@Override
	public String deleteSQL() {
		return getModel().deleteSQL();
	}

	@Override
	public void setModelDao(BaseModelDao modelDao) {
		getModel().setModelDao(modelDao);
		
	}
}
