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
#******#        String ${field.LowerCamel}_ = null;
#******#        try{
#******#            if (hasColumn(cursor, ${Target}.${field.UpperAndUnderscores}.getName())) {
#******#                ${field.LowerCamel}_ = cursor.getString(${Target}.${field.UpperAndUnderscores}.getName());
#******#            }
#******#        } catch (java.sql.SQLException e) {
#******#            throw new RuntimeException(e);
#******#        }
#******#        ${association.Klass} ${association.KeyToA}_ = null;
#******#        if (${field.LowerCamel}_ != ${defaultId}) {
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
#******##if ($field.Klass.equals("Date") )
#******#        ${field.type} ${field.LowerCamel}_ = null;
#******#        try{
#******#            try {
#******#                if (hasColumn(cursor, ${Target}.${field.UpperAndUnderscores}.getName())) {
#******#                    String temp = cursor.getString(${Target}.${field.UpperAndUnderscores}.getName());
#******#                    ${field.LowerCamel}_ = (temp == null)?
#******#                        null :
#******#                        new java.text.SimpleDateFormat(
#******#                            "yyyy-MM-dd HH:mm:ss.SSS").parse(temp);
#******#                }
#******#            } catch (java.text.ParseException p) {
#******#                throw new RuntimeException(p);
#******#            }
#******##else
#******##if ($field.Klass.equals("Long") )
#******#        ${field.type} ${field.LowerCamel}_ = 0;
#******##elseif ($field.Klass.equals("Boolean") )
#******#        ${field.type} ${field.LowerCamel}_ = false;
#******##else
#******#        ${field.type} ${field.LowerCamel}_ = null;
#******##end
#******#        try{
#******#            if (hasColumn(cursor, ${Target}.${field.UpperAndUnderscores}.getName())) {
#******#                ${field.LowerCamel}_ = cursor.get${field.Klass}(${Target}.${field.UpperAndUnderscores}.getName());
#******#            }
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
#******#        if (useCache && id_ != null) {
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
