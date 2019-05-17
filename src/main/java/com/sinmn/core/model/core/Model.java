package com.sinmn.core.model.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.util.CollectionUtils;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import com.sinmn.core.model.annotation.Table;
import com.sinmn.core.model.dto.ModelJoin;
import com.sinmn.core.model.dto.ModelSet;
import com.sinmn.core.model.dto.ModelWhere;
import com.sinmn.core.model.dto.TableField;
import com.sinmn.core.model.emun.ModelCondition;
import com.sinmn.core.model.emun.ModelOperator;
import com.sinmn.core.model.exception.ModelNoWhereException;
import com.sinmn.core.model.interfaces.BaseModelDao;
import com.sinmn.core.model.interfaces.IModel;
import com.sinmn.core.utils.spring.SpringContextUtil;
import com.sinmn.core.utils.util.FastJsonUtils;
import com.sinmn.core.utils.util.IntUtil;
import com.sinmn.core.utils.util.ListUtil;
import com.sinmn.core.utils.util.LongUtil;
import com.sinmn.core.utils.util.MapUtil;
import com.sinmn.core.utils.util.StringUtil;

public class Model<T> extends BaseModel implements IModel<T>{
	
	private Class<T> clazz;
	private boolean bInit = false;
	private Object initLock = new Object();
	
	private BaseModelDao modelDao;
	
	public Model(String table,String prefix,Class<T> clazz){
		super(table,prefix);
		this.clazz = clazz;
	}
	
