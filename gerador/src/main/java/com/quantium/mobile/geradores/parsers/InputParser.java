package com.quantium.mobile.geradores.parsers;

import java.util.Collection;
import java.util.Map;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.geradores.GeneratorConfig;
import com.quantium.mobile.geradores.GeradorException;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;

public interface InputParser {

	Collection<JavaBeanSchema> getSchemas(
			GeneratorConfig information,
			Map<String, Object> defaultProperties)
			throws GeradorException;

	void generateSqlResources(
			GeneratorConfig config,
			Collection<Table> tables)
			throws GeradorException;
}
