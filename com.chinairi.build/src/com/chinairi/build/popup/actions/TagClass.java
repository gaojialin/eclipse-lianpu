package com.chinairi.build.popup.actions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

public class TagClass {
	private List<String> fields;
	private String tagName;
	
	// 根据tagName 获取属性数组
	
	
	
	
	
	
	public String[] getFields(String t) {
		fields = new ArrayList<>();
		// 创建一个DocumentBuilderFactory的对象
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// 创建DocumentBuilder对象
		try {
			String className = getTagClass("stringColumn");
			Class<?> c = Class.forName("com.chinairi.lianpu.tag.layout.datetable.column."+className+"Tag");
			for(Field f:FieldUtils.getAllFields(c)){
				fields.add(f.getName());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return fields.toArray(new String[]{});

	}
	private String getTagClass(String t) {
		if(StringUtils.isNotBlank(t)){
			return t.substring(0, 1).toUpperCase()+t.substring(1, t.length());
		}
		return t;
	}
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	
}
