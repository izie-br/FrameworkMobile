package com.quantium.mobile.geradores.dbschema;

import java.sql.Date;

import com.quantium.mobile.framework.utils.DateUtil;
import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.tabelaschema.TabelaSchema;
import com.quantium.mobile.geradores.tabelaschema.TabelaSchema.Coluna;
import com.quantium.mobile.geradores.util.ColumnsUtils;
import com.quantium.mobile.geradores.util.SQLiteGeradorUtils;

public class SQLiteSchemaGenerator {

	public String getSchemaFor(TabelaSchema table) {
		StringBuilder schemaSb = new StringBuilder("CREATE TABLE ");
		schemaSb.append(table.getNome());
		schemaSb.append("(");

		//Chave primaria
		Coluna pk = table.getPrimaryKeys().get(0);
		schemaSb.append(pk.getNome());
		schemaSb.append(" INTEGER PRIMARY KEY AUTOINCREMENT");

		//Outras colunas, exceto a chave primaria
		
		for (String columnName : ColumnsUtils.orderedColumnsFromTableSchema (table)) {

			Coluna column = null;
			for (Coluna col : table.getColunas ()) {
				if (col.getNome ().equals (columnName))
					column = col;
			}

			//pulando a chave primaria
			if (column == pk || column.getNome().equals(pk.getNome()))
				continue;

			schemaSb.append(',');
			schemaSb.append(column.getNome());
			schemaSb.append(' ');
			schemaSb.append(SQLiteGeradorUtils.getSqlTypeFromClass(column.getType()));
			writeConstraints(column, schemaSb);
		}

		schemaSb.append(");\n");

		return schemaSb.toString();
	}

	private static void writeConstraints(
			Coluna column, StringBuilder out)
	{
		Constraint constraints [] = column.getConstraints();
		if (constraints == null)
			return;
		for (Constraint constraint : constraints) {
			switch (constraint.getType()) {
			case UNIQUE:
				out.append(" UNIQUE");
				break;
			case NOT_NULL:
				out.append(" NOT NULL");
				break;
			case DEFAULT:
				out.append(" DEFAULT ");
				Class<?> type = column.getType();
				if (type.equals(Date.class)) {
					out.append('\'');
					out.append(DateUtil.timestampToString(
							(Date)constraint.getArgs()[0]
					));
					out.append('\'');
				} else if (type.equals(String.class)) {
					out.append('\'');
					out.append( (String)constraint.getArgs()[0] );
					out.append('\'');
				} else {
					out.append( (constraint.getArgs()[0]).toString() );
				}
				break;
			default:
				/* no-op */
			}
		}
	}


}
