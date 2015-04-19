##
##
#set ($primaryKeyIndex = 0)
##
##
    @Override
    public  $Target cursorToObject(Cursor cursor, boolean useCache){
		ValueParser parser = this.factory.getValueParser();
#foreach ($field in $fields)
#**##set ($columnIndex = $foreach.index)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#        String ${field.LowerCamel}AG = hasColumn(cursor, ${Target}.${field.UpperAndUnderscores}.getName()) ? cursor.getString(cursor.getColumnIndex(${Target}.${field.UpperAndUnderscores}.getName())) : null;
#******#        ${association.Klass} ${association.KeyToA}AG = null;
#******#        if (${field.LowerCamel}AG != null) {
#******#            Object cacheItem = factory.cacheLookup(
#******#                ${association.Klass}.class,
#******#                new Serializable[]{${field.LowerCamel}AG});
#******#            if (cacheItem == null) {
#******#                LazyInvocationHandler<${association.Klass}> handler =
#******#                    new LazyInvocationHandler<${association.Klass}>(
#******#                        ${association.Klass}.class,
#******#                        factory.getModelFacade(),
#******#                        factory.getDaoFor(${association.Klass}.class).query(
#******#                            ${association.Klass}.${association.ReferenceKey.UpperAndUnderscores}.eq(${field.LowerCamel}AG)),
#******#                            ${field.LowerCamel}AG,
#******#                            "${getter[$association.ReferenceKey]}");
#******#                ${association.KeyToA}AG = (${association.Klass})Proxy.newProxyInstance(
#******#                    this.getClass().getClassLoader(),
#******#                    new Class[]{ ${association.Klass}Editable.class },
#******#                    handler);
#******#            } else if (cacheItem instanceof ${association.Klass}) {
#******#                ${association.KeyToA}AG = (${association.Klass}) cacheItem;
#******#            }
#******#        }
#******#
#**##else
#******##if ($field.Klass.equals("Boolean") )
#******#        ${field.Type} ${field.LowerCamel}AG =
#******#            hasColumn(
#******#                cursor,
#******#                ${Target}.${field.UpperAndUnderscores}.getName()) ?
#******#                parser.booleanFromDatabase(
#******#                cursor.getShort(
#******#                cursor.getColumnIndex(
#******#                ${Target}.${field.UpperAndUnderscores}.getName())))
#******#                : false;
#******##elseif ($field.Klass.equals("Date") )
#******#        ${field.Type} ${field.LowerCamel}AG =
#******#            hasColumn(
#******#                cursor,
#******#                ${Target}.${field.UpperAndUnderscores}.getName()) ?
#******#                parser.dateFromDatabase(
#******#                cursor.getString(
#******#                cursor.getColumnIndex(
#******#                ${Target}.${field.UpperAndUnderscores}.getName())))
#******#                : null;
#******##elseif ($field.Klass.equals("Long") )
#******#        ${field.Type} ${field.LowerCamel}AG =
#******#            hasColumn(
#******#                cursor,
#******#                ${Target}.${field.UpperAndUnderscores}.getName()) ?
#******#                cursor.getLong(
#******#                cursor.getColumnIndex(${Target}.${field.UpperAndUnderscores}.getName()))
#******#                : 0l;
#******##elseif ($field.Klass.equals("Double") )
#******#        ${field.Type} ${field.LowerCamel}AG =
#******#            hasColumn(
#******#                cursor,
#******#                ${Target}.${field.UpperAndUnderscores}.getName()) ?
#******#                cursor.getDouble(
#******#                cursor.getColumnIndex(
#******#                ${Target}.${field.UpperAndUnderscores}.getName()))
#******#                : 0.0d;
#******##elseif ($field.Klass.equals("String") )
#******#        ${field.Type} ${field.LowerCamel}AG =
#******#            hasColumn(cursor,
#******#                ${Target}.${field.UpperAndUnderscores}.getName()) ?
#******#                cursor.getString(
#******#                cursor.getColumnIndex(
#******#                ${Target}.${field.UpperAndUnderscores}.getName()))
#******#                : null;
#******##end
#**##end
#**##if ($field.PrimaryKey)
#******##set ($primaryKeyIndex = $primaryKeyIndex + 1)
#**##end
#**##if ($primaryKeyIndex.equals(1))
#******#        Serializable pks [] = null;
#******#        if (useCache && ${primaryKey.LowerCamel}AG != null) {
#******#            pks = new Serializable[]{
#******#                 ${primaryKey.LowerCamel}AG,
#******#            };
#******#            Object cacheItem = factory.cacheLookup(${Target}.class, pks);
#******#            if (cacheItem != null &&
#******#                (cacheItem instanceof ${Target})) {
#******#                return (${Target}) cacheItem;
#******#            }
#******#        }
#******##set ($primaryKeyIndex = 0)
#**##end
#end

        ${Target} target = new${Target}(${constructorArgs});

        if (useCache)
            factory.pushToCache(${Target}.class, pks, target);

        return target;
    }


    public boolean hasColumn(Cursor cursor, String columnName) throws SQLException {
        return cursor.getColumnIndex(columnName) > -1;
    }
