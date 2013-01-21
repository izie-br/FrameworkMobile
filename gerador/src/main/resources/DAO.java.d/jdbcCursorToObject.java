##
##
#set ($primaryKeyIndex = 0)
##
##
    @Override
    public  $Target cursorToObject(ResultSet cursor, boolean useCache){
#foreach ($field in $fields)
#**##set ($columnIndex = $foreach.count)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#        Long _${field.LowerCamel};
#******#        try{
#******#            _${field.LowerCamel} = cursor.getLong(${columnIndex});
#******#        } catch (java.sql.SQLException e) {
#******#            throw new RuntimeException(e);
#******#        }
#******#        ${association.Klass} _${association.KeyToA} = null;
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
#******#        ${field.type} _${field.LowerCamel};
#******#        try{
#******##if ($field.Klass.equals("Date") )
#******#            Timestamp temp = cursor.getTimestamp(${columnIndex});
#******#            _${field.LowerCamel} = (temp == null)?
#******#                null :
#******#                new Date(temp.getTime());
#******##else
#******#            _${field.LowerCamel} = cursor.get${field.Klass}(${columnIndex});
#******##end
#******#        } catch (java.sql.SQLException e) {
#******#            throw new RuntimeException(e);
#******#        }
#**##end
#**###
#**##if ($field.PrimaryKey)
#******##set ($primaryKeyIndex = $primaryKeyIndex + 1)
#**##end
#**###
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

        ${KlassImpl} target = new ${KlassImpl}(${constructorArgs});

        if (useCache)
            factory.pushToCache(${Target}.class, pks, target);

        return target;
    }
