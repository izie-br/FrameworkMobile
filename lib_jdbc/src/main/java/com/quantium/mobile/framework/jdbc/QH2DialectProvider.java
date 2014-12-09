package com.quantium.mobile.framework.jdbc;

import java.util.List;

import com.quantium.mobile.framework.query.AbstractQSQLProvider;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.query.Table.Column;
import com.quantium.mobile.framework.utils.ValueParser;

public class QH2DialectProvider extends AbstractQSQLProvider {

	public QH2DialectProvider(Q q) {
        super(q, new ValueParser());
    }
	
	public QH2DialectProvider(Q q, ValueParser parser) {
        super(q, parser);
    }

    @Override
    protected String getColumn(String tableAs, Column<?> column) {
        // Eh bom reforcar o uso de "alias" para tabelas em um ORM
//        String columnNameWithTable =
//                (
//                    (tableAs != null) ?
//                        // se a tabela for nomeada com "tablename AS tablealias"
//                        (tableAs + '.') :
//                         // se nao ha alias
//                        ""
//                ) + column.getName();
      String columnNameWithTable = tableAs + '.' + column.getName();
            return columnNameWithTable;
    }

    @Override
    protected String getColumnForWhere(String tableAs, Column<?> column) {
        String columnNameWithTable = tableAs + '.' + column.getName();
        return columnNameWithTable;
    }

    @Override
    protected void limitOffsetOut(long limit, long offset,
                                  StringBuilder selectStatement)
    {
        if (limit <= 0)
            limit = -1;
        String limitStr =
                (offset > 0) ?
                        String.format(" LIMIT %d OFFSET %d", limit, offset) :
                (limit > 0) ?
                        String.format(" LIMIT %d", limit):
                //(limit == 0 && offset == 0) ?
                        "";
        selectStatement.append(limitStr);
    }

    @Override
    protected String getNullOrderingClause() {
        return null;
    }

    @Override
    protected void outputQNode1X1(
            Q.QNode1X1 node, Table table, StringBuilder sb,
            List<Object> args)
    {
        if (node.op().equals(Q.Op1x1.GLOB)){
            Object arg = node.getArg();
            if (arg instanceof String){
                Column<?> column = node.column();
                sb.append(getColumnForWhere(column.getTable().getName(), column));
                sb.append(" REGEXP ?");
                String pattern = globPatternToRegex((String)arg);
                args.add(pattern);
            }
        } else {
            super.outputQNode1X1(node, table, sb, args);
        }
    }

    private String globPatternToRegex(String glob){
        StringBuilder sb = new StringBuilder(2*glob.length());

        // Escape torna-se true ao encontrar um '\'
        boolean escapeNext = false;

        for (int i = 0; i < glob.length(); i++){
            char c = glob.charAt(i);
            switch (c){
            case '[':
                if (escapeNext) {
                    sb.append("\\[");
                } else {
                    sb.append('[');
                    escapeNext = true;
                }
                break;
            case ']':
                if (escapeNext) {
                    escapeNext = false;
                    sb.append(']');
                } else {
                    sb.append("\\]");
                }
                break;
            case '*':
                if (escapeNext)
                    sb.append("\\*");
                else
                    sb.append(".*");
                break;
            case '?':
                if (escapeNext)
                    sb.append("\\?");
                else
                    sb.append(".");
                break;
            // Escapar estes .^$+-(){}
            case '.':
            case '^':
            case '$':
            case '+':
            case '-':
            case '(':
            case ')':
            case '{':
            case '}':
                sb.append("\\");
                /* fall through */
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
