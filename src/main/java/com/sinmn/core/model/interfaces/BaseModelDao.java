package com.sinmn.core.model.interfaces;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface BaseModelDao {

	public List<Map<String,Object>> select(@Param("paramSQL")String sql);
	
	public long insert(@Param("paramSQL")String sql);
	
	public long update(@Param("paramSQL")String sql);
	
	public long delete(@Param("paramSQL")String sql);
}
