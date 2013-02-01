    @Override
    public ${Target} get(Object id) {
        if (id instanceof ${primaryKey.Klass}) {
            return query (${Target}.${primaryKey.UpperAndUnderscores}.eq ((${primaryKey.Klass})id)).first ();
        }
        throw new RuntimeException ();
    }
