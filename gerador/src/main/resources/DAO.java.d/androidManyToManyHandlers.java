    public boolean add${association.Klass}To${Target}(${association.Klass} obj, $Target target) throws IOException {
#**##if (${association.IsThisTableA})
#******#            ${association.KeyToA.Klass} idA = target.${getter[$association.ReferenceA]}();
#******#            ${association.KeyToB.Klass} idB = obj.${getter[$association.ReferenceB]}();
#******#            if (idA == ${defaultId}) {
#******#                return false;
#******#            }
#******#            if (idB == ${defaultId}) {
#******#                if(this.factory.getDaoFor(${association.Klass}.class).save(obj)) {
#******#                    idB = obj.${getter[$association.ReferenceB]}();
#******#                } else {
#******#                    return false;
#******#                }
#******#            }
#**##else
#******#            ${association.KeyToA.Klass} idA = obj.${getter[$association.ReferenceA]}();
#******#            ${association.KeyToB.Klass} idB = target.${getter[$association.ReferenceB]}();
#******#            if (idB == ${defaultId}) {
#******#                return false;
#******#            }
#******#            if (idA == ${defaultId}) {
#******#                if(this.factory.getDaoFor(${association.Klass}.class).save(obj)) {
#******#                    idA = obj.${getter[$association.ReferenceB]}();
#******#                } else {
#******#                    return false;
#******#                }
#******#            }
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
                throw new IOException();
            } else if (cursor.getLong(0) != ${defaultId}){
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
#if (${association.IsThisTableA})
        if (target.${getter[$association.ReferenceA]}() == ${defaultId}) {
            return false;
        }
        String whereSql = "${association.KeyToA.LowerAndUnderscores} = ? AND ${association.KeyToB.LowerAndUnderscores} = ?";
        String [] args = new String[]{
            ((${association.KeyToA.Klass})target.${getter[$association.ReferenceA]}()).toString(),
            ((${association.KeyToB.Klass})obj.${getter[$association.ReferenceB]}()).toString()
       };
#else
        if (target.${getter[$association.ReferenceB]}() == ${defaultId}) {
            return false;
        }
        String whereSql = "${association.KeyToB.LowerAndUnderscores} = ? AND ${association.KeyToA.LowerAndUnderscores} = ?";
        String [] args = new String[]{
            ((${association.KeyToA.Klass})obj.${getter[$association.ReferenceA]}()).toString(),
            ((${association.KeyToB.Klass})target.${getter[$association.ReferenceB]}()).toString()
       };
#end
        SQLiteDatabase db = this.factory.getDb();
        Cursor cursor = db.query(
            "${association.JoinTable}", (new String[]{"rowid"}),
            whereSql, args, null, null, null, "1");
        if (!cursor.moveToNext()) {
            return false;
        }
        long rowid = cursor.getLong(0);
        cursor.close();
        if (rowid<= 0) {
            return false;
        }
        long affected = db.delete("${association.JoinTable}", "rowid=?", new String[] {((Long) rowid).toString()});
        return (affected == 1);
    }

