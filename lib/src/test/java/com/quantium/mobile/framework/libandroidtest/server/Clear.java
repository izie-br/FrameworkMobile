package com.quantium.mobile.framework.libandroidtest.server;

public class Clear extends BaseServerBean {

	private String classname;

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getResponse() {
		setAttribute(classname, null);
		return "";
	}

}
