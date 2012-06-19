package br.com.cds.mobile.framework.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.cds.mobile.framework.utils.SQLiteUtils;

public final class Q  implements Cloneable {

/*
SQL avg()
SQL count()
SQL first()
SQL last()
SQL max()
SQL min()
SQL sum()
SQL Group By
SQL Having
SQL ucase()
SQL lcase()
SQL mid()
SQL len()
SQL round()
SQL now()
SQL format()

public static byte COUNT = 14;
public static byte AVG = 15;
public static byte SUM = 16;
public static byte MIN = 17;
public static byte MAX = 18;
public static byte FIRST = 19;
public static byte LAST = 20;
public static byte ABS = 21;
public static byte ROUND = 22;
*/

    private Table table;
    private ArrayList<InnerJoin> joins = new ArrayList<Q.InnerJoin>(0);

    private QNode root;

    private String qstringCache;
    private List<String> argsCache;

    public Q (Table table) {
        this.table = table;
    }

    public <T> Q (Table.Column<T> column, Op1x1 op, T arg){
        this.table = column.getTable();
        init1x1(column, op, arg);
    }

    private <T> void init1x1(Table.Column<T> column, Op1x1 op, Object arg) {
        QNode1X1 node = new QNode1X1();
        node.column = column;
        node.op = op;
        node.arg = arg;
        this.root = node;
    }

    public <T> Q (
        Table.Column<T> column,
        Op1x1 op,
        Table.Column<?> otherColumn
    ){
        this.table = column.getTable();
        if ( otherColumn.getTable().equals(this.table) ) {
            init1x1(column, op, otherColumn);
        }
        else {
            InnerJoin join = new InnerJoin();
            join.column = column;
            join.op = op;
            join.foreignColumn = otherColumn;
            this.joins.add(join);
        }
    }

    public Q (Table.Column<?> column, OpUnary op) {
        QNodeUnary node = new QNodeUnary ();
        node.column = column;
        node.op = op;
        this.root = node;
    }


    public Q (Q q){
        this.table = q.table;
        this.joins = q.joins;
        if (q.root != null) {
            QNodeGroup group = new QNodeGroup();
            group.node = q.root;
            this.root = group;
        }
    }

/*    public static Q not (Q q){
        Q out = new Q(q);
        if (q.root != null)
            out.root = 
        return q;
    }
*/

    public Q and (Q q) {
        return mergeQs (this, q, " AND ");
    }

    public Q or (Q q) {
        return mergeQs (this, q, " OR ");
    }

    public String select(Table.Column<?>...columns){
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
        out += " FROM " + this.table.getName() + " AS " + this.table.getName();
        if(this.joins != null ){
            for(InnerJoin j: this.joins) {
                out += " JOIN " + j.foreignColumn.getTable().getName() +
                    " AS " + j.foreignColumn.getTable().getName() +
                    " ON " +
                    getColumn(j.column.getTable().getName(), j.column) +
                    j.op.toString() +
                    getColumn(j.foreignColumn.getTable().getName(), j.foreignColumn);
            }
        }
        String qstring = getQString();
        if(qstring != null){
            out += " WHERE " + qstring;
        }
        return out;
    }

    public String getQString () {
        if (qstringCache == null )
            genQstringAndArgs();
        return qstringCache;
    }

    /**
     * Gets the arguments for this instance.
     *
     * @return arguments.
     */
    public List<String> getArguments() {
        if (qstringCache == null)
            genQstringAndArgs();
        return argsCache;
    }

    private void genQstringAndArgs () {
        if (root == null )
            return;
        StringBuilder sb = new StringBuilder();
        ArrayList<String> args = new ArrayList<String>();
        root.output(table, sb, args);
        qstringCache = sb.toString();
        argsCache = args;
    }

    private static Q mergeQs (Q q1, Q q2, String op) {
        Q out;
        try {
            out = (Q)q1.clone();
        } catch (CloneNotSupportedException e) {
            // Este erro nao ira acontecer se Q implementar Cloneable
            throw new RuntimeException("classe Q sem suporte a clone");
        }
        out.qstringCache = null;
        out.argsCache = null;
        if (out.root == null) {
            out.root = q2.root;
        } else if (q2.root != null) {
            if (q1.root.next != null ) {
                QNodeGroup group = new QNodeGroup();
                group.node = q1.root;
                out.root = group;
            }
            if (q2.root.next != null ) {
                QNodeGroup nextGroup = new QNodeGroup();
                nextGroup.node = q2.root;
                out.root.next = nextGroup;
            } else {
                out.root.next = q2.root;
            }
            out.root.nextOp = op;
        }
        if (out.joins == null){
            out.joins = q2.joins;
        } else if (q2.joins != null) {
            out.joins.addAll(q2.joins);
        }
        return out;
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

    private static class QNode {
        QNode next;
        String nextOp;

        void output(Table table, StringBuilder sb, ArrayList<String> args) {
            if (next != null) {
                sb.append(nextOp);
                next.output(table, sb, args);
            }
        }

    }

    private static class QNodeGroup extends QNode{
        QNode node;

        @Override
        void output(Table table, StringBuilder sb, ArrayList<String> args) {
            sb.append('(');
            this.node.output(table, sb, args);
            sb.append(')');
            super.output(table, sb, args);
        }

    }

    private static class QNode1X1 extends QNode {
        Table.Column<?> column;
        Op1x1 op;
        Object arg;

        @Override
        void output(Table table, StringBuilder sb, ArrayList<String> args) {
            sb.append( Q.getColumn(
                column.getTable().getName(),
                this.column)
            );
            sb.append(op.toString());
            if (arg instanceof Table.Column) {
                sb.append( ((Table.Column<?>)arg).getName() );
            } else {
                sb.append('?');
                args.add(SQLiteUtils.parse(arg));
            }
            super.output(table, sb, args);
        }

    }

    private static class QNodeUnary extends QNode {
        Table.Column<?> column;
        OpUnary op;

        @Override
        void output(Table table, StringBuilder sb, ArrayList<String> args) {
            sb.append( Q.getColumn(
                // conferir se eh da mesma tabela
                ( table.equals(this.column.getTable()) ?
                    // se for a coluna for da mesma tabela
                    null :
                    // caso seja uma coluna de outra tabela
                    column.getTable().getName()
                ),
                this.column)
            );
            sb.append(op.toString());
            super.output(table, sb, args);
        }

    }

    private class InnerJoin {
        Table.Column<?> foreignColumn;
        Op1x1 op;
        Table.Column<?> column;
    }

    public static enum OpUnary {
        ISNULL, NOTNULL;

        public String toString () {
            return
                this == ISNULL     ?     " ISNULL " :
                /* this == NOTNULL ?  */ " NOTNULL ";
        }
    }

    public static enum Op1x1 {

        NE, EQ, LT, GT, LE, GE /*, IN*/;

        public String toString() {
            return
                this == NE     ?  "<>"     :
                this == EQ     ?  "="      :
                this == LT     ?  "<"      :
                this == GT     ?  ">"      :
                this == LE     ?  "<="     :
                /* this == GE ?*/ ">="     ;
                /* this == LIKE             ?  " LIKE " : */
                /*this == IN   ?  " IN " ; */
        }
    }


}
