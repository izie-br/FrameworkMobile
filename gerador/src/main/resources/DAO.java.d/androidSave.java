    @Override
    public boolean save($Target target) throws IOException {
        return save(target, Save.INSERT_IF_NULL_PK);
    }

    @Override
    public boolean save($Target target, int flags) throws IOException {
        ContentValues contentValues = new ContentValues();
#foreach ($field in $fields)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#        contentValues.put("${field.LowerAndUnderscores}",
#******#                          (target.get${association.KeyToA}() == null) ? 0 : target.get${association.KeyToA}().get${association.ReferenceKey.UpperCamel}());
#**##elseif (!$field.PrimaryKey)
#******##if ($field.Klass.equals("Date") )
#******#        contentValues.put("${field.LowerAndUnderscores}",
#******#                          DateUtil.timestampToString(target.${getter[$field]}()));
#******##elseif ($field.Klass.equals("Boolean") )
#******#        contentValues.put("${field.LowerAndUnderscores}", SQLiteUtils.booleanToInteger(target.${getter[$field]}()));
#******##else
#******#        contentValues.put("${field.LowerAndUnderscores}", target.${getter[$field]}());
#******##end
#**##end
#end
        SQLiteDatabase db = this.factory.getDb();
        boolean insert;
        String primaryKeysArgs [] = new String[]{
            ((Long)target.${getter[$primaryKey]}()).toString()
        };
        boolean insertIfNotExists = ( (flags&Save.INSERT_IF_NOT_EXISTS) != 0);
        insert = target.${getter[$primaryKey]}() == ${defaultId};
        if (insertIfNotExists)
        {
            Cursor cursor = this.factory.getDb().rawQuery(
                "SELECT COUNT(*) FROM ${table} WHERE ${primaryKey.LowerAndUnderscores}=?",
                primaryKeysArgs);
            insert = cursor.moveToNext() && cursor.getLong(0) == 0L;
            cursor.close();
        }
        Serializable pks [] = new Serializable[]{
            target.${getter[$primaryKey]}()
        };
        if (insert) {
            if (insertIfNotExists) {
                contentValues.put("${primaryKey.LowerAndUnderscores}", target.${getter[$primaryKey]}());
            }
            long value;
            try{
                value = db.insertOrThrow("${table}", null, contentValues);
            } catch (SQLException e){
                throw new IOException(StringUtil.getStackTrace(e));
            }
            if (value > 0){
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
                    boolean itemFoundInCache = updateCache(target);
                    if (!itemFoundInCache) {
                        pks = new Serializable[]{ value };
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
            int value = db.update(
                "${table}", contentValues,
                "${primaryKey.LowerAndUnderscores}=?",
                primaryKeysArgs
            );
            if (value > 0) {
                boolean itemFoundInCache = updateCache(target);
                if (!itemFoundInCache) {
                    factory.pushToCache(${Target}.class, pks, target);
                }
                return true;
            } else {
                return false;
            }
        }
    }
