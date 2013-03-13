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
        public ${Target} first() {
            List<${Target}> cache = factory.loadQFromCache(${Target}.class, this.q);
            if (cache != null && cache.size() == 1) {
                return cache.get(0);
            } else {
                return super.first();
            }
        }

        @Override
        public List<${Target}> all() {
            List<${Target}> cache = factory.loadQFromCache(${Target}.class, this.q);
            if (cache != null) {
                return cache;
            } else {
                return super.all();
            }
        }

        @Override
        public long count() {
            List<${Target}> cache = factory.loadQFromCache(${Target}.class, this.q);
            if (cache != null) {
                return cache.size();
            } else {
                return super.count();
            }
        }

        @Override
        protected Connection getConnection() {
            return ${Klass}.this.factory.getConnection();
        }

        @Override
        public Table getTable() {
            return ${Target}._TABLE;
        }

        @Override
        protected List<Table.Column<?>> getColumns() {
            return COLUMNS;
        }

        protected ${Target} cursorToObject(ResultSet cursor) {
            return ${Klass}.this.cursorToObject(cursor, true);
        }

    }
