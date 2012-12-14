package com.quantium.mobile.geradores.parsers;

import java.util.HashMap;
import java.util.Map;

import com.quantium.mobile.geradores.GeradorException;

public class FileParserMapper {

	@SuppressWarnings("serial")
	private static Map<String, String> fileParserMap = new HashMap<String, String>() {
		{
			put("xml", "sqlite");
			put("json", "json");
		}
	};

	public static String getTypeFromFileName(String name) throws GeradorException {
		String type = fileParserMap.get(name);
		if (type == null) type = fileParserMap.get(name.split("\\.")[1]);
		if (type == null) throw new GeradorException("Nenhum formato de " +
				"arquivo de entrada do gerador encontrado para o arquivo " +
				name);
		return type;
	}
}
