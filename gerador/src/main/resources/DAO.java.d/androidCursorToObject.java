##
##
#set ($primaryKeyIndex = 0)
##
##
    @Override
    public  $Target cursorToObject(Cursor cursor, boolean useCache){
#foreach ($field in $fields)
#**##set ($columnIndex = $foreach.index)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#        Long _${field.LowerCamel} = cursor.getLong(${columnIndex});
#******#        ${association.Klass} _${association.Klass} = null;
#******#        if (!_${field.LowerCamel}.equals((long)${defaultId})) {
#******#            Object cacheItem = factory.cacheLookup(
#******#                ${association.Klass}.class,
#******#                new Serializable[]{_${field.LowerCamel}});
#******#            if (cacheItem == null) {
#******#                LazyInvocationHandler<${association.Klass}> handler =
#******#                    new LazyInvocationHandler<${association.Klass}>(
#******#                        factory.getDaoFor(${association.Klass}.class).query(
#******#                            ${association.Klass}.${association.ReferenceKey.UpperAndUnderscores}.eq(_${field.LowerCamel})),
#******#                        _${field.LowerCamel},
#******#                        "${getter[$field]}");
#******#                _${association.Klass} = (${association.Klass})Proxy.newProxyInstance(
#******#                    this.getClass().getClassLoader(),
#******#                    new Class[]{ ${association.Klass}Editable.class },
#******#                    handler);
#******#            } else if (cacheItem instanceof ${association.Klass}) {
#******#                _${association.Klass} = (${association.Klass})cacheItem;
#******#            }
#******#        }
#******#
#**##else
#******##if ($field.Klass.equals("Boolean") )
#******#        ${field.Type} _${field.LowerCamel} = (cursor.getShort(${columnIndex}) > 0);
#******##elseif ($field.Klass.equals("Date") )
#******#        ${field.Type} _${field.LowerCamel} = DateUtil.stringToDate(cursor.getString(${columnIndex}));
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
#**##if ($primaryKeyIndex.equals($primaryKeys.size()))
#******#        Serializable pks [] = null;
#******#        if (useCache) {
#******#            pks = new Serializable[]{
#******##foreach ($key in $primaryKeys)
#******#                 _${key.LowerCamel},
#******##end
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

        ${KlassImpl} target = new ${KlassImpl}(${constructorArgs});

        if (useCache)
            factory.pushToCache(${Target}.class, pks, target);

        return target;
    }