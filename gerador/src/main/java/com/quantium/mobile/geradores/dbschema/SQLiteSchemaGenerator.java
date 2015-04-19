package com.quantium.mobile.geradores.dbschema;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.utils.DateUtil;
import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.util.ColumnsUtils;
import com.quantium.mobile.geradores.util.SQLiteGeradorUtils;

import java.sql.Date;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Gerador de schema SQL para instancias de {@link Table}.
 *
 * @author Igor Soares
 */
public class SQLiteSchemaGenerator {

    private static Table.Column<?> extractPK(Table table) {
        for (Table.Column<?> col : table.getColumns()) {
            if (ColumnsUtils.checkIfIsPK(col))
                return col;
        }
        throw new RuntimeException(table.getName() + " sem PK");
    }

    private static void writeConstraints(
            Table.Column<?> column, StringBuilder out) {
        Collection<Constraint> constraints = column.getConstraintList();
        if (constraints == null)
            return;
        for (Constraint constraint : constraints) {
            if (constraint instanceof Constraint.Unique) {
                out.append(" UNIQUE");
            } else if (constraint instanceof Constraint.NotNull) {
                out.append(" NOT NULL");
            } else if (constraint instanceof Constraint.Default) {
                Constraint.Default<?> defaultConstraint =
                        (Constraint.Default<?>) constraint;
                out.append(" DEFAULT ");
                Class<?> type = column.getKlass();
                if (type.equals(Date.class)) {
                    out.append('\'');
                    out.append(DateUtil.timestampToString(
                            (Date) defaultConstraint.getValue()
                    ));
                    out.append('\'');
                } else if (type.equals(String.class)) {
                    out.append('\'');
                    out.append((String) defaultConstraint.getValue());
                    out.append('\'');
                } else {
                    out.append(defaultConstraint.getValue().toString());
                }
            } else {
                /* no-op */
            }
        }
    }

    private static Collection<Table.Column<?>> orderedColumns(Table table) {
        Set<Table.Column<?>> cols = new TreeSet<Table.Column<?>>(new Comparator<Table.Column<?>>() {
            public int compare(Table.Column<?> c1, Table.Column<?> c2) {
                if (c1.equals(c2))
                    return 0;
                if (ColumnsUtils.checkIfIsPK(c1))
                    return 1;
                if (ColumnsUtils.checkIfIsPK(c2))
                    return -1;
                int val = (c1.getKlass().getSimpleName()).compareTo
                        (c2.getKlass().getSimpleName());
                if (val != 0)
                    return val;
                return (c1.getName()).compareTo(c2.getName());
            }

            ;
        });
        cols.addAll(table.getColumns());
        return cols;
    }

    public String getSchemaFor(Table table) {
        StringBuilder schemaSb = new StringBuilder("CREATE TABLE ");
        schemaSb.append(table.getName());
        schemaSb.append("(");

        //Chave primaria
        Table.Column<?> pk = extractPK(table);
        schemaSb.append(pk.getName());
        if (Long.class.equals(pk.getKlass())) {
            schemaSb.append(" INTEGER PRIMARY KEY AUTOINCREMENT");
        } else {
            schemaSb.append(' ');
            schemaSb.append(SQLiteGeradorUtils.getSqlTypeFromClass(pk.getKlass()));
            schemaSb.append(" TEXT PRIMARY KEY ");
        }

        //Outras colunas, exceto a chave primaria


        for (Table.Column<?> column : orderedColumns(table)) {

            //pulando a chave primaria
            if (column.equals(pk) || column.getName().equals(pk.getName()))
                continue;

            schemaSb.append(',');
            schemaSb.append(column.getName());
            schemaSb.append(' ');
            schemaSb.append(SQLiteGeradorUtils.getSqlTypeFromClass(column.getKlass()));
            writeConstraints(column, schemaSb);
        }

        schemaSb.append(");\n");

        return schemaSb.toString();
    }


}
