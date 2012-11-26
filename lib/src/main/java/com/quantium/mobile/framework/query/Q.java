package com.quantium.mobile.framework.query;

import java.util.ArrayList;
import java.util.Collection;

import com.quantium.mobile.framework.query.Table.Column;
import com.quantium.mobile.framework.utils.StringUtil;

/**
 * Classe geradora de querystrings.
 */
public final class Q {

    public static final OrderByAsc ASC = OrderByAsc.ASC;
    public static final OrderByAsc DESC = OrderByAsc.DESC;

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

    public <T> Q (Table.Column<T> column, Op1xN op, Collection<T> args){
        this.table = column.getTable();
        QNode1xN node = new QNode1xN();
        node.column = column;
        node.op = op;
        node.args = args;
        this.root = node;
    }

    private <T> void init1x1(Table.Column<T> column, Op1x1 op, Object arg) {
        QNode1X1 node = new QNode1X1(column, op, arg);
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
        this.table = column.getTable();
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
        return out;
    }

    /**
     * <p>Faz a busca por texto com operador LIKE.</p>
     * <p>Exemplo: &quot;colunm LIKE ?&quot;</p>
     *
     * @param colum coluna
     * @param pattern expressao
     */
    @SuppressWarnings("unchecked")
	public static Q like ( Table.Column<?> column, String pattern) {
        return new Q((Table.Column<Object>)column, Op1x1.LIKE, pattern);
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
        return mergeQs (this, ChainOp.AND, q);
    }

    /**
     * Combina dois fragmentos de querystring usando o operador OR.
     */
    public Q or (Q q) {
        return mergeQs (this, ChainOp.OR, q);
    }

    public Q.QNode getRooNode(){
        return this.root;
    }

    public Table getTable(){
        return this.table;
    }

    public Collection<Q.InnerJoin> getInnerJoins(){
        return this.joins;
    }


    private ArrayList<InnerJoin> getJoins () {
        if (joins == null)
            joins = new ArrayList<InnerJoin>(1);
       return joins;
    }

    private static Q mergeQs (Q q1, ChainOp op, Q q2) {
        Q out = new Q(q1.table);
        if (q1.joins != null)
            out.joins = new ArrayList<Q.InnerJoin>(q1.joins);

        if (q1.root != null && q2.root != null) {
            QNodeGroup outRoot = new QNodeGroup();
            outRoot.node = q1.root.clone();
            if (q1.root instanceof QNodeGroup && ((QNodeGroup)q1.root).next != null) {
                QNodeGroup group = new QNodeGroup();
                group.node = out.root;
                out.root = group;
            }
            outRoot.nextOp = op;
            outRoot.next = q2.root.clone();
            out.root = outRoot;
        } else if (q1.root != null){
            out.root = q1.root.clone();
        } else{
            out.root = q2.root.clone();
        }
        if (out.joins == null){
            out.joins = q2.joins;
        } else if (q2.joins != null) {
            out.joins.addAll(q2.joins);
        }
        return out;
    }

    //Classes NODE

    public static class QNode{
        @Override
        protected QNode clone(){
            QNode cloned;
            try {
                cloned = (QNode) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(StringUtil.getStackTrace(e));
            }
            return cloned;
        }
    }

    static class QNodeGroup extends QNode implements Cloneable{
        private QNode next;
        private ChainOp nextOp;
        private boolean notOp;
        private QNode node;

        public QNode next(){
            return next;
        }

        public String nextOp(){
            if (next == null)
               return null;
            return nextOp.toString();
        }

        public boolean isNot(){
            return notOp;
        }

        public QNode child(){
            return node;
        }

        @Override
        public QNodeGroup clone(){
            QNodeGroup cloned = (QNodeGroup) super.clone();
            if (node != null)
                cloned.node = node.clone();
            if (next != null)
                cloned.next = next.clone();
            return cloned;
        }

    }

    public static class QNode1X1 extends QNode implements Cloneable{
        private Table.Column<?> column;
        private Op1x1 op;
        private Object arg;

        public QNode1X1(Column<?> column, Op1x1 op, Object arg) {
            super();
            this.column = column;
            this.op = op;
            this.arg = arg;
        }

        public Table.Column<?> column(){
            return column;
        }

        public Op1x1 op(){
            return op;
        }

        public Object getArg(){
            return arg;
        }

    }

    public static class QNode1xN extends QNode implements Cloneable{
        private Table.Column<?> column;
        private Op1xN op;
        private Collection<?> args;

        public Table.Column<?> column(){
            return column;
        }

        public Op1xN op(){
            return op;
        }

        public Collection<?> getArgs(){
            return args;
        }
    }

    public static class QNodeUnary extends QNode {
        private Table.Column<?> column;
        private OpUnary op;

        public Table.Column<?> column(){
            return column;
        }

        public OpUnary op(){
            return op;
        }
    }

    public class InnerJoin {
        private Table.Column<?> foreignColumn;
        private Op1x1 op;
        private Table.Column<?> column;

        public Table.Column<?> getForeignKey(){
            return foreignColumn;
        }

        public Op1x1 op(){
            return op;
        }

        public Table.Column<?> getColumn(){
            return column;
        }
    }

    /**
     * Operadores unarios. Operam uma coluna.
     */
    public static enum OpUnary {
        ISNULL, NOTNULL;

        public String toString () {
            return
                this == ISNULL   ?  " ISNULL"  :
                this == NOTNULL  ?  " NOTNULL" :
                null;
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
                this == GLOB   ?  " GLOB " :
             /* this == REGEXP ? " REGEXP "; */
                null;
        }
    }

    public static enum Op1xN {
        IN;
        public String toString() throws QueryParseException {
            return
                this == IN   ?  " IN " :
                null;
        }
    }

    public static enum ChainOp {
        AND, OR;
        public String toString() throws QueryParseException {
            return
                this == AND  ?  " AND " :
                this == OR   ?  " OR "  :
                null;
        }
    }

    public static enum OrderByAsc {
        ASC, DESC
    }

}
