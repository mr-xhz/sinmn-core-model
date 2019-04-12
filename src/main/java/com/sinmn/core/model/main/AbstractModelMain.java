package com.sinmn.core.model.main;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;

import com.sinmn.core.model.core.Model;
import com.sinmn.core.utils.util.BeanUtil;

public abstract class AbstractModelMain implements InitializingBean{

	@Override
	public void afterPropertiesSet() throws Exception {
		final List<Field> fields = BeanUtil.getAllField(this.getClass());
		final Object self = this;
		//系统表
		new Thread(new Runnable() {
			@Override
			public void run() {
				for(Field field : fields){
					if(!field.getType().equals(Model.class)){
						continue;
					}
					try {
						PropertyDescriptor pd = new PropertyDescriptor(field.getName(),self.getClass());
						Method method = pd.getReadMethod();
						Object o = method.invoke(self);
						if(o != null && o instanceof Model){
							((Model)o).init();
						}
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			}
		}).start();
		
	}
}
