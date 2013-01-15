package com.quantium.mobile.geradores.parsers;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.quantium.mobile.geradores.GeradorException;

public class FileParserMapper {

	@SuppressWarnings("serial")
	private static Map<String, String> fileParserMap = new HashMap<String, String>() {
		{
			put("xml", "sqlite");
			put("json", "json");
			put("migrations", "migrations");
		}
	};

	public static String getTypeFromFileName(String name) throws GeradorException {
		String type = fileParserMap.get(name);
		if (type == null) {
			// buscar ultimo fragmento do caminho do arquivo (ou diretorio)
			String arr [] = name.split (Pattern.quote (System.getProperty ("file.separator")));

			// tratar caso o caminho seja pasta e termine em '/'
			String last = (arr[arr.length-1].equals (""))?
					arr[arr.length-2] :
					arr[arr.length-1];

			type = fileParserMap.get(arr[arr.length-1]);
		}
		if (type == null) type = fileParserMap.get(name.split("\\.")[1]);
		if (type == null) throw new GeradorException("Nenhum formato de " +
				"arquivo de entrada do gerador encontrado para o arquivo " +
				name);
		return type;
	}
}
