package com.quantium.mobile.framework.query;

import java.util.Date;

import com.quantium.mobile.framework.AndroidValueParser;
import com.quantium.mobile.framework.utils.SQLiteUtils;
import com.quantium.mobile.framework.utils.ValueParser;

public class QSQLProvider extends AbstractQSQLProvider {

    public QSQLProvider(Q q){
        super(q, new AndroidValueParser());
    }


    public QSQLProvider(Q q, ValueParser parser){
        super(q, parser);
    }

    @Override
    protected void limitOffsetOut(long limit, long offset,
                                  StringBuilder selectStatement)
    {
        if (limit <= 0)
            limit = -1;
        String limitStr =
                (offset > 0) ?
                        String.format(" LIMIT %d,%d", offset, limit):
                (limit > 0) ?
                        String.format(" LIMIT %d", limit):
                // limit <= 0 && offset <= 0
                        "";
        selectStatement.append(limitStr);
    }

    @Override
    protected String getColumn(String tableAs, Table.Column<?> column) {
          // Eh bom reforcar o uso de "alias" para tabelas em um ORM
//        String columnNameWithTable =
//            (
//                (tableAs != null) ?
//                    // se a tabela for nomeada com "tablename AS tablealias"
//                    tableAs + '.' :
//                     // se nao ha alias
//                    ""
//            ) + column.getName();
      String columnNameWithTable = tableAs + '.' + column.getName();
        return
            // tratar a classe Date para o SQlite3
            (column.getKlass().equals(Date.class)) ?
                // se eh date, buscar por "datetime(coluna)"
                SQLiteUtils.dateTimeForColumn(columnNameWithTable) :
                // se nao, apenas o nome
                columnNameWithTable;
    }


}
