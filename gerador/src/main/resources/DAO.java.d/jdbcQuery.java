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

    private final class QuerySetImpl extends JdbcQuerySet<${Target}> {

        @Override
        protected Connection getConnection() {
            return ${Klass}.this.factory.getConnection();
        }

        @Override
        public Table getTable() {
            return ${Target}._TABLE;
        }

        @Override
        protected Table.Column<?> [] getColunas() {
            return new Table.Column[] {
#foreach ($field in $fields)
                ${Target}.${field.UpperAndUnderscores},
#end
            };
        }

        protected ${Target} cursorToObject(ResultSet cursor) {
            return ${Klass}.this.cursorToObject(cursor, true);
        }

    }
