package com.quantium.mobile.framework.query;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.quantium.mobile.framework.query.Q.QNode1X1;
import com.quantium.mobile.framework.utils.StringUtil;

public abstract class AbstractQSQLProvider {

    private static final String NULL_ARGUMENT_EXCEPTION_FMT =
            "Operador %s operando coluna %s com argumento %s";

    protected abstract String getColumn(String tableAs, Table.Column<?> column);

    protected abstract Object parseArgument(Object arg);

    protected abstract void limitOffsetOut(long limit, long offset,
                                           StringBuilder selectStatement);

    public AbstractQSQLProvider(Q q) {
        this.q = q;
    }

    private Q q;
    private long limit;
    private long offset;
    private String orderby;

    public AbstractQSQLProvider limit(long limit) {
        this.limit = limit;
        return this;
    }

    public AbstractQSQLProvider offset(long offset) {
        this.offset = offset;
        return this;
    }

    public AbstractQSQLProvider orderBy(String orderby) {
        this.orderby = orderby;
        return this;
    }

    protected void orderByOut(String orderby, StringBuilder out) {
        if (StringUtil.isNull(orderby))
            return;
        out.append(" ORDER BY ");
        out.append(orderby);
    }

    public String select(List<?> selection, List<Object> args){
        Table table = q.getTable();
        Collection<Q.InnerJoin> joins = q.getInnerJoins();

        StringBuilder out = new StringBuilder("SELECT ");
        Iterator<?> it = selection.iterator ();
        while (it.hasNext ()){
            Object obj = it.next ();
            if (obj instanceof Table.Column) {
                Table.Column<?> column = (Table.Column<?>)obj;
                out.append(getColumn(
                    column.getTable().getName(),
                    column
                ));
            } else if (obj instanceof String) {
                out.append((String)obj);
            }
            if( it.hasNext () )
                out.append(',');
            else
                break;
        }
        String tableName = table.getName();
        out.append(" FROM ");
        out.append(tableName);
        out.append(" AS ");
        out.append(tableName);
        if (joins != null ){
            for(Q.InnerJoin j: joins) {
                String foreignKeyTableName = j.getForeignKey().getTable().getName();
                out.append(" JOIN ");
                out.append(foreignKeyTableName);
                out.append(" AS ");
                out.append(foreignKeyTableName);
                out.append(" ON ");
                out.append(getColumn(j.getColumn().getTable().getName(), j.getColumn()));
                out.append(j.op().toString());
                out.append(getColumn(foreignKeyTableName, j.getForeignKey()));
            }
        }
        StringBuilder sb = new StringBuilder();
        genQstringAndArgs(sb, args);
        String qstring = sb.toString();
        if(qstring != null && !qstring.matches("\\s*")){
            out.append(" WHERE ");
            out.append(qstring);
        }
        orderByOut(orderby, out);
        limitOffsetOut(limit, offset, out);
        return out.toString();
    }

    private void genQstringAndArgs (StringBuilder sb, List<Object> args) {
        Q.QNode node = q.getRooNode();
        if (node == null )
            return;
        output(node, q.getTable(), sb, args);
    }

    protected void output(Q.QNode node, Table table, StringBuilder sb, List<Object> args) {
        if (node == null)
            return;
        if (node instanceof Q.QNodeGroup){
            outputQNodeGroup((Q.QNodeGroup)node, table, sb, args);
        } else if (node instanceof Q.QNode1X1){
            outputQNode1X1((Q.QNode1X1)node, table, sb, args);
        } else if (node instanceof Q.QNode1xN) {
            outputQNode1XN((Q.QNode1xN)node, table, sb, args);
        } else if (node instanceof Q.QNodeUnary) {
            outputQNodeUnary((Q.QNodeUnary)node, table, sb, args);
        } else {
            throw new RuntimeException();
        }
    }

    protected void outputQNodeGroup(Q.QNodeGroup node, Table table, StringBuilder sb, List<Object> args) {
        boolean parenthesis = node.isNot() ||
                              (node.child() instanceof Q.QNodeGroup);
        if (node.isNot())
            sb.append(" NOT ");
        if (parenthesis)
            sb.append('(');
        output(node.child(), table, sb, args);
        if (parenthesis)
            sb.append(')');
        if (node.next() == null)
            return;
        sb.append(node.nextOp());
        parenthesis = node.next() instanceof Q.QNodeGroup;
        if (parenthesis)
            sb.append('(');
        output(node.next(), table, sb, args);
        if (parenthesis)
            sb.append(')');
    }

    protected void outputQNode1X1(Q.QNode1X1 node, Table table, StringBuilder sb, List<Object> args) {
        Table.Column<?> column = node.column();
        Q.Op1x1 op = node.op();
        Object arg = node.getArg();

        // Para o caso de argumentos NULL com tipos Long e Double
        // o argumento deve ser convertido em "0"
        if (arg == null) {
            if (column.getKlass().equals(Long.class)) {
                arg = ((Long)0L);
            } else if (column.getKlass().equals(Double.class)) {
                arg = ((Double)0.0);
            }
        }

        sb.append( getColumn(
                column.getTable().getName(),
                column)
        );
        if (arg == null) {
            switch (op) {
            case EQ:
                sb.append( Q.OpUnary.ISNULL.toString());
                break;
            case NE:
                sb.append( Q.OpUnary.NOTNULL.toString());
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
            if (arg instanceof Table.Column) {
                sb.append( ((Table.Column<?>)arg).getName() );
            } else {
                sb.append('?');
                args.add(parseArgument(arg));
            }
        }
    }

    protected void outputQNode1XN(Q.QNode1xN node, Table table, StringBuilder sb, List<Object> args) {
        Table.Column<?> column = node.column();
        Q.Op1xN op = node.op();
        Collection<?> arg = node.getArgs();

        sb.append( getColumn(
                column.getTable().getName(),
                column)
        );
        sb.append(op.toString());
        sb.append('(');
        if (args instanceof Collection){
            Iterator<?> it = ((Collection<?>)arg).iterator();
            if (it.hasNext()){
                for (;;){
                    Object next = it.next();
                    sb.append('?');
                    args.add(parseArgument(next));
                    if (it.hasNext()){
                        sb.append(',');
                    } else {
                        break;
                    }
                }
            }
        }
        sb.append(')');
    }


    protected void outputQNodeUnary(Q.QNodeUnary node, Table table, StringBuilder sb, List<Object> args) {
        Table.Column<?> column = node.column();
        Q.Op1x1 op1x1;
        switch (node.op()) {
        case ISNULL:
            op1x1 = Q.Op1x1.EQ;
            break;
        case NOTNULL:
            op1x1 = Q.Op1x1.NE;
            break;
        default:
            throw new RuntimeException();
        }
        QNode1X1 node1x1 = new Q.QNode1X1(column, op1x1, null);
        this.outputQNode1X1(node1x1, table, sb, args);
    }

}
