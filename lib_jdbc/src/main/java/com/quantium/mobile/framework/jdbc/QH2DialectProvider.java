package com.quantium.mobile.framework.jdbc;

import com.quantium.mobile.framework.query.AbstractQSQLProvider;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.Table.Column;

public class QH2DialectProvider extends AbstractQSQLProvider {

    public QH2DialectProvider(Q q) {
        super(q);
    }

    @Override
    protected String getColumn(String tableAs, Column<?> column) {
        String columnNameWithTable =
                (
                    (tableAs != null) ?
                        // se a tabela for nomeada com "tablename AS tablealias"
                        (tableAs + '.') :
                         // se nao ha alias
                        ""
                ) + column.getName();
            return columnNameWithTable;
    }

    @Override
    protected Object parseArgument(Object arg) {
        return arg;
    }

}
