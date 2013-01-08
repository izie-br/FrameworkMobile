    @Override
    public QuerySet<${Target}> query() {
        return query(null);
    }

    @Override
    public QuerySet<${Target}> query(Q q) {
        QuerySet<${Target}> queryset =
            new QuerySetImpl();
        if (q == null) {
            return queryset;
        }
        return queryset.filter(q);
    }

    private final class QuerySetImpl extends SQLiteQuerySet<${Target}> {

        @Override
        protected SQLiteDatabase getDb() {
            return ${Klass}.this.factory.getDb();
        }

        @Override
        public Table getTable() {
            return ${Target}._TABLE;
        }

        @Override
        protected Table.Column<?> [] getColunas() {
            final Table.Column<?>[] columns = {
#foreach ($field in $fields)
                ${Target}.${field.UpperAndUnderscores},
#end
            };
            return columns;
        }

        @Override
        protected ${Target} cursorToObject(Cursor cursor) {
            return ${Klass}.this.cursorToObject(cursor, true);
        }
    }
