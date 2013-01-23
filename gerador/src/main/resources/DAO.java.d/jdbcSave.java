    @Override
    public boolean save($Target target) throws IOException {
        return save(target, Save.INSERT_IF_NULL_PK);
    }

    @Override
    public boolean save($Target target, int flags) throws IOException {
        boolean insertIfNotExists = ( (flags&Save.INSERT_IF_NOT_EXISTS) != 0);
        boolean insert = target.${getter[$primaryKey]}() == ${defaultId};
        if (insertIfNotExists){
            try {
                PreparedStatement stm = getStatement(COUNT_BY_PRIMARY_KEYS);
                stm.setLong(1, target.${getter[$primaryKey]}());
                ResultSet rs = stm.executeQuery();
                insert = rs.next() && rs.getLong(1) == 0L;
                rs.close();
            } catch (java.sql.SQLException e) {
                throw new RuntimeException(StringUtil.getStackTrace(e));
            }
        }
        Serializable pks [] = new Serializable[]{
            target.${getter[$primaryKey]}(),
        };
        if (insert) {
            long value;
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
#**********#                            0 :
#**********#                            target.get${association.KeyToA}().get${association.ReferenceKey.UpperCamel}());
#******##elseif (!$field.PrimaryKey)
#**********##if ($field.Klass.equals("Date") )
#**********#                stm.setTimestamp(
#**********#                        ${argIndex},
#**********#                        (target.${getter[$field]}() == null) ?
#**********#                            null :
#**********#                            new java.sql.Timestamp(target.${getter[$field]}().getTime()));
#**********##else
#**********#                stm.set${field.Klass}(${argIndex}, target.${getter[$field]}());
#**********##end
#******##end
#**##end
#end
##
                if (insertIfNotExists) {
#set ($argIndex = $argIndex + 1)
                    stm.setLong(${argIndex}, target.${getter[$primaryKey]}());
                }
                int qty = stm.executeUpdate();
                if (qty != 1) {
                    LogPadrao.e("Insert retornou %d", qty);
                    return false;
                }
                value = qty;
                if (!insertIfNotExists) {
                    ResultSet rs = stm.getGeneratedKeys();
                    value = (rs.next() ) ? rs.getLong(1) : -1;
                    if (value <= 0){
                        LogPadrao.e("id '%d' gerado", value);
                        return false;
                    }
                }
            } catch (java.sql.SQLException e){
                throw new IOException(StringUtil.getStackTrace(e));
            }
            if (value > 0){
                if (target instanceof ${EditableInterface}) {
                    $EditableInterface editable = (${EditableInterface})target;
                    editable.set${primaryKey.UpperCamel}(value);
#foreach ($association in $oneToManyAssociations)
#**##if ($association.ReferenceKey)
#******##set ($referenceKey = $association.ReferenceKey)
#**##elseif ($association.IsThisTableA)
#******##set ($referenceKey = $association.ReferenceA)
#**##else
#******##set ($referenceKey = $association.ReferenceB)
#**##end
#**#                    if (editable.get${association.KeyToAPluralized}() == null) {
#**#                        editable.set${association.KeyToAPluralized}(querySetFor${association.KeyToAPluralized}(editable.${getter[$referenceKey]}()));
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
#**#                        editable.set${association.Pluralized}(querySetFor${association.Pluralized}(editable.${getter[$referenceKey]}()));
#**#                    }
#end
                    pks = new Serializable[]{ value };
                    factory.pushToCache(${Target}.class, pks, target);
                } else {
                    factory.removeFromCache(${Target}.class, pks);
                    LogPadrao.e(String.format("%s nao editavel salvo", target.getClass().getName()));
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
#**********#                            0 :
#**********#                            target.get${association.KeyToA}().get${association.ReferenceKey.UpperCamel}());
#******##elseif (!$field.PrimaryKey)
#**********##if ($field.Klass.equals("Date") )
#**********#                stm.setTimestamp(
#**********#                        ${argIndex},
#**********#                        (target.${getter[$field]}() == null) ?
#**********#                            null :
#**********#                            new java.sql.Timestamp(target.${getter[$field]}().getTime()));
#**********##else
#**********#                stm.set${field.Klass}(${argIndex}, target.${getter[$field]}());
#**********##end
#******##end
#**##end
#end
##
#set ($argIndex = $argIndex + 1)
                stm.setLong(${argIndex}, target.${getter[$primaryKey]}());
                int value = stm.executeUpdate();
                if (value == 1) {
                    factory.pushToCache(${Target}.class, pks, target);
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