	public Model(String table,String prefix,Table annotationTable,Class<T> clazz,BaseModelDao modelDao){
		super(table,prefix);
		this.clazz = clazz;
		this.modelDao = modelDao;
		this.setAnnotationTable(annotationTable);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void init(){
		if(bInit){
			return;
		}
		synchronized (initLock) {
			if(bInit){
				return;
			}
			List<Map<String,Object>> mapFields = null;
			try{
				mapFields = this.query("SHOW FULL COLUMNS FROM `"+this.getTable()+"`");
			}catch(BadSqlGrammarException e){
				if(e.getCause() instanceof MySQLSyntaxErrorException){
					MySQLSyntaxErrorException ee = (MySQLSyntaxErrorException)e.getCause();
					if(ee.getErrorCode() == 1146){
						//table 不存在
						if(this.getAnnotationTable() != null && this.getAnnotationTable().create()){
							logger.debug("[Model.init] 表`{}`不存在，创建表",this.getTable());
							//如果该表为自动创建的话
							String createSQL = ModelHelper.getCreateSQL(this.getClazz());
							logger.debug("[Model.init] SQL 语句:{}",createSQL);
							query(createSQL);
							logger.debug("[Model.init] SQL 成功创建表:`{}`",this.getTable());
							//判断是否有初始化数据
							List initData = ModelHelper.getInitData(clazz);
							if(initData != null && initData.size() > 0){
								this.ignoreOnce(true).insert(initData);
							}else{
								init();
							}
							return;
						}
					}
				} else {
					logger.error("model error:", e);
				}
				
			}
			
			liTableField = ModelHelper.convert(mapFields, TableField.class);
			for(TableField tableField : liTableField){
				if("PRI".equalsIgnoreCase(tableField.getKey())){
					this.setPriKey(tableField.getField());
				}
				if("auto_increment".equalsIgnoreCase(tableField.getExtra())){
					this.setAutoIncrement(true);
				}
			}
			//判断列是否相等，找出不存在的列
			if(this.getAnnotationTable() != null && this.getAnnotationTable().create()){
				List<TableField> currentTableField = ModelHelper.getTableFields(this.getClazz());
				Map<String,TableField> mapTableField = MapUtil.toMapString(currentTableField, "field");
				for(TableField tableField : liTableField){
					mapTableField.remove(tableField.getField());
				}
				if(mapTableField.size() != 0){
					//设置新的field
					String sql = ModelHelper.getAddColumnSQL(this.getClazz(),mapTableField);
					logger.debug("[Model.init] SQL 语句:{}",sql);
					query(sql);
					init();
				}
			}
			bInit= true;
		}
	}
	
	@Override
	public List<T> list(){
		init();
		return list(this.getClazz());
	}
	
	@Override
	public List<T> list(Object key){
		init();
		if(key != null && key instanceof List){
			this.where(this.getPriKey(),key,ModelOperator.IN);
		}else{
			this.where(this.getPriKey(),"-999999",ModelOperator.IN);
		}
		return list(this.getClazz());
	}
	
	@Override
	public <T2> List<T2> list(Class<T2> clazz){
		init();
		if(this.getModelJoin() != null){
			return listJoin(clazz);
		}
		return ModelHelper.convert(query(listSQL()), clazz);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes"})
	private <T2> List<T2> listJoin(Class<T2> clazz){
		init();
		ModelJoin modelJoin = this.getModelJoin();
		
		List<Object> whereCondition = new ArrayList<Object>();
		
		List<Object> listSource = new ArrayList<Object>();
		if(!(modelJoin.getSource() instanceof List)){
			listSource.add(modelJoin.getSource());
		}else{
			listSource = (List)modelJoin.getSource();
		}
		
		for(Object o : listSource){
			whereCondition.add(ModelHelper.getValue(o, modelJoin.getSourceColumn()));
		}
		
		if(whereCondition.isEmpty()){
			this.clear();
			return new ArrayList<T2>();
		}
		
		this.include(modelJoin.getTargetColumn().keySet().toArray(new String[0]),modelJoin.getSourceColumnAlias())
		.where(modelJoin.getSourceColumnAlias(),whereCondition,ModelOperator.IN);
		
		List<Map<String,Object>> list = query(listSQL());
		
		Map<Object,Map<String,Object>> listMap = new HashMap<Object,Map<String,Object>>();
		
		for(Map<String,Object> l : list){
			listMap.put(l.get(modelJoin.getSourceColumnAlias()).toString(), l);
		}
		
		List<Map> mapSource = ModelHelper.convert(listSource, Map.class);
		
		for(Map s : mapSource){
			//获取realname
			String realName = ModelHelper.getAliasNameByAnnotation(listSource.get(0).getClass(), modelJoin.getSourceColumn());
			Object o = s.get(realName);
			if(o == null){
				continue;
			}
			Object tmp = listMap.get(o.toString());
			if(tmp == null){
				continue;
			}
			for(String key : modelJoin.getTargetColumn().keySet()){
				s.put(modelJoin.getTargetColumn().get(key), ModelHelper.getValue(tmp, key));
			}
		}
		
		return ModelHelper.convert(mapSource, clazz);
	}
	
	@Override
	public T get(){
		init();
		return get(this.getClazz());
	}
	
	@Override
	public T get(Object key){
		init();
		if(key != null){
			this.where(this.getPriKey(),key);
		}
		return this.get();
	}
	
	@Override
	public <T2> T2 get(Class<T2> clazz){
		init();
		if(this.getModelJoin() != null){
			List<T2> result = listJoin(clazz);
			if(result == null || result.isEmpty()){
				return null;
			}
			return result.get(0);
		}
		this.limit(1);
		
		List<T2> list = null;
		
		if(clazz.equals(this.getClazz())){
			list = ModelHelper.convert(query(listSQL()), clazz);
		}else{
			List<T> tmpList = ModelHelper.convert(query(listSQL()), this.getClazz());
			list = FastJsonUtils.getBeanList(FastJsonUtils.toJsonString(tmpList), clazz);
			tmpList.clear();
		}
		
		if(list == null || list.size() == 0){
			return null;
		}
		return list.get(0);
	}
	
	@Override
	public boolean isExists(){
		return this.count() != 0;
	}
	
	@Override
	public boolean isNotExists(){
		return this.count() == 0;
	}
	
	@Override
	public boolean isExists(Object value){
		return this.count(value) != 0;
	}
	
	@Override
	public boolean isNotExists(Object value){
		return this.count(value) == 0;
	}
	
	@Override
	public long count(Object key){
		init();
		if(key != null){
			this.where(this.getPriKey(),key);
		}
		return count();
	}
	
	@Override
	public long count(){
		this.fields("count(1)");
		return this.getLong();
	}
	
	@Override
	public <T2> List<T2> listSingle(Class<T2> clazz){
		init();
		List<Map<String,Object>> list = query(this.listSQL());
		List<T2> result = new ArrayList<T2>();
		for(Map<String,Object> m : list){
			for(String key : m.keySet()){
				result.add(ModelHelper.convertSingle(m.get(key), clazz));
				break;
			}
		}
		return result;
	}
	
	@Override
	public <T2> T2 getSingle(Class<T2> clazz){
		this.limit(1);
		List<T2> list = listSingle(clazz);
		if(CollectionUtils.isEmpty(list)){
			return null;
		}
		return list.get(0);
	}
	
	@Override
	public String getString(Object value){
		this.where(this.getPriKey(),value);
		return getSingle(String.class);
	}
	
	@Override
	public String getString(){
		return getSingle(String.class);
	}
	
	@Override
	public Integer getInteger(Object value){
		this.where(this.getPriKey(),value);
		return getSingle(Integer.class);
	}
	
	@Override
	public Integer getInteger(){
		return getSingle(Integer.class);
	}
	
	@Override
	public Long getLong(Object value){
		this.where(this.getPriKey(),value);
		return getSingle(Long.class);
	}
	
	@Override
	public Long getLong(){
		return getSingle(Long.class);
	}
	
	@Override
	public List<String> listString(){
		return listSingle(String.class);
	}
	
	@Override
	public List<Integer> listInteger(){
		return listSingle(Integer.class);
	}
	
	@Override
	public List<Long> listLong(){
		return listSingle(Long.class);
	}
	
	@Override
	public Model<T> ignoreOnce(boolean ignoreOnce){
		this.setIgnoreOnce(ignoreOnce);
		return this;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Model<T> limit(int size){
		this.getModelWhereExt().limit(size);
		return this;
	}
	
	@Override
	public Model<T> limit(int start,int size){
		this.getModelWhereExt().limit(start,size);
		return this;
	}
	
	@Override
	public Model<T> distinct(){
		this.setDistinct(true);
		return this;
	}
	
	@Override
	public Model<T> orderBy(String orderBy){
		this.getModelWhereExt().orderBy(orderBy);
		return this;
	}
	
	@Override
	public Model<T> orderBy(String...args){
		List<String> orderBy = new ArrayList<String>();
		String key = null;
		for(String arg : args){
			if(key == null){
				key = arg;
				if(key.indexOf("`") == -1){
					key = "`"+key+"`";
				}
				continue;
			}
			orderBy.add(key+" "+arg);
			key = null;
		}
		if(key != null){
			orderBy.add(key+" ASC");
		}
		this.getModelWhereExt().orderBy(ListUtil.join(orderBy));
		return this;
	}
	
	@Override
	public Model<T> groupBy(String groupBy){
		this.getModelWhereExt().groupBy(groupBy);
		return this;
	}
	
	@Override
	public Model<T> fields(String fields){
		this.setFields(fields);
		return this;
	}
	
	@Override
	public Model<T> include(String fields){
		this.setFields(fields);
		return this;
	}
	
	@Override
	public Model<T> include(String ...args){
		this.setFields(ListUtil.join(args,","));
		return this;
	}
	
	@Override
	public Model<T> include(String[] arrArgs,String ...args){
		String fields = ListUtil.join(arrArgs,",");
		if(args.length != 0){
			fields+=","+ListUtil.join(args,",");
		}
		this.setFields(fields);
		return this;
	}
	
	@Override
	public Model<T> exclude(String fields){
		this.setExcludeFields(fields);
		return this;
	}
	
	@Override
	public Model<T> exclude(String ...args){
		this.setExcludeFields(ListUtil.join(args,","));
		return this;
	}
	
	@Override
	public Model<T> exclude(String[] arrArgs,String ...args){
		String excludeFields = ListUtil.join(arrArgs,",");
		if(args.length != 0){
			excludeFields+=","+ListUtil.join(args,",");
		}
		this.setExcludeFields(excludeFields);
		return this;
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// where start
	//////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public Model<T> or(String column,Object value){
		return this.where(column, value,ModelCondition.OR);
	}
	
	@Override
	public Model<T> and(String column,Object value){
		return this.where(column, value,ModelCondition.AND);
	}
	
	@Override
	public Model<T> where(String column,Object value){
		return this.where(column, value,null,null);
	}
	
	@Override
	public Model<T> where(String column,Object value,ModelOperator mo){
		return this.where(column, value,mo,null);
	}

	@Override
	public Model<T> where(String column,Object value,ModelCondition mc){
		return this.where(column, value,null,mc);
	}

	@Override
	public Model<T> where(ModelWhere mw){
		this.getModelWhere().add(mw);
		return this;
	}

	@Override
	public Model<T> where(Object value){
		this.init();
		return this.where(this.getPriKey(),value);
	}

	@Override
	public Model<T> where(ModelWhere mw,ModelCondition mc){
		this.getModelWhere().add(mw,mc);
		return this;
	}

	@Override
	public Model<T> where(String column,ModelOperator mo){
		return this.where(column, null,mo,null);
	}

	@Override
	public Model<T> where(String column,Object value,ModelOperator mo,ModelCondition mc){
		if(value == null && !ModelOperator.IS_NULL.equals(mo) && !ModelOperator.IS_NOT_NULL.equals(mo)){
			return this;
		}
		this.getModelWhere().add(column,value,mo,mc);
		return this;
	}

	@Override
	public Model<T> whereFormat(String whereStr,Object ...args){
		this.getModelWhere().addFormat(whereStr,args);
		return this;
	}

	@Override
	public Model<T> whereFormat(ModelCondition mc,String whereStr,Object ...args){
		this.getModelWhere().addFormat(mc,whereStr,args);
		return this;
	}

	@Override
	public Model<T> whereSQL(String sql){
		this.getModelWhere().setWhereString(sql);
		return this;
	}
	
	/////////////////////////////////////////////////////////////////////
	// where end
	/////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////////
	// join start
	/////////////////////////////////////////////////////////////////////

	@Override
	public Model<T> join(Object source,String sourceColumn,String sourceColumnAlias,String ...targetColumn){
		ModelJoin modelJoin = new ModelJoin(); 
		modelJoin.setSource(source);
		modelJoin.setSourceColumn(sourceColumn);
		modelJoin.setSourceColumnAlias(sourceColumnAlias);
		Map<String,String> targetColumnMap = new HashMap<String,String>();
		String key = null;
		for(String strTargetColumn : targetColumn){
			if(key == null){
				key = strTargetColumn;
			}else{
				targetColumnMap.put(key, strTargetColumn);
				key = null;
			}
		}
		if(targetColumn.length % 2 == 1){
			targetColumnMap.put(key, key);
		}
		modelJoin.setTargetColumn(targetColumnMap);
		this.setModelJoin(modelJoin);
		return this;
	}
	
	/////////////////////////////////////////////////////////////////////
	// join end
	/////////////////////////////////////////////////////////////////////
	
	
	/////////////////////////////////////////////////////////////////////
	// insert
	/////////////////////////////////////////////////////////////////////

	@Override
	public String insertSQL(Object insertModel,boolean ignorePriKey){
		
		String fields = getFields().trim();
		if(fields.equals("*")){
			fields = "";
		}
		List<String> includeFields = new ArrayList<String>();
		for(String field : fields.split(",")){
			if(StringUtil.isEmpty(field)){
				continue;
			}
			includeFields.add(field.replaceAll("`", ""));
		}
		
		String sql = String.format("INSERT INTO `%s` %s VALUES %s",this.getTable(),
				ModelHelper.getInsertColumn(insertModel,includeFields, liTableField, ignorePriKey),
				ModelHelper.getInsertValue(insertModel,includeFields, liTableField, ignorePriKey)
				);
		this.clear();
		return sql;
	}
	
	@Override
	public long insert(Object insertModel,boolean ignorePriKey){
		init();
		long result =  LongUtil.toLong(query(insertSQL(insertModel,ignorePriKey)).get(0).get("affactRow"));
		result = 1;
		if(result > 0 && this.getAutoIncrement()){
			//获取lastInsertId
			long lastInsertId = LongUtil.toLong(query("SELECT LAST_INSERT_ID() as id").get(0).get("id"));
			ModelHelper.setLastInsertId(insertModel, this.getPriKey(), lastInsertId);
		}
		
		return result;
	}
	
	@Override
	public long insert(Object insertModel){
		return insert(insertModel,this.getAutoIncrement());
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String updateSQL(Object updateModel,String fields,boolean cleanWhere,boolean ignorePriKey){
		String sql = "";
		if(fields.equals("*")){
			fields = "";
		}
		if(!this.hasWhere()){
			Object priValue = ModelHelper.getValue(updateModel, this.getPriKey());
			if(priValue == null){
				priValue = ModelHelper.getValue(updateModel, "_value");
				if(priValue == null){
					throw new ModelNoWhereException("请传入更新条件");
				}else{
					this.where(ModelHelper.getValue(updateModel, "_column").toString(), priValue);
					cleanWhere = true;
				}
			}else{
				this.where(this.getPriKey(), priValue);
				cleanWhere = true;
			}
		}
		List<String> includeField = new ArrayList<String>();
		for(String field : fields.split(",")){
			if(StringUtil.isEmpty(field)){
				continue;
			}
			includeField.add(field.replaceAll("`", ""));
		}
		
		sql = String.format("UPDATE `%s` SET %s %s", this.getTable(),
				ModelHelper.getSetSQL(updateModel,includeField,this.liTableField,ignorePriKey),
				this.getModelWhereString());
		if(cleanWhere){
			this.clear();
		}
		return sql;
	}
	
	@Override
	public String updateSQL(Object updateModel){
		return updateSQL(updateModel,true);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public String updateSQL(Object updateModel,boolean ignorePriKey){
		String fields = getFields().trim();
		if(updateModel instanceof List){
			List<String> liSQL = new ArrayList<String>();
			for(Object o : (List)updateModel){
				if(o instanceof ModelSet){
					o = ((ModelSet)o).get();
				}
				String sql = this.updateSQL(o,fields,false,ignorePriKey);
				liSQL.add(sql);
			}
			this.clear();
			return ListUtil.join(liSQL,";");
		}else{
			return this.updateSQL(updateModel,fields,true,ignorePriKey);
		}
	}

	@Override
	public Model<T> sub(String column,Object value){
		this.getUpdateModel().add(column, value,ModelOperator.SUB);
		return this;
	}

	@Override
	public Model<T> add(String column,Object value){
		this.getUpdateModel().add(column, value,ModelOperator.ADD);
		return this;
	}

	@Override
	public long update(){
		if(this.getUpdateModel().isEmpty()){
			this.clear();
			return 0;
		}
		return this.update(this.getUpdateModel());
	}

	@Override
	public long update(String column,Object value){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put(column, value);
		return update(map);
	}

	@Override
	public long update(ModelSet updateModel){
		return update(updateModel.get());
	}

	@Override
	public long update(Object updateModel){
		init();
		long result =  LongUtil.toLong(query(updateSQL(updateModel)).get(0).get("affactRow"));
		return result;
	}
	
	@Override
	public String deleteSQL(){
		String sql = String.format("DELETE FROM `%s` %s", this.getTable(),
				this.getModelWhereString());
		this.clear();
		return sql;
	}
	
	//保存
	@Override
	public long save(Object saveModel){
		if(StringUtil.isEmpty(this.getPriKey())){
			return this.insert(saveModel);
		}
		Object key = ModelHelper.getValue(saveModel, this.getPriKey());
		if(key == null){
			return this.insert(saveModel);
		}else{
			return this.update(saveModel);
		}
	}
	
	@Override
	public long delete(Object value){
		this.init();
		if(value instanceof List){
			this.where(this.getPriKey(),value,ModelOperator.IN);
		}else{
			this.where(this.getPriKey(),value);
		}
		return delete();
	}
	
	@Override
	public long delete(){
		init();
		if(!this.hasWhere()){
			throw new ModelNoWhereException("请传入删除条件，防止误删");
		}
		int result =  IntUtil.toInt(query(deleteSQL()).get(0).get("affactRow"));
		return result;
	}

	
	////////////////////////////////////////////////////////
	@Override
	public List<Map<String,Object>> query(String sql) {
		logger.debug(sql);
		
		long currentTime = System.currentTimeMillis();
		while(SpringContextUtil.getContext() == null && modelDao == null){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			if(System.currentTimeMillis() - currentTime > 60 * 60 * 1000){
				logger.error("请先配置SpringContextUtil");
				return null;
			}
			continue;
		}
		
		if(modelDao == null && SpringContextUtil.getContext() != null){
			modelDao = (BaseModelDao) SpringContextUtil
					.getBean(this.getResource());
		}
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		if (sql.toLowerCase().indexOf("select ") == 0) {
			return modelDao.select(sql);
		} else if (sql.toLowerCase().indexOf("insert ") == 0) {
		    Map<String,Object> affactRow = new HashMap<String,Object>();
		    affactRow.put("affactRow", modelDao.insert(sql));
		    result.add(affactRow);
		} else if (sql.toLowerCase().indexOf("update ") == 0) {
		    Map<String,Object> affactRow = new HashMap<String,Object>();
            affactRow.put("affactRow", modelDao.update(sql));
            result.add(affactRow);
		} else if (sql.toLowerCase().indexOf("delete ") == 0) {
		    Map<String,Object> affactRow = new HashMap<String,Object>();
            affactRow.put("affactRow", modelDao.delete(sql));
            result.add(affactRow);
		}else{
			return modelDao.select(sql);
		}
		return result;
	}

	protected Class<T> getClazz() {
		return clazz;
	}

	protected void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@Override
	public ModelSelect createSelect(){
		return new ModelSelect(this.clazz);
	}

	@Override
	public void setModelDao(BaseModelDao modelDao) {
		this.modelDao = modelDao;
	}
	
	
}
