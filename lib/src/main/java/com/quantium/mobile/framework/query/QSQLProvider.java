package com.quantium.mobile.framework.query;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.quantium.mobile.framework.query.Q.InnerJoin;
import com.quantium.mobile.framework.query.Q.Op1x1;
import com.quantium.mobile.framework.query.Q.OpUnary;
import com.quantium.mobile.framework.utils.SQLiteUtils;

public class QSQLProvider {

    private static final String NULL_ARGUMENT_EXCEPTION_FMT =
            "Operador %s operando coluna %s com argumento %s";

    public QSQLProvider(Q q) {
        this.q = q;
    }

    private Q q;

    public String select(Table.Column<?> columns [], List<String> args){
        Table table = q.getTable();
        Collection<Q.InnerJoin> joins = q.getInnerJoins();

        String out = "SELECT ";
        for(int i=0 ; ; i++){
            out += getColumn(
                columns[i].getTable().getName(),
                columns[i]
            );
            if( i < columns.length -1 )
                out += ',';
            else
                break;
        }
        out += " FROM " + table.getName() + " AS " + table.getName();
        if (joins != null ){
            for(InnerJoin j: joins) {
                out += " JOIN " + j.foreignColumn.getTable().getName() +
                    " AS " + j.foreignColumn.getTable().getName() +
                    " ON " +
                    getColumn(j.column.getTable().getName(), j.column) +
                    j.op.toString() +
                    getColumn(j.foreignColumn.getTable().getName(), j.foreignColumn);
            }
        }
        StringBuilder sb = new StringBuilder();
        genQstringAndArgs(sb, args);
        String qstring = sb.toString();
        if(qstring != null && !qstring.matches("\\s*")){
            out += " WHERE " + qstring;
        }
        return out;
    }

    private void genQstringAndArgs (StringBuilder sb, List<String> args) {
        Q.QNode node = q.getRooNode();
        if (node == null )
            return;
        output(node, q.getTable(), sb, args);
    }

    void output(Q.QNode node, Table table, StringBuilder sb, List<String> args) {
        if (node == null)
            return;
        for(;;) {
            if (node instanceof Q.QNodeGroup)
                outputQNodeGroup((Q.QNodeGroup)node, table, sb, args);
            else if (node instanceof Q.QNode1X1)
                outputQNode1X1((Q.QNode1X1)node, table, sb, args);
            else if (node instanceof Q.QNodeUnary)
                outputQNodeUnary((Q.QNodeUnary)node, table, sb, args);
            else
                throw new RuntimeException();
            if (node.next == null)
                break;
            sb.append(node.nextOp);
            node = node.next;
        }
    }

    void outputQNodeGroup(Q.QNodeGroup node, Table table, StringBuilder sb, List<String> args) {
        if (node.notOp)
            sb.append(" NOT ");
        sb.append('(');
        output(node.node, table, sb, args);
        sb.append(')');
        //output(node, table, sb, args);
    }

    void outputQNode1X1(Q.QNode1X1 node, Table table, StringBuilder sb, List<String> args) {
        Table.Column<?> column = node.column;
        Op1x1 op = node.op;
        Object arg = node.arg;

        sb.append( getColumn(
                column.getTable().getName(),
                column)
        );
        if (arg == null) {
            switch (op) {
            case EQ:
                sb.append( OpUnary.ISNULL.toString());
                break;
            case NE:
                sb.append( OpUnary.NOTNULL.toString());
                break;
            default:
                throw new QueryParseException(String.format(
                    NULL_ARGUMENT_EXCEPTION_FMT,
                    op.toString(),
                    column.getTable().getName() + column.getName(),
                    (arg == null) ? "NULL" : arg.toString()
                ));
            }
        } else {
            sb.append(op.toString());
            switch(op){
            case IN:
                sb.append('(');
                if (args instanceof Collection){
                    Iterator<?> it = ((Collection<?>)arg).iterator();
                    if (it.hasNext()){
                        for (;;){
                            Object next = it.next();
                            sb.append('?');
                            args.add(SQLiteUtils.parse(next));
                            if (it.hasNext()){
                                sb.append(',');
                            } else {
                                break;
                            }
                        }
                    }
                }
                sb.append(')');
                break;
            default:
                if (arg instanceof Table.Column) {
                    sb.append( ((Table.Column<?>)arg).getName() );
                } else {
                    sb.append('?');
                    args.add(SQLiteUtils.parse(arg));
                }
            }
        }

    }

    void outputQNodeUnary(Q.QNodeUnary node, Table table, StringBuilder sb, List<String> args) {
        Table.Column<?> column = node.column;
        OpUnary op = node.op;

        sb.append( getColumn(
            // conferir se eh da mesma tabela
            ( table.equals(column.getTable()) ?
                // se for a coluna for da mesma tabela
                null :
                // caso seja uma coluna de outra tabela
                column.getTable().getName()
            ),
            column)
        );
        sb.append(op.toString());
    }

    private static String getColumn(String tableAs, Table.Column<?> column) {
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
