    public boolean add${association.Klass}To${Target}(${association.Klass} obj, $Target target) throws IOException {
        ContentValues contentValues = new ContentValues();
#if (${association.IsThisTableA})
        if (target.${getter[$association.ReferenceA]}() == ${defaultId}) {
            return false;
        }
        contentValues.put("${association.KeyToA.LowerAndUnderscores}", target.${getter[$association.ReferenceA]}());
        contentValues.put("${association.KeyToB.LowerAndUnderscores}", obj.${getter[$association.ReferenceB]}());
#else
        if (target.${getter[$association.ReferenceB]}() == ${defaultId}) {
            return false;
        }
        contentValues.put("${association.KeyToB.LowerAndUnderscores}", target.${getter[$association.ReferenceB]}());
        contentValues.put("${association.KeyToA.LowerAndUnderscores}", obj.${getter[$association.ReferenceA]}());
#end
        SQLiteDatabase db = this.factory.getDb();
        long value;
        try{
            value = db.insertOrThrow("${association.JoinTable}", null, contentValues);
        } catch (SQLException e){
            throw new IOException(StringUtil.getStackTrace(e));
        }
        return (value > 0);
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

