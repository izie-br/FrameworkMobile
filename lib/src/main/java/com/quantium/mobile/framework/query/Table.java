package com.quantium.mobile.framework.query;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.quantium.mobile.framework.validation.Constraint;

public final class Table {

    /**
     * Collection de instancias de Table em uso.
     */
    private static final ArrayList<WeakReference<Table>> POOL =
            new ArrayList<WeakReference<Table>> ();
    private static final Lock POOL_LOCK = new ReentrantLock ();

    private final String name;
    private final ArrayList<Table.Column<?>> columns =
        new ArrayList<Table.Column<?>> ();
    private final ArrayList<Constraint> constraints =
        new ArrayList<Constraint> ();

    private Table (String name){
        this.name  = name;
    }

    private void addColumn (
            Class<?> klass, String name,
            Constraint...constraints)
    {
        if (constraints == null)
            constraints = new Constraint[0];
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Table.Column col =
            new Table.Column(name, klass, constraints);
        Table.this.columns.add (col);
    }

    private void addConstraint (Constraint constraint) {
        Table.this.constraints.add (constraint);
    }

    public static Builder create(String name) {
        // Cria uma Table, e a partir dela, um Builder, que eh
        // uma innerclass de Table.
        // Note a sintaxe complexa "table.new InnerClass()"
        return new Table (name).new Builder();
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

        public Q like(String pattern){
            return Q.like(this, pattern);
        }

        public Q isNull () {
            return new Q(this, Q.OpUnary.ISNULL);
        }

        public Q isNotNull () {
            return new Q(this, Q.OpUnary.NOTNULL);
        }

    }

    public final class Builder {

        public <T> Builder addColumn (Class<T> klass, String name,
                Constraint...constraints)
        {
            Table.this.addColumn (klass, name, constraints);
            return this;
        }

        public Builder addConstraint (Constraint constraint) {
            Table.this.addConstraint (constraint);
            return this;
        }

        public Table get () {
            POOL_LOCK.lock ();
            try {
                // busca uma tabela igual na POOL
                Iterator<WeakReference<Table>> it = POOL.iterator ();
                while (it.hasNext ()) {
                    Table poolTable = it.next ().get ();
                    if (poolTable == null) {
                        it.remove ();
                        continue;
                    }
                    if (tableEquals (poolTable))
                        return poolTable;
                }
                // se nao ha esta tabela na POOL
                Table returnedTable = copyFromThis ();
                POOL.add (new WeakReference<Table> (returnedTable));
                return returnedTable;
            } finally {
                POOL_LOCK.unlock ();
            }
        }

        private Table copyFromThis () {
            Table returnedTable = new Table (Table.this.name);
            for (Table.Column<?> col : Table.this.columns) {
                Constraint array [] =
                    new Constraint[col.constraints.size ()];
                col.constraints.toArray (array);
                returnedTable.addColumn (col.klass, col.name, array);
            }
            for (Constraint constraint : Table.this.constraints) {
                returnedTable.addConstraint (constraint);
            }
            return returnedTable;
        }

        private boolean tableEquals (Table t2) {
            if (Table.this == t2)
                return true;
            if (!Table.this.name.equals (t2.name))
                return false;
            if (Table.this.columns.size () != t2.columns.size())
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

        private boolean tableHaveColumn (Table.Column<?> column) {
            for (Table.Column<?> col : Table.this.columns) {
                if (col.name.equals (column.name) &&
                    col.klass.equals (column.klass) &&
                    constraintsEquals (col.constraints, column.constraints) )
                {
                    return true;
                }
            }
            return false;
        }
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
