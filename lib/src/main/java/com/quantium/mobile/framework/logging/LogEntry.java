package com.quantium.mobile.framework.logging;

public interface LogEntry extends Cloneable{

	public static final String LEVEL_ERROR = "erro";
	public static final String LEVEL_INFO = "info";


	boolean save();
	LogEntry clone();
	void log(String level, String message);

}
