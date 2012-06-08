package br.com.cds.mobile.framework.query;

import java.util.ArrayList;
import java.util.Date;

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
    private String argsCache[];

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
        Table.Column<T> otherColumn
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

    public String getSelectStm(Table.Column<?>...columns){
        String out = "SELECT ";
        for(int i=0 ; ; i++){
            out += getColumn(null, columns[i]);
            if( i < columns.length -1 )
                out += ',';
            else
                break;
        }
        out += " FROM " + this.table.getName();
        if(this.joins != null ){
            for(InnerJoin j: this.joins){
                out += " JOIN " + j.foreignColumn.getTable().getName() +
                    " ON " + j.column.getName() + j.op.toString() +
                    j.foreignColumn.getTable().getName() + "." +
                    j.foreignColumn.getName();
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
    public String[] getArguments() {
        if (qstringCache == null)
            genQstringAndArgs();
        return argsCache;
    }

    private void genQstringAndArgs () {
        if (root == null )
            return;
        StringBuilder sb = new StringBuilder();
        ArrayList<String> args = new ArrayList<String>();
        root.output(sb, args);
        qstringCache = sb.toString();
        argsCache = new String[args.size()];
        for (int i=0 ; i < argsCache.length ; i++)
            argsCache[i] = args.get(i);
    }

    private static Q mergeQs (Q q1, Q q2, String op) {
        Q out;
        try {
            out = (Q)q1.clone();
        } catch (CloneNotSupportedException e) {
            // Este erro nao ira acontecer a se Q implementar Cloneable
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

    private String getColumn(String tableAs, Table.Column<?> column) {
        return (
                // para innerJoins multiplos
                (tableAs != null) ?
                    // se a tabela for nomeada com "tablename AS tablealias"
                    tableAs + '.' :
                // conferir se a tabela da coluna eh a mesma que a atual
                (column.getTable().equals(this.table)) ?
                    // se a tabela for a mesma, nao adiciona nada
                    "" :
                    // adiciona o nome da tabela
                    column.getTable().getName() + '.'
            ) + (
                // tratar a classe Date para o SQlite3
                (column.getKlass().equals(Date.class)) ?
                    // se eh date, buscar por "datetime(coluna)"
                    SQLiteUtils.dateTimeForColumn(column.getName()) :
                    // se nao, apenas o nome
                    column.getName()
            );
    }

/*
    private static Q mergeQs (Q q1, Q q2, String op){
        Q out;
        try {
            out = (Q)q1.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        if (out.query == null ) {
             out.query = q2.query;
        } else if (q2.query != null){
            if (q1.args.size() > 1)
                out.query = "("+out.query+')';
            out.query += op + (
                (q2.args != null && q2.args.size() > 1) ?
                    "(" + q2.query + ')' :
                    q2.query
            );
        }
        if (out.joins == null){
            out.joins = q2.joins;
        } else if (q2.joins != null) {
            out.addInnerJoin(q2.joins);
        }
        return out;
    }
*/


    private static class QNode {
        QNode next;
        String nextOp;

        void output(StringBuilder sb, ArrayList<String> args) {
            if (next != null) {
                sb.append(nextOp);
                next.output(sb, args);
            }
        }

    }

    private static class QNodeGroup extends QNode{
        QNode node;

        @Override
        void output(StringBuilder sb, ArrayList<String> args) {
            sb.append('(');
            this.node.output(sb, args);
            sb.append(')');
            super.output(sb, args);
        }

    }

    private class QNode1X1 extends QNode {
        Table.Column<?> column;
        Op1x1 op;
        Object arg;

        @Override
        void output(StringBuilder sb, ArrayList<String> args) {
            sb.append( Q.this.getColumn(null, this.column) );
            sb.append(op.toString());
            if (arg instanceof Table.Column) {
                sb.append( ((Table.Column<?>)arg).getName() );
            } else {
                sb.append('?');
                args.add(SQLiteUtils.parse(arg));
            }
            super.output(sb, args);
        }

    }

    private class InnerJoin {
        Table.Column<?> foreignColumn;
        Op1x1 op;
        Table.Column<?> column;
    }

    public static enum Op1x1 {

        NE, EQ, LT, GT, LE, GE, IN;

        public String toString() {
            return
                this == NE     ?  "<>"     :
                this == EQ     ?  "="      :
                this == LT     ?  "<"      :
                this == GT     ?  ">"      :
                this == LE     ?  "<="     :
                this == GE     ?  ">="     :
                /* this == LIKE             ?  " LIKE " : */
                /*this == IN   ?*/  " IN " ;
        }
    }


}
