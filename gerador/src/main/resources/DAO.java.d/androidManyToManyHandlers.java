    public boolean add${association.Klass}To${Target}(${association.Klass} obj, $Target target) throws IOException {
#**##if (${association.IsThisTableA})
#******#        ${association.KeyToA.Klass} idA = target.${getter[$association.ReferenceA]}();
#******#        ${association.KeyToB.Klass} idB = obj.${getter[$association.ReferenceB]}();
#******#        if (idA == ${defaultId}) {
#******#            return false;
#******#        }
#******#        if (idB == ${defaultId}) {
#******#            if(this.factory.getDaoFor(${association.Klass}.class).save(obj)) {
#******#                idB = obj.${getter[$association.ReferenceB]}();
#******#            } else {
#******#                return false;
#******#            }
#******#        }
#**##else
#******#        ${association.KeyToA.Klass} idA = obj.${getter[$association.ReferenceA]}();
#******#        ${association.KeyToB.Klass} idB = target.${getter[$association.ReferenceB]}();
#******#        if (idB == ${defaultId}) {
#******#            return false;
#******#        }
#******#        if (idA == ${defaultId}) {
#******#            if(this.factory.getDaoFor(${association.Klass}.class).save(obj)) {
#******#                idA = obj.${getter[$association.ReferenceB]}();
#******#            } else {
#******#                return false;
#******#            }
#******#        }
#**##end
        SQLiteDatabase db = this.factory.getDb();
        try{
            Cursor cursor = db.rawQuery(
                "SELECT count(*) FROM ${association.JoinTable} WHERE " +
                    "${association.KeyToA.LowerAndUnderscores}=? AND " +
                    "${association.KeyToB.LowerAndUnderscores}=?",
                    new String[] {
                        ((${association.KeyToA.Klass})idA).toString(),
                        ((${association.KeyToB.Klass})idB).toString(),
                    });
            if (!cursor.moveToNext()) {
                throw new IOException("Erro ao mover o cursor ao adicionar many-to-many");
            } else if (cursor.getLong(0) != 0L){
                return false;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put("${association.KeyToA.LowerAndUnderscores}", idA);
            contentValues.put("${association.KeyToB.LowerAndUnderscores}", idB);
            long value = db.insertOrThrow("${association.JoinTable}", null, contentValues);
            return (value > 0);
        } catch (SQLException e){
            throw new IOException(StringUtil.getStackTrace(e));
        }
    }

    public boolean remove${association.Klass}From${Target}(${association.Klass} obj, $Target target) throws IOException {
#**##if (${association.IsThisTableA})
#******#        ${association.KeyToA.Klass} idA = target.${getter[$association.ReferenceA]}();
#******#        ${association.KeyToB.Klass} idB = obj.${getter[$association.ReferenceB]}();
#**##else
#******#        ${association.KeyToA.Klass} idA = obj.${getter[$association.ReferenceA]}();
#******#        ${association.KeyToB.Klass} idB = target.${getter[$association.ReferenceB]}();
#**##end
        if (idA == ${defaultId} || idB == ${defaultId}) {
            return false;
        }
        String whereSql = "${association.KeyToA.LowerAndUnderscores} = ? AND ${association.KeyToB.LowerAndUnderscores} = ?";
        String [] args = new String[]{
            ((${association.KeyToA.Klass})idA).toString(),
            ((${association.KeyToB.Klass})idB).toString(),
        };
        SQLiteDatabase db = this.factory.getDb();
        long affected = db.delete("${association.JoinTable}", whereSql, args);
        return (affected == 1);
    }

