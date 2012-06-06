package br.com.cds.mobile.framework.query;

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

        public Q ne(T arg){
            return new Q(this, Q.Op1x1.NE, arg);
        }

        public Q lt(T arg){
            return new Q(this, Q.Op1x1.LT, arg);
        }

        public Q le(T arg){
            return new Q(this, Q.Op1x1.LE, arg);
        }

        public Q gt(T arg){
            return new Q(this, Q.Op1x1.GT, arg);
        }

        public Q ge(T arg){
            return new Q(this, Q.Op1x1.GE, arg);
        }

        public Q in(T arg){
            return new Q(this, Q.Op1x1.IN, arg);
        }

    }

}
