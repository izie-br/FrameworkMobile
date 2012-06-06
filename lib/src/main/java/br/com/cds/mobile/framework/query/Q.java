package br.com.cds.mobile.framework.query;

import java.util.ArrayList;

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
    private InnerJoin joins[];
    private String query;
    private ArrayList<String> args = new ArrayList<String>();

    public <T> Q (Table.Column<T> column, Op1x1 op, T arg){
        this.table = column.getTable();
        this.query = column.getName() + op.toString() + "?";
        this.args.add(arg.toString());
    }

    public <T> Q (
        Table.Column<T> column,
        Op1x1 op,
        Table.Column<T> foreignColumn
    ){
        this.table = column.getTable();
        this.joins = new InnerJoin[1];
        InnerJoin join = new InnerJoin();
        join.column = column;
        join.op = op;
        join.foreignColumn = foreignColumn;
        this.joins[0] = join;
    }

    public Q (Q q){
        this.table = q.table;
        this.joins = q.joins;
        if (q.query != null)
            this.query = "(" + q.query + ')';
        this.args = q.args;
    }

    public static Q not (Q q){
        Q out = new Q(q);
        if (q.query != null)
            out.query = "!" + q.query;
        return q;
    }

    public Q and (Q q) {
        return mergeQs (this, q, " AND ");
    }

    public Q or (Q q) {
        return mergeQs (this, q, " OR ");
    }

    public String getQstring(){
        return this.query;
    }

    public String getSelectStm(Table.Column<?>...columns){
        String out = "SELECT ";
        for(int i=0 ; ; i++){
            out += columns[i].getName();
            if( i < columns.length )
                out += ',';
            else
                break;
        }
        out += "count(*)" + " FROM " + this.table.getName();
        if(this.joins != null ){
            for(InnerJoin j: this.joins){
                out += " JOIN " + j.foreignColumn.getTable().getName() +
                    " ON " + j.column.getName() + j.op.toString() +
                    j.foreignColumn.getTable().getName() + "." +
                    j.foreignColumn.getName();
            }
        }
        if(this.query != null){
            out += " WHERE " + this.query;
        }
        return out;
    }

    /**
     * Gets the arguments for this instance.
     *
     * @return arguments.
     */
    public String[] getArguments() {
        String argsArr[] = new String[this.args.size()];
        for (int i=0; i<argsArr.length; i++)
            argsArr[i] = this.args.get(i);
        return argsArr;
    }

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

    private void addInnerJoin (InnerJoin... innerJoins) {
        InnerJoin newJoins[] =
            new InnerJoin[this.joins.length+innerJoins.length];
        System.arraycopy(
            this.joins, 0,
            newJoins, 0,
            this.joins.length
        );
        System.arraycopy(
            innerJoins, 0,
            newJoins, this.joins.length,
            innerJoins.length
        );
        this.joins = newJoins;
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
