package com.quantium.mobile.framework.query;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.quantium.mobile.framework.validation.Constraint;

public final class Table {

    /**
     * Collection de instancias de Table em uso.
     */
    private static final ArrayList<WeakReference<Table>> POOL =
            new ArrayList<WeakReference<Table>> ();

    private ReadWriteLock lock = new ReentrantReadWriteLock ();

    private final String name;
    private final ArrayList<Table.Column<?>> columns =
        new ArrayList<Table.Column<?>> ();
    private final ArrayList<Constraint> constraints =
        new ArrayList<Constraint> ();

    private Table (String name){
        this.name  = name;
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

    public class Builder {

        public <T> Builder addColumn (Class<T> klass, String name,
                Constraint...constraints)
        {
            Lock l1 = Table.this.lock.writeLock ();
            l1.lock ();
            {
                if (constraints == null)
                    constraints = new Constraint[0];
                Table.Column<T> col =
                    new Table.Column<T>(name, klass, constraints);
                Table.this.columns.add (col);
            }
            l1.unlock ();
            return this;
        }

        public Builder addConstraint (Constraint constraint) {
            Lock l1 = Table.this.lock.writeLock ();
            l1.lock ();
            {
                Table.this.constraints.add (constraint);
            }
            l1.unlock ();
            return this;
        }

        public Table get () {
            synchronized (POOL) {
                // busca uma tabela igual na POOL
                for (WeakReference<Table> poolReference : POOL) {
                    Table poolTable = poolReference.get ();
                    if (poolTable == null) {
                        POOL.remove (poolReference);
                        continue;
                    }
                    if (tableEquals (poolTable))
                        return poolTable;
                }
                // se nao ha esta tabela na POOL
                POOL.add (new WeakReference<Table> (Table.this));
                return Table.this;
            }
        }

        private boolean tableEquals (Table t2) {
            Lock l1 = Table.this.lock.readLock ();
            l1.lock ();
            Lock l2 = t2.lock.readLock ();
            l2.lock ();
            try {
                if (Table.this == t2)
                    return true;
                if (Table.this.name.equals (t2.name))
                    return false;
                if (Table.this.columns.size () != t2.columns.size())
                    return false;
                for (Table.Column<?> col : t2.columns) {
                    if (!tableHaveColumn (col))
                        return false;
                }
                return true;
            } finally {
                l1. unlock();
                l2.unlock ();
            }
        }

        // TODO tratar constraints
        private boolean tableHaveColumn (Table.Column<?> column) {
            for (Table.Column<?> col : Table.this.columns) {
                if (col.name.equals (column.name) &&
                    col.klass.equals (column.klass) &&
                    constraintsEquals (col, column) )
                {
                    return true;
                }
            }
            return false;
        }

        private boolean constraintsEquals (
        		Table.Column<?> c1, Table.Column<?> c2)
        {
            if ( !(c1.constraints.size () == c2.constraints.size ()) )
                return false;

            // Note esta label do loop
            constraint1_loop:
            for (Constraint constraint1 : c1.constraints) {
                for (Constraint constraint2 : c2.constraints) {
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
}
