package com.quantium.mobile.framework.query;

import java.util.Date;

import com.quantium.mobile.framework.utils.SQLiteUtils;

public class QSQLProvider extends AbstractQSQLProvider {

    public QSQLProvider(Q q){
        super(q);
    }

    @Override
    protected Object parseArgument(Object arg) {
        return SQLiteUtils.parse(arg);
    }

    @Override
    protected String getColumn(String tableAs, Table.Column<?> column) {
        String columnNameWithTable =
            (
                (tableAs != null) ?
                    // se a tabela for nomeada com "tablename AS tablealias"
                    tableAs + '.' :
                     // se nao ha alias
                    ""
            ) + column.getName();
        return
            // tratar a classe Date para o SQlite3
            (column.getKlass().equals(Date.class)) ?
                // se eh date, buscar por "datetime(coluna)"
                SQLiteUtils.dateTimeForColumn(columnNameWithTable) :
                // se nao, apenas o nome
                columnNameWithTable;
    }


}
