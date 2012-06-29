package com.quantium.mobile.framework.logging;

public interface LogEntry extends Cloneable{

	String LEVEL_ERROR = "erro";
	String LEVEL_INFO = "info";


	boolean save();
	LogEntry clone();
	void log(String level, String message);

}
