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
#******#        String ${field.LowerCamel}_ = cursor.getString(${columnIndex});
#******#        ${association.Klass} ${association.KeyToA}_ = null;
#******#        if (${field.LowerCamel}_ != null) {
#******#            Object cacheItem = factory.cacheLookup(
#******#                ${association.Klass}.class,
#******#                new Serializable[]{${field.LowerCamel}_});
#******#            if (cacheItem == null) {
#******#                LazyInvocationHandler<${association.Klass}> handler =
#******#                    new LazyInvocationHandler<${association.Klass}>(
#******#                        ${association.Klass}.class,
#******#                        factory.getModelFacade(),
#******#                        factory.getDaoFor(${association.Klass}.class).query(
#******#                            ${association.Klass}.${association.ReferenceKey.UpperAndUnderscores}.eq(${field.LowerCamel}_)),
#******#                            ${field.LowerCamel}_,
#******#                            "${getter[$association.ReferenceKey]}");
#******#                ${association.KeyToA}_ = (${association.Klass})Proxy.newProxyInstance(
#******#                    this.getClass().getClassLoader(),
#******#                    new Class[]{ ${association.Klass}Editable.class },
#******#                    handler);
#******#            } else if (cacheItem instanceof ${association.Klass}) {
#******#                ${association.KeyToA}_ = (${association.Klass})cacheItem;
#******#            }
#******#        }
#******#
#**##else
#******##if ($field.Klass.equals("Boolean") )
#******#        ${field.Type} ${field.LowerCamel}_ = parser.booleanFromDatabase(cursor.getShort(${columnIndex}));
#******##elseif ($field.Klass.equals("Date") )
#******#        ${field.Type} ${field.LowerCamel}_ = parser.dateFromDatabase(cursor.getString(${columnIndex}));
#******##elseif ($field.Klass.equals("Long") )
#******#        ${field.Type} ${field.LowerCamel}_ = cursor.getLong(${columnIndex});
#******##elseif ($field.Klass.equals("Double") )
#******#        ${field.Type} ${field.LowerCamel}_ = cursor.getDouble(${columnIndex});
#******##elseif ($field.Klass.equals("String") )
#******#        ${field.Type} ${field.LowerCamel}_ = cursor.getString(${columnIndex});
#******##end
#**##end
#**##if ($field.PrimaryKey)
#******##set ($primaryKeyIndex = $primaryKeyIndex + 1)
#**##end
#**##if ($primaryKeyIndex.equals(1))
#******#        Serializable pks [] = null;
#******#        if (useCache) {
#******#            pks = new Serializable[]{
#******#                 ${primaryKey.LowerCamel}_,
#******#            };
#******#            Object cacheItem = factory.cacheLookup(${Target}.class, pks);
#******#            if (cacheItem != null &&
#******#                (cacheItem instanceof ${Target}))
#******#            {
#******#                return (${Target})cacheItem;
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

