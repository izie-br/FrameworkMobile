package com.quantium.mobile.geradores.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.quantium.mobile.geradores.util.Constants;

public class MigrationsInputParser extends SQLiteInputParser {

	public static final String INPUT_PARSER_IDENTIFIER = "migrations";

	@Override
	protected String getSqlTill(File sqlResource, Integer version) {
		StringBuilder sb = new StringBuilder();
		File files [] = sqlResource.listFiles();

		Pattern pat = Pattern.compile (
				"^" + Pattern.quote (Constants.DB_VERSION_PREFIX) +
				"(\\d+).sql$");

		// Filtrar quais arquivos tem nome correto e coloca-los em ordem
		ArrayList<File> migrationFiles = new ArrayList<File> ();
		for (int i=0; i < version; i++){
			for (File f : files) {
				Matcher mobj = pat.matcher (f.getName ());
				if (mobj.find () &&
				    Integer.parseInt (mobj.group (1)) == 1)
				{
					migrationFiles.add (f);
					break;
				}
			}
		}

		for (File f : migrationFiles) writeFileToSb (f, sb);

		return sb.toString();
	}

	private static void writeFileToSb(File f, StringBuilder sb) {
		try {
			BufferedReader reader = new BufferedReader (
				new InputStreamReader (
					new FileInputStream (f),
					"UTF-8"
				)
			);
			sb.append (reader.readLine ());
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}

}
