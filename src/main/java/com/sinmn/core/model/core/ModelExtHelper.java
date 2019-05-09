package com.sinmn.core.model.core;

import com.sinmn.core.model.interfaces.IModel;
import com.sinmn.core.utils.util.FastJsonUtils;

public class ModelExtHelper {

	public static <T> T getModelExt(IModel<?> model,Class<T> clazz){
		Object result = model.get();
		if(result == null){
			return null;
		}
		Object ext = ModelHelper.getValue(result, "ext");
		if(ext == null){
			return null;
		}
		return FastJsonUtils.getBean(ext.toString(), clazz);
	}
}
