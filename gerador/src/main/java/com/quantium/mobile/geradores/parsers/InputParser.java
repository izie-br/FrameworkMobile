package com.quantium.mobile.geradores.parsers;

import java.util.Collection;
import java.util.Map;

import com.quantium.mobile.geradores.GeneratorConfig;
import com.quantium.mobile.geradores.GeradorException;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.tabelaschema.TabelaSchema;

public interface InputParser {

	Collection<JavaBeanSchema> getSchemas(
			GeneratorConfig information,
			Map<String, Object> defaultProperties)
			throws GeradorException;

	void generateSqlResources(
			GeneratorConfig config,
			Collection<TabelaSchema> tables)
			throws GeradorException;
}
