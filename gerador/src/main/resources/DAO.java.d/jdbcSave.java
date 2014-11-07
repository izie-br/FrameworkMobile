    @Override
    public boolean save($Target target) throws IOException {
        return save(target, Save.INSERT_IF_NULL_PK);
    }

    @Override
    public boolean save($Target target, int flags) throws IOException {
        boolean insertIfNotExists = ((flags&Save.INSERT_IF_NOT_EXISTS) != 0);
        boolean insert = target.${getter[$primaryKey]}() == ${defaultId};
        if (insertIfNotExists){
            try {
            	if (target.${getter[$primaryKey]}() == null) {
            		insert = true;
            	} else {
	                PreparedStatement stm = getStatement(COUNT_BY_PRIMARY_KEYS);
	                stm.setString(1, target.${getter[$primaryKey]}());
	                ResultSet rs = stm.executeQuery();
	                insert = rs.next() && rs.getLong(1) == 0L;
	                rs.close();
            	}
            } catch (java.sql.SQLException e) {
                throw new RuntimeException(StringUtil.getStackTrace(e));
            }
        }
        Serializable pks [] = new Serializable[]{
            target.${getter[$primaryKey]}(),
        };
        if (insert) {
            String value;
            int qty;
            try{
                PreparedStatement stm;
                if (insertIfNotExists) {
                    stm = getStatement(
                            "INSERT INTO ${table} (" +
#foreach ($field in $fields)
#**##if (!$field.PrimaryKey)
#**#                                "${field.LowerAndUnderscores}" + "," +
#**##end
#end
##
                                "${primaryKey.LowerAndUnderscores}" +

############################ A linha abaixo produz por exemplo,
############################ ") VALUES (?,?,?)"
############################ para um bean com o id e mais 3 campos
##
                            ") VALUES (#foreach ($field in $fields)?#if ($foreach.count < $fields.size()),#else)#end#end");
                }
                else
                {
                    stm = getStatement(
                            "INSERT INTO ${table} (" +
#set ($argCount = $fields.size() - 1)
#set ($argIndex = 0)
#foreach ($field in $fields)
#**##if (!$field.PrimaryKey)
#******##set ($argIndex = $argIndex + 1)
#******#                                "${field.LowerAndUnderscores}" +#if ($argIndex < $argCount ) "," +#end
#******#
#**##end
#end
                            ") VALUES (#foreach ($field in $fields)?#if ($foreach.count < $argCount),#else)#break#end#end");
                }

#set ($argIndex = 0)
#foreach ($field in $fields)
#**##if (!$field.PrimaryKey)
#******##set ($argIndex = $argIndex + 1)
#******##if ($associationForField[$field])
#**********##set ($association = $associationForField[$field])
#**********#                stm.set${field.Klass}(
#**********#                        ${argIndex},
#**********#                        (target.get${association.KeyToA}() == null) ?
#**********#                            null :
#**********#                            target.get${association.KeyToA}().get${association.ReferenceKey.UpperCamel}());
#******##elseif (!$field.PrimaryKey)
#**********##if ($field.Klass.equals("Date") )
#**********#                stm.setString(
#**********#                        ${argIndex},
#**********#                        (target.${getter[$field]}() == null) ?
#**********#                            null :
#**********#                            new java.text.SimpleDateFormat(
#**********#                                "yyyy-MM-dd HH:mm:ss.SSS").format(target.${getter[$field]}()));
#**********##else
#**********#                stm.set${field.Klass}(${argIndex}, target.${getter[$field]}());
#**********##end
#******##end
#**##end
#end
##
                if (insertIfNotExists) {
#set ($argIndex = $argIndex + 1)
                    stm.setString(${argIndex}, target.${getter[$primaryKey]}());
                }
                qty = stm.executeUpdate();
                if (qty != 1) {
                    LogPadrao.e("Insert retornou %d", qty);
                    return false;
                }
                value = String.valueOf(qty);
                if (!insertIfNotExists) {
                    ResultSet rs = stm.getGeneratedKeys();
                    value = (rs.next() ) ? rs.getString(1) : null;
                    if (value == null){
                        LogPadrao.e("id '%d' gerado", value);
                        return false;
                    }
                }
            } catch (java.sql.SQLException e){
                throw new IOException(StringUtil.getStackTrace(e));
            }
            if (qty > 0){
                if (target instanceof ${EditableInterface}) {
                    $EditableInterface editable = (${EditableInterface})target;
                    if (!insertIfNotExists) {
                        editable.set${primaryKey.UpperCamel}(value);
                    }
#foreach ($association in $oneToManyAssociations)
#**##if ($association.ReferenceKey)
#******##set ($referenceKey = $association.ReferenceKey)
#**##elseif ($association.IsThisTableA)
#******##set ($referenceKey = $association.ReferenceA)
#**##else
#******##set ($referenceKey = $association.ReferenceB)
#**##end
#**#                    if (editable.get${association.KeyToAPluralized}() == null) {
#**#                        editable.set${association.KeyToAPluralized}(
#**#                            querySetFor${association.KeyToAPluralized}(editable.${getter[$referenceKey]}()));
#**#                    }
#end
#foreach ($association in $manyToManyAssociations)
#**##if ($association.ReferenceKey)
#******##set ($referenceKey = $association.ReferenceKey)
#**##elseif ($association.IsThisTableA)
#******##set ($referenceKey = $association.ReferenceA)
#**##else
#******##set ($referenceKey = $association.ReferenceB)
#**##end
#**#                    if (editable.get${association.Pluralized}() == null) {
#**#                        editable.set${association.Pluralized}(
#**#                            querySetFor${association.Pluralized}(editable.${getter[$referenceKey]}()));
#**#                    }
#end
                    boolean itemFoundInCache = updateCache(target);
                    if (!itemFoundInCache) {
                        pks = new Serializable[]{ target.${getter[$primaryKey]}() };
                        factory.pushToCache(${Target}.class, pks, target);
                    }
                } else {
                    factory.removeFromCache(${Target}.class, pks);
                    LogPadrao.e("${table} nao editavel salvo");
                }
                return true;
            } else {
                return false;
            }
        } else {
            try {
                PreparedStatement stm = getStatement(
                        "UPDATE ${table} SET " +
##
#set ($argCount = $fields.size() - 1)
#set ($argIndex = 0)
#foreach ($field in $fields)
#**##if (!$field.PrimaryKey)
#******##set ($argIndex = $argIndex + 1)
#******#                            "${field.LowerAndUnderscores}=?" +#if ($argIndex < $argCount) ", " +#end
#******#
#**##end
#end
                        " WHERE ${primaryKey.LowerAndUnderscores}=?");
##
#set ($argIndex = 0)
#foreach ($field in $fields)
#**##if (!$field.PrimaryKey)
#******##set ($argIndex = $argIndex + 1)
#******##if ($associationForField[$field])
#**********##set ($association = $associationForField[$field])
#**********#                stm.set${field.Klass}(
#**********#                        ${argIndex},
#**********#                        (target.get${association.KeyToA}() == null) ?
#**********#                            null :
#**********#                            target.get${association.KeyToA}().get${association.ReferenceKey.UpperCamel}());
#******##elseif (!$field.PrimaryKey)
#**********##if ($field.Klass.equals("Date") )
#**********#                stm.setString(
#**********#                        ${argIndex},
#**********#                        (target.${getter[$field]}() == null) ?
#**********#                            null :
#**********#                            new java.text.SimpleDateFormat(
#**********#                                "yyyy-MM-dd HH:mm:ss.SSS").format(target.${getter[$field]}()));
#**********##else
#**********#                stm.set${field.Klass}(${argIndex}, target.${getter[$field]}());
#**********##end
#******##end
#**##end
#end
##
#set ($argIndex = $argIndex + 1)
                stm.setString(${argIndex}, target.${getter[$primaryKey]}());
                int value = stm.executeUpdate();
                if (value == 1) {
                    boolean itemFoundInCache = updateCache(target);
                    if (!itemFoundInCache) {
                        factory.pushToCache(${Target}.class, pks, target);
                    }
                    return true;
                } else {
                    return false;
                }
            } catch (java.sql.SQLException e) {
                LogPadrao.e(e);
                return false;
            }
        }
    }
