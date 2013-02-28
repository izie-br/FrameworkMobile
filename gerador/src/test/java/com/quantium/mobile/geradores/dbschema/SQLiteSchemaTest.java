package com.quantium.mobile.geradores.dbschema;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.javabean.ModelSchema;
import com.quantium.mobile.geradores.util.SQLiteGeradorUtils;
import com.quantium.mobile.geradores.util.TableUtil;

public class SQLiteSchemaTest {

	private static final String TABLE_NAME = "the_table";

	private static final String     PK_COLUMN_NAME = "the_pk";
	private static final Class<?>   PK_COLUMN_TYPE = Long.class;
	private static final Constraint PK_COLUMN_CONSTRAINTS [] = {
		Constraint.primaryKey()
	};

	private static final String     NAME_COLUMN_NAME = "name";
	private static final Class<?>   NAME_COLUMN_TYPE = String.class;
	private static final Constraint NAME_COLUMN_CONSTRAINTS [] = {
		Constraint.notNull()
	};

	private static final String     VALUE_COLUMN_NAME = "value";
	private static final Class<?>   VALUE_COLUMN_TYPE = Double.class;
	private static final Constraint VALUE_COLUMN_CONSTRAINTS [] = {
		Constraint.defaultValue(0.0),
		Constraint.notNull()
	};

	private static final String     OPTS_COLUMN_NAME = "opts";
	private static final Class<?>   OPTS_COLUMN_TYPE = String.class;
	private static final Constraint OPTS_COLUMN_CONSTRAINTS [] = {};


	@Test
	public void testSQLiteGeneratedFromTableSchema() {
		ModelSchema table = ModelSchema.create("default", TABLE_NAME)
				.addProperty(PK_COLUMN_NAME, PK_COLUMN_TYPE,
				             PK_COLUMN_CONSTRAINTS)
				.addProperty(NAME_COLUMN_NAME, NAME_COLUMN_TYPE,
				             NAME_COLUMN_CONSTRAINTS)
				.addProperty(VALUE_COLUMN_NAME, VALUE_COLUMN_TYPE,
				             VALUE_COLUMN_CONSTRAINTS)
				.addProperty(OPTS_COLUMN_NAME, OPTS_COLUMN_TYPE,
				             OPTS_COLUMN_CONSTRAINTS)
				.get();
		SQLiteSchemaGenerator schemaGeneator = new SQLiteSchemaGenerator();
		String schema = schemaGeneator.getSchemaFor(
				TableUtil.tableForModelSchema (table));

		//removendo newlines
		schema = schema.replaceAll("[\\r\\n]+", " ");

		final Pattern createTablePattern = Pattern.compile(
				"create\\s+table\\s+(\\w+)\\s*\\((.+),(.+),(.+),(.+)\\);",
				Pattern.CASE_INSENSITIVE);

		Matcher mobj = createTablePattern.matcher(schema);
		assertTrue(mobj.find());

		//conferir o name
		assertEquals(TABLE_NAME, mobj.group(1));

		// o primeiro grupo (1) do Matcher sera o nome,
		// os segintes (2,3,4,...) serao os qualificadores da coluna
		Pattern columnPattern = Pattern.compile(
				"^\\s*(\\w+)\\s*([\\w\"\\.]+\\s*)*$");

		boolean pkFound = false;
		boolean nameFound = false;
		boolean valueFound = false;
		boolean optsFound = false;

		for (int i=0; i < 4; i++) {
			String columnStr = mobj.group(i+2);
			assertTrue(columnStr, columnPattern.matcher(columnStr).find());

			String columnWords [] = columnStr.trim().split("\\s+");
			String columnName = columnWords[0];

			if (columnName.equals(PK_COLUMN_NAME)) {
				assertFalse("coluna repetida", pkFound);

				assertEquals(
						SQLiteGeradorUtils.getSqlTypeFromClass(PK_COLUMN_TYPE),
						columnWords[1]);

				// conferir se os qualificadores da coluna pk tem
				// as palavras chave abaixo
				Pattern pkopts = Pattern.compile("^(integer|primary|key|autoincrement)$", Pattern.CASE_INSENSITIVE);

				for (int j=2; j< columnWords.length; j++) {
					assertTrue(pkopts.matcher(columnWords[j]).find());
				}
				// marcar como encontrada
				pkFound = true;
			} else if (columnName.equals(NAME_COLUMN_NAME)) {
				assertFalse("coluna repetida", nameFound);

				assertEquals(
						SQLiteGeradorUtils.getSqlTypeFromClass(NAME_COLUMN_TYPE),
						columnWords[1]);

				Pattern opts = Pattern.compile("^(not|null)$", Pattern.CASE_INSENSITIVE);

				for (int j=2; j< columnWords.length; j++) {
					assertTrue(opts.matcher(columnWords[j]).find());
				}
				nameFound = true;
			} else if (columnName.equals(VALUE_COLUMN_NAME)) {
				assertFalse("coluna repetida", valueFound);

				assertEquals(
						SQLiteGeradorUtils.getSqlTypeFromClass(VALUE_COLUMN_TYPE),
						columnWords[1]);

				Pattern opts = Pattern.compile("^(not|null|default|0\\.0)$", Pattern.CASE_INSENSITIVE);

				for (int j=2; j< columnWords.length; j++) {
					assertTrue(opts.matcher(columnWords[j]).find());
				}
				valueFound = true;
			} else if (columnName.equals(OPTS_COLUMN_NAME)) {
				assertFalse("coluna repetida", optsFound);

				assertEquals(
						SQLiteGeradorUtils.getSqlTypeFromClass(OPTS_COLUMN_TYPE),
						columnWords[1]);

				assertEquals(2,columnWords.length);

				optsFound = true;
			}
		}

		assertTrue(pkFound);
		assertTrue(nameFound);
		assertTrue(valueFound);
		assertTrue(optsFound);
	}

}
