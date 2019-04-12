package com.sinmn.core.model.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sinmn.core.model.annotation.ModelAutowiredAnnotationBeanPostProcessor;

@Configuration
public class ModelAutowiredConfiguration {
   
    //支持自定义注解  ModelAutowired
    @Bean
    public ModelAutowiredAnnotationBeanPostProcessor modelAutowiredAnnotationBeanPostProcessor(){
    	return new ModelAutowiredAnnotationBeanPostProcessor();
    }
}
