package com.quantium.mobile.framework.query;

import java.util.Arrays;
import java.util.Collection;

public class Table {

    private String name;

    public Table (String name){
        this.name  = name;
    }

    public <T> Table.Column<T> addColumn (Class<T> klass, String name){
        Table.Column<T> col = new Table.Column<T>();
        col.klass = klass;
        col.name = name;
        return col;
    }

    /**
     * Gets the name for this instance.
     *
     * @return name.
     */
    public String getName() {
        return this.name;
    }

    public class Column<T> {
        private String name;
        private Class<T> klass;

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

}
