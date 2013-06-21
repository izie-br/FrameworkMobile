package com.quantium.mobile.geradores.parsers;

import java.util.HashMap;
import java.util.Map;

import com.quantium.mobile.geradores.GeradorException;
import com.quantium.mobile.geradores.util.LoggerUtil;

public class InputParserRepository {


	private static Map<String, InputParser> parsersMap = new HashMap<String, InputParser>();
	// TODO pensar em maneira melhor de registrar parsers.
	//  De preferencia um arquivo de resources ou annotations,etc...
	//  Acho bom remover todos estes campos static tb
	static {
		InputParserRepository.registerInputParser(
				SQLiteInputParser.INPUT_PARSER_IDENTIFIER,
				new SQLiteInputParser());
		InputParserRepository.registerInputParser(
				JsonInputParser.INPUT_PARSER_IDENTIFIER,
				new JsonInputParser());
		InputParserRepository.registerInputParser(
				MMInputParser.INPUT_PARSER_IDENTIFIER,
				new MMInputParser());
		InputParserRepository.registerInputParser (
				MigrationsInputParser.INPUT_PARSER_IDENTIFIER,
				new MigrationsInputParser ());
	}

	public static InputParser getInputParser(String parserInputType) throws GeradorException {
		LoggerUtil.getLog().info("Type:: " + parserInputType);
		InputParser parser = parsersMap.get(parserInputType);
		if (parser == null) {
			throw new GeradorException("Nenhum parser do tipo \"" + parserInputType + "\"");
		}
		return parser;
	}

	public static void registerInputParser(String parserInputType,
			InputParser parser) {
		parsersMap.put(parserInputType, parser);
	}

}
