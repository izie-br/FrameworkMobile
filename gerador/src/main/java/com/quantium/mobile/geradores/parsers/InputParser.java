package com.quantium.mobile.geradores.parsers;

import java.util.Collection;
import java.util.Map;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.geradores.GeneratorConfig;
import com.quantium.mobile.geradores.GeradorException;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;

/**
 * <p>
 *   Usa o GeneratorConfig de entrada para criar os modelos (entidades)
 *   a partir de um certo tipo de entrada.
 * </p>
 * <p>Adicionalmente, produz scripts SQL, dependendo do tipo de entrada</p>
 * 
 * @author Igor Soares
 *
 */
public interface InputParser {

	/**
	 * Buscao arquivo de entrada de {@link GeneratorConfig#getInputFile()}
	 * e extrai dele os JavaBeanSchemas dos modelos.
	 * 
	 * @param information
	 * @param defaultProperties
	 * @return schemas
	 * @throws GeradorException
	 */
	Collection<JavaBeanSchema> getSchemas(
			GeneratorConfig config,
			Map<String, Object> defaultProperties)
			throws GeradorException;

	/**
	 * <p>Cria scripts do esquema SQL relacionado ao modelo atual.</p>
	 * <p>
	 *   Cada implementacao tem uma estrategia que eh baseada no
	 *   tipo de entrada.
	 * </p>
	 * 
	 * @param config
	 * @param tables
	 * @throws GeradorException
	 */
	void generateSqlResources(
			GeneratorConfig config,
			Collection<Table> tables)
			throws GeradorException;
}
