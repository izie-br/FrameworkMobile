package com.quantium.mobile.framework.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.quantium.mobile.framework.validation.Constraint;

public final class Table {

    private final String name;
    private final ArrayList<Table.Column<?>> columns =
        new ArrayList<Table.Column<?>> ();
    private final ArrayList<Constraint> constraints =
        new ArrayList<Constraint> ();

    public Table (String name){
        this.name  = name;
    }

    public <T> Table.Column<T> addColumn (
            Class<T> klass, String name,
            Constraint...constraints)
    {
        if (constraints == null)
            constraints = new Constraint[0];
        Table.Column<T> col =
            new Table.Column<T>(name, klass, constraints);
        Table.this.columns.add (col);
        return col;
    }

    public void addConstraint (Constraint constraint) {
        this.constraints.add (constraint);
    }

    /**
     * Nome da tabela
     *
     * @return name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * colunas da tabela
     * 
     * @return columns as an unmodifiable collection.
     */
    public Collection<Table.Column<?>> getColumns () {
        return Collections.unmodifiableCollection (this.columns);
    }

    public <T> Table.Column<T> findColumn(Class<T> klass, String name) {
        for (Table.Column<?> col: this.columns) {
            if (col.name.equals (name) &&
                col.klass.equals (klass))
            {
                @SuppressWarnings("unchecked")
                Table.Column<T> columnFound = (Table.Column<T>)col;
                return  columnFound;
            }
        }
        throw new RuntimeException (String.format(
            "Column '%s' '%s' not found", name, klass.getSimpleName ()));
    }

    /**
     * Constraints associadas a tabela.
     * Complemento para as constraints das colunas,
     * necessario para o caso de constraints envolvendo mais de
     * uma coluna ( Ex.: UNIQUE(col1,col2) )
     * 
     * @return table constraints as an unmodifiable collection.
     */
    public Collection<Constraint> getConstraints () {
        return Collections.unmodifiableCollection (this.constraints);
    }

    @Override
    public int hashCode() {
        return (this.getName () == null)? 0 : this.name.hashCode ();
    }

    @Override
    public boolean equals (Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Table))
            return false;
        Table t2 = (Table)obj;
        if (!this.name.equals (t2.name))
            return false;
        if (this.columns.size () != t2.columns.size())
            return false;
        for (Table.Column<?> col : t2.columns) {
            if (!tableHaveColumn (col))
                return false;
        }
        if (!constraintsEquals (
             Table.this.constraints,
             t2.constraints))
        {
            return false;
        }
        return true;
    }


    public final class Column<T> {

        private final String name;
        private final Class<T> klass;
        private final Collection<Constraint> constraints;

        public Column(String name, Class<T> klass, Constraint...constraints) {
            this.name = name;
            this.klass = klass;
            if (constraints == null || constraints.length == 0) {
                this.constraints = new ArrayList<Constraint> (0);
            } else {
                this.constraints =
                    new ArrayList<Constraint> (constraints.length);
                for (Constraint constraint : constraints) {
                    this.constraints.add (constraint);
                }
            }
        }

        /**
         * Gets the name for this instance.
         *
         * @return name.
         */
        public String getName() {
            return this.name;
        }

        public Class<T> getKlass() {
            return klass;
        }

        public Collection<Constraint> getConstraintList () {
            return Collections.unmodifiableCollection (this.constraints);
        }

        public Table getTable(){
            return Table.this;
        }

        public Q eq(T arg){
            return new Q(this, Q.Op1x1.EQ, arg);
        }

        public Q eq(Table.Column<?> arg){
            return new Q(this, Q.Op1x1.EQ, arg);
        }

        public Q eqlj(Table.Column<?> arg){
            return new Q(this, Q.Op1x1.EQLF, arg);
        }

        public Q ne(T arg){
            return new Q(this, Q.Op1x1.NE, arg);
        }

        public Q ne(Table.Column<?> arg){
            return new Q(this, Q.Op1x1.NE, arg);
        }

        public Q lt(T arg){
            return new Q(this, Q.Op1x1.LT, arg);
        }

        public Q lt(Table.Column<?> arg){
            return new Q(this, Q.Op1x1.LT, arg);
        }

        public Q le(T arg){
            return new Q(this, Q.Op1x1.LE, arg);
        }

        public Q le(Table.Column<?> arg){
            return new Q(this, Q.Op1x1.LE, arg);
        }

        public Q gt(T arg){
            return new Q(this, Q.Op1x1.GT, arg);
        }

        public Q gt(Table.Column<?> arg){
            return new Q(this, Q.Op1x1.GT, arg);
        }

        public Q ge(T arg){
            return new Q(this, Q.Op1x1.GE, arg);
        }

        public Q ge(Table.Column<?> arg){
            return new Q(this, Q.Op1x1.GE, arg);
        }

        public Q in(T...array){
            Collection<T> col = Arrays.asList(array);
            return in(col);
        }

        public Q in(Collection<T> args){
            return new Q(this, Q.Op1xN.IN, args);
        }
        
        public Q notIn(T...array){
            Collection<T> col = Arrays.asList(array);
            return notIn(col);
        }

        public Q notIn(Collection<T> args){
            return new Q(this, Q.Op1xN.NOT_IN, args);
        }

        public Q like(String pattern){
            return Q.like(this, pattern);
        }

        public Q isNull () {
            return new Q(this, Q.OpUnary.ISNULL);
        }

        public Q isNotNull () {
            return new Q(this, Q.OpUnary.NOTNULL);
        }

        public Q.OrderByClause asc() {
            return new Q.OrderByClause(this, Q.ASC);
        }

        public Q.OrderByClause desc() {
            return new Q.OrderByClause(this, Q.DESC);
        }

        public Q.GroupByClause sum() {
            return new Q.GroupByClause(this, Q.SUM);
        }

        public Q.GroupByClause min() {
            return new Q.GroupByClause(this, Q.MIN);
        }

        public Q.GroupByClause max() {
            return new Q.GroupByClause(this, Q.MAX);
        }

        public Q.GroupByClause avg() {
            return new Q.GroupByClause(this, Q.AVG);
        }

        public Q.GroupByClause count() {
            return new Q.GroupByClause(this, Q.COUNT);
        }

        public Q.GroupByClause custom(String custom) {
            return new Q.GroupByClause(this, Q.CUSTOM, custom);
        }

        @Override
        public int hashCode() {
            return Table.this.getName().hashCode() * this.getName().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null)
                return false;
            if (obj.getClass() != this.getClass())
                return false;
            Table.Column<?> other = (Table.Column<?>) obj;
            if (!Table.this.getName().equals(other.getTable().getName()))
                return false;
            return (this.name.equals (other.name) &&
                this.klass.equals (other.klass) &&
                constraintsEquals (this.constraints, other.constraints));
        }
    }

    private boolean tableHaveColumn (Table.Column<?> column) {
        for (Table.Column<?> col : Table.this.columns) {
            if (col.equals(column)) {
                return true;
            }
        }
        return false;
    }

    private static boolean constraintsEquals (
            Collection<Constraint> constraints1,
            Collection<Constraint> constraints2)
        {
            if ( !(constraints1.size () == constraints2.size ()) )
                return false;

            // Note esta label do loop
            constraint1_loop:
            for (Constraint constraint1 : constraints1) {
                for (Constraint constraint2 : constraints2) {
                    if ( constraint1.equals (constraint2) ) {
                        // pula para proxima constraint1, usando a 'label'
                        continue constraint1_loop;
                    }
                }
                // se uma das constraint nao foi encontrada
                return false;
            }
            return true;
        }

}
