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
#******#        String _${field.LowerCamel} = cursor.getString(${columnIndex});
#******#        ${association.Klass} _${association.KeyToA} = null;
#******#        if (_${field.LowerCamel} != null) {
#******#            Object cacheItem = factory.cacheLookup(
#******#                ${association.Klass}.class,
#******#                new Serializable[]{_${field.LowerCamel}});
#******#            if (cacheItem == null) {
#******#                LazyInvocationHandler<${association.Klass}> handler =
#******#                    new LazyInvocationHandler<${association.Klass}>(
#******#                        factory.getDaoFor(${association.Klass}.class).query(
#******#                            ${association.Klass}.${association.ReferenceKey.UpperAndUnderscores}.eq(_${field.LowerCamel})),
#******#                            _${field.LowerCamel},
#******#                            "${getter[$association.ReferenceKey]}");
#******#                _${association.KeyToA} = (${association.Klass})Proxy.newProxyInstance(
#******#                    this.getClass().getClassLoader(),
#******#                    new Class[]{ ${association.Klass}Editable.class },
#******#                    handler);
#******#            } else if (cacheItem instanceof ${association.Klass}) {
#******#                _${association.KeyToA} = (${association.Klass})cacheItem;
#******#            }
#******#        }
#******#
#**##else
#******##if ($field.Klass.equals("Boolean") )
#******#        ${field.Type} _${field.LowerCamel} = parser.booleanFromDatabase(cursor.getShort(${columnIndex}));
#******##elseif ($field.Klass.equals("Date") )
#******#        ${field.Type} _${field.LowerCamel} = parser.dateFromDatabase(cursor.getString(${columnIndex}));
#******##elseif ($field.Klass.equals("Long") )
#******#        ${field.Type} _${field.LowerCamel} = cursor.getLong(${columnIndex});
#******##elseif ($field.Klass.equals("Double") )
#******#        ${field.Type} _${field.LowerCamel} = cursor.getDouble(${columnIndex});
#******##elseif ($field.Klass.equals("String") )
#******#        ${field.Type} _${field.LowerCamel} = cursor.getString(${columnIndex});
#******##end
#**##end
#**##if ($field.PrimaryKey)
#******##set ($primaryKeyIndex = $primaryKeyIndex + 1)
#**##end
#**##if ($primaryKeyIndex.equals(1))
#******#        Serializable pks [] = null;
#******#        if (useCache) {
#******#            pks = new Serializable[]{
#******#                 _${primaryKey.LowerCamel},
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

