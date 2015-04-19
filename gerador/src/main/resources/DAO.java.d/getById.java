    @Override
    public ${Target} get(Object id) throws IOException {
        if (id instanceof ${primaryKey.Klass}) {
            return query(${Target}.${primaryKey.UpperAndUnderscores}.eq((${primaryKey.Klass}) id)).first ();
        }
        throw new RuntimeException (
            "id is not valid:" + id + (id == null ? "" :
                " from class " + id.getClass().getSimpleName()));
    }
