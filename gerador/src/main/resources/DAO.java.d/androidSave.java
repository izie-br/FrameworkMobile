    @Override
    public boolean save($Target target) throws IOException {
        return save(target, Save.INSERT_IF_NULL_PK);
    }

    @Override
    public boolean save($Target target, int flags) throws IOException {
#if ($compoundPk)
        if (${nullPkCondition}) {
            return false;
        }
#end
        ContentValues contentValues = new ContentValues();
#foreach ($field in $fields)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#        contentValues.put("${field.LowerAndUnderscores}",
#******#                          (target.get${association.Klass}() == null) ? 0 : target.get${association.Klass}().get${association.ReferenceKey.UpperCamel}());
#**##elseif ($compoundPk || !$primaryKey.equals($field))
#******##if ($field.Klass.equals("Date") )
#******#        contentValues.put("${field.LowerAndUnderscores}",
#******#                          DateUtil.timestampToString(target.${getter[$field]}()));
#******##elseif ($field.Klass.equals("Boolean") )
#******#        contentValues.put("${field.LowerAndUnderscores}", (target.${getter[$field]}())?1:0 );
#******##else
#******#        contentValues.put("${field.LowerAndUnderscores}", target.${getter[$field]}());
#******##end
#**##end
#end
        SQLiteDatabase db = this.factory.getDb();
        boolean insert;
        String queryByPrimaryKey = "${queryByPrimaryKey}";
        String primaryKeysArgs [] = ${primaryKeysArgs};
#if (!$compoundPk)
        boolean insertIfNotExists = ( (flags&Save.INSERT_IF_NOT_EXISTS) != 0);
        insert = target.${getter[$primaryKey]}() == ${defaultId};
        if (insertIfNotExists)
#end
        {
            Cursor cursor = this.factory.getDb().rawQuery(
                "SELECT COUNT(*) FROM ${table} WHERE " + queryByPrimaryKey,
                primaryKeysArgs);
            insert = cursor.moveToNext() && cursor.getLong(0) == 0L;
            cursor.close();
        }
        Serializable pks [] = new Serializable[]{
#foreach ($field in $primaryKeys)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#            target.get${association.Klass}().get${association.ReferenceKey.UpperCamel}(),
#**##else
#******#            target.${getter[$field]}(),
#**##end
#end
        };
        if (insert) {
#if (!$compoundPk)
            if (insertIfNotExists) {
                contentValues.put("${primaryKey.LowerAndUnderscores}", target.${getter[$primaryKey]}());
            }
#end
            long value;
            try{
                value = db.insertOrThrow("${table}", null, contentValues);
            } catch (SQLException e){
                throw new IOException(StringUtil.getStackTrace(e));
            }
#if ($compoundPk)
#**#            if (value > 0) {
#**#                if (target instanceof ${EditableInterface}) {
#**##if ($toManyAssociations.size() > 0)
#**#                    $EditableInterface editable = (${EditableInterface})target;
#**##end
#**##foreach ($association in $toManyAssociations)
#******##if ($association.ReferenceKey)
#**********##set ($referenceKey = $association.ReferenceKey)
#******##elseif ($association.IsThisTableA)
#**********##set ($referenceKey = $association.ReferenceA)
#******##else
#**********##set ($referenceKey = $association.ReferenceB)
#******##end
#******#                    if (editable.get${association.Pluralized}() == null) {
#******#                        editable.set${association.Pluralized}(querySetFor${association.Pluralized}(editable.${getter[$referenceKey]}()));
#******#                    }
#**##end
#**#                    factory.pushToCache(${Target}.class, pks, target);
#**#                } else {
#**#                    factory.removeFromCache(${Target}.class, pks);
#**#                    LogPadrao.e(String.format("%s nao editavel salvo", target.getClass().getName()));
#**#                }
#**#                return true;
#**#            } else {
#**#                return false;
#**#            }
#else
#**#            if (value > 0){
#**#                if (target instanceof ${EditableInterface}) {
#**#                    $EditableInterface editable = (${EditableInterface})target;
#**#                    editable.set${primaryKey.UpperCamel}(value);
#**##foreach ($association in $toManyAssociations)
#******##if ($association.ReferenceKey)
#**********##set ($referenceKey = $association.ReferenceKey)
#******##elseif ($association.IsThisTableA)
#**********##set ($referenceKey = $association.ReferenceA)
#******##else
#**********##set ($referenceKey = $association.ReferenceB)
#******##end
#******#                    if (editable.get${association.Pluralized}() == null) {
#******#                        editable.set${association.Pluralized}(querySetFor${association.Pluralized}(editable.${getter[$referenceKey]}()));
#******#                    }
#**##end
#**#                    pks = new Serializable[]{ value };
#**#                    factory.pushToCache(${Target}.class, pks, target);
#**#                } else {
#**#                    factory.removeFromCache(${Target}.class, pks);
#**#                    LogPadrao.e(String.format("%s nao editavel salvo", target.getClass().getName()));
#**#                }
#**#                return true;
#**#            } else {
#**#                return false;
#**#            }
#end
        } else {
            int value = db.update(
                "${table}", contentValues, queryByPrimaryKey, primaryKeysArgs);
            if (value > 0) {
                factory.pushToCache(${Target}.class, pks, target);
                return true;
            } else {
                return false;
            }
        }
    }
