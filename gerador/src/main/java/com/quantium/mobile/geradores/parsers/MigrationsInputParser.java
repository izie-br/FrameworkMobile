package com.quantium.mobile.geradores.parsers;

import com.quantium.mobile.geradores.util.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Le multiplos arquivos de entrada SQL, seguindo o dialeto do H2DB.</p>
 * <p>
 * Apos concatenar todos scripts, realiza processo semelhante ao
 * {@link SQLiteInputParser}
 * </p>
 * <p/>
 * <p>TODO:: Isolar trechos especificos do H2DB</p>
 *
 * @author Igor Soares
 */
public class MigrationsInputParser extends SQLiteInputParser {

    public static final String INPUT_PARSER_IDENTIFIER = "migrations";

    public static ArrayList<File> getOrderedFiles(
            File sqlResource, Integer version) {
        File files[] = sqlResource.listFiles();

        final Pattern pat = Pattern.compile(
                "^" + Pattern.quote(Constants.DB_VERSION_PREFIX) +
                        "(\\d+).sql$");


        // Filtrar quais arquivos tem nome correto e coloca-los em ordem
        ArrayList<File> migrationFiles = new ArrayList<File>();

        // Cuidado, o operador de parada deve ser menor igual em "i <= version"
        for (int i = 0; i <= version; i++) {
            for (File f : files) {
                Matcher mobj = pat.matcher(f.getName());
                if (mobj.find() &&
                        Integer.parseInt(mobj.group(1)) == i) {
                    migrationFiles.add(f);
                    break;
                }
            }
        }
        return migrationFiles;
    }

    private static void writeFileToSb(File f, StringBuilder sb) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(f),
                            "UTF-8"
                    )
            );

            String line = removeH2Dbkeywords(reader.readLine());
            while (line != null) {
                sb.append(line);
                line = removeH2Dbkeywords(reader.readLine());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    //TODO reposicionar isto, nem sempre sera H2Db
    private static final String removeH2Dbkeywords(String input) {
        if (input == null)
            return null;
        return input.replaceAll("AUTO_INCREMENT", "AUTOINCREMENT");
    }

    @Override
    protected String getSqlTill(File sqlResource, Integer version) {
        StringBuilder sb = new StringBuilder();

        ArrayList<File> migrationFiles = getOrderedFiles(
                sqlResource, version);

        for (File f : migrationFiles) writeFileToSb(f, sb);

        return sb.toString();
    }

}
