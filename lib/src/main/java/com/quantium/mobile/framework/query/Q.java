package com.quantium.mobile.framework.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.quantium.mobile.framework.utils.SQLiteUtils;

/**
 * Classe geradora de querystrings.
 */
public final class Q {

    private static final String NULL_ARGUMENT_EXCEPTION_FMT =
        "Operador %s operando coluna %s com argumento %s";

    private Table table;
    // pode ser NULL
    private ArrayList<InnerJoin> joins;
    // pode ser NULL
    private QNode root;


    /**
     * Busca simples por tabela sem filtros.
     * @param table tabela
     */
    public Q (Table table) {
        this.table = table;
    }

    /**
     * <p>Gera uma queryString &quot;column op ?&quot;.</p>
     * <p>O argumento tambem é armazenado.</p>
     * <p>Exemplo: &quot;id = ?&quot;, com argumento long &quot;1&quot;</p>
     *
     * @param colum coluna
     * @param op operador Op1x1
     * @param arg argumento
     */
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

    /**
     * <p>
     *   Gera uma queryString &quot;column1 op coluna2&quot; ou então um
     *   &quot;inner join table2 on table1.column1 op table2.coluna2&quot;.
     * </p>
     *
     * @param colum coluna
     * @param op operador Op1x1
     * @param otherColumn outra coluna
     */
    public <T> Q (
        Table.Column<T> column,
        Op1x1 op,
        Table.Column<?> otherColumn
    ){
        this.table = column.getTable();
        if ( otherColumn.getTable().equals(this.table) ) {
            init1x1(column, op, otherColumn);
        } else {
            InnerJoin join = new InnerJoin();
            join.column = column;
            join.op = op;
            join.foreignColumn = otherColumn;
            getJoins().add(join);
        }
    }

    /**
     * <p>Gera uma queryString &quot;column op&quot;</p>
     *
     * @param colum coluna
     * @param op operador OpUnary
     */
    public Q (Table.Column<?> column, OpUnary op) {
        QNodeUnary node = new QNodeUnary ();
        node.column = column;
        node.op = op;
        this.root = node;
    }

    /**
     * Força agrupamento em um fragmento querystring, envolvendo-o com
     * perênteses.
     */
    public Q (Q q){
        this.table = q.table;
        if (q.joins != null)
            this.joins = new ArrayList<InnerJoin>(q.joins);
        if (q.root != null) {
            QNodeGroup group = new QNodeGroup();
            group.node = q.root;
            this.root = group;
        }
    }

    /**
     * Usa o operador NOT em um fragmento querystring, envolvendo-o com
     * perênteses.
     */
    public static Q not (Q q){
        Q out = new Q(q);
        // alterar se o o construtor "Q (Q)" for alterado
        if (q.root != null)
            ((QNodeGroup)out.root).notOp = true;
        return q;
    }

    /**
     * <p>Faz a busca por texto com operador LIKE.</p>
     * <p>Exemplo: &quot;colunm LIKE ?&quot;</p>
     *
     * @param colum coluna
     * @param pattern expressao
     */
    public static Q like ( Table.Column<String> column, String pattern) {
        return new Q(column, Op1x1.LIKE, pattern);
    }

    /**
     * <p>Faz a busca por texto com operador GLOB (unix-like).</p>
     * <p>Exemplo: &quot;colunm GLOB ?&quot;</p>
     *
     * @param colum coluna
     * @param pattern expressao
     */
    public static Q glob ( Table.Column<String> column, String pattern) {
        return new Q(column, Op1x1.GLOB, pattern);
    }

    /*
     * <p>Faz a busca por texto com operador REGEXP.</p>
     * <p>Exemplo: &quot;colunm REGEXP ?&quot;</p>
     *
     * @param colum coluna
     * @param pattern expressao
     *
    public static Q regexp( Table.Column<String> column, String pattern) {
        return new Q(column, Op1x1.REGEXP, pattern);
    }
     */

    /**
     * Combina dois fragmentos de querystring usando o operador AND.
     */
    public Q and (Q q) {
        return mergeQs (this, " AND ", q);
    }

    /**
     * Combina dois fragmentos de querystring usando o operador OR.
     */
    public Q or (Q q) {
        return mergeQs (this, " OR ", q);
    }

    public String select(Table.Column<?> columns [], List<String> args){
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
        StringBuilder sb = new StringBuilder();
        genQstringAndArgs(sb, args);
        String qstring = sb.toString();
        if(qstring != null && !qstring.matches("\\s*")){
            out += " WHERE " + qstring;
        }
        return out;
    }

    private ArrayList<InnerJoin> getJoins () {
        if (joins == null)
            joins = new ArrayList<InnerJoin>(1);
       return joins;
    }

    private void genQstringAndArgs (StringBuilder sb, List<String> args) {
        if (root == null )
            return;
        root.output(table, sb, args);
    }

    private static Q mergeQs (Q q1, String op, Q q2) {
        Q out = new Q(q1.table);
        if (q1.joins != null)
            out.joins = new ArrayList<Q.InnerJoin>(q1.joins);

        out.root = (q1.root != null) ? q1.root : q2.root;

        if (q1.root != null && q2.root != null) {
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

        void output(Table table, StringBuilder sb, List<String> args) {
            if (next != null) {
                sb.append(nextOp);
                next.output(table, sb, args);
            }
        }

    }

    private static class QNodeGroup extends QNode{
        boolean notOp;
        QNode node;

        @Override
        void output(Table table, StringBuilder sb, List<String> args) {
            if (notOp)
                sb.append(" NOT ");
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
        void output(Table table, StringBuilder sb, List<String> args) {
            sb.append( Q.getColumn(
                column.getTable().getName(),
                this.column)
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
                if (arg instanceof Table.Column) {
                    sb.append( ((Table.Column<?>)arg).getName() );
                } else {
                    sb.append('?');
                    args.add(SQLiteUtils.parse(arg));
                }
            }
            super.output(table, sb, args);
        }

    }

    private static class QNodeUnary extends QNode {
        Table.Column<?> column;
        OpUnary op;

        @Override
        void output(Table table, StringBuilder sb, List<String> args) {
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

    /**
     * Operadores unarios. Operam uma coluna.
     */
    public static enum OpUnary {
        ISNULL, NOTNULL;

        public String toString () {
            return
                this == ISNULL     ?     " ISNULL" :
                /* this == NOTNULL ?  */ " NOTNULL";
        }
    }

    /**
     * Operadores 1x1. Operam uma coluna com outra coluna, ou coluna com
     * uma argumento.
     */
    public static enum Op1x1 {

        NE, EQ, LT, GT, LE, GE, LIKE, GLOB; // REGEXP;

        public String toString() throws QueryParseException {
            return
                this == NE     ?  "<>"     :
                this == EQ     ?  "="      :
                this == LT     ?  "<"      :
                this == GT     ?  ">"      :
                this == LE     ?  "<="     :
                this == GE     ?  ">="     :
                this == LIKE   ?  " LIKE " :
             /* this == GLOB   ?*/" GLOB " ;
             /* this == REGEXP ? " REGEXP "; */
                /*this == IN   ?  " IN " ; */
        }
    }



}
