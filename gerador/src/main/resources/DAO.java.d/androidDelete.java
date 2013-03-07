    @Override
    public boolean delete(${Target} target) throws IOException {
        if (target.${getter[$primaryKey]}() == ${defaultId}) {
            return false;
        }
        SQLiteDatabase db = this.factory.getDb();
        try {
            db.beginTransaction();
#if ($hasNullableAssociation)
            ContentValues contentValues;
#end
##
#foreach ($relation in $oneToManyAssociations)
#**##if ($relation.Nullable)
#**#            contentValues = new ContentValues();
#**#            contentValues.putNull("${relation.ForeignKey.LowerAndUnderscores}");
#**#            db.update(
#**#                "${relation.Table.Name}", contentValues,
#**#                "${relation.ForeignKey.LowerAndUnderscores} = ?",
#**#                new String[] {((${relation.ForeignKey.Klass}) target.${getter[$relation.ReferenceKey]}()).toString()});
#**#           Runnable _${relation.KeyToAPluralized}NullFkThread =
#**#               new ${relation.KeyToAPluralized}NullFkThread(target);
#**#           //_${relation.Klass}NullFkThread.start();
#**#
#**##else
#**#            DAO<${relation.Klass}> daoFor${relation.Klass} = (DAO<${relation.Klass}>)factory.getDaoFor(${relation.Klass}.class);
#**#            for (${relation.Klass} obj: target.get${relation.KeyToAPluralized}().all()) {
#**#                daoFor${relation.Klass}.delete(obj);
#**#            }
#**##end
#end
##
#foreach ($relation in $manyToManyAssociations)
#**#            {
#**##if ($association.isThisTableA)
#**#                final String sqlWhereClause = "${relation.KeyToA.LowerAndUnderscores} = ?";
#**##else
#**#                final String sqlWhereClause = "${relation.KeyToB.LowerAndUnderscores} = ?";
#**##end
#**##if ($association.isThisTableA)
#**#                String manyToManyArgs[] = new String[] {((${relation.ReferenceA.Klass}) target.${getter[$relation.ReferenceA]}()).toString()};
#**##else
#**#                String manyToManyArgs[] = new String[] {((${relation.ReferenceB.Klass}) target.${getter[$relation.ReferenceB]}()).toString()};
#**##end
#**#                db.delete("${relation.JoinTable}", sqlWhereClause,manyToManyArgs);
#**#            }
#end
            int affected;
            try {
                affected = db.delete(
                    "${table}",
                    "${primaryKey.LowerAndUnderscores}=?",
                    new String[]{ ((Long)target.${getter[$primaryKey]}()).toString() });
            } catch (SQLException e) {
                throw new IOException(StringUtil.getStackTrace(e));
            }
            if (affected == 0) {
                return false;
            }
            db.setTransactionSuccessful();
#foreach ($relation in $oneToManyAssociations)
#**##if ($relation.Nullable)
#**#            if (_${relation.KeyToAPluralized}NullFkThread != null) {
#**#                try {
#**#                    // _${relation.Klass}NullFkThread.join();
#**#                    _${relation.KeyToAPluralized}NullFkThread.run();
#**#                } catch (Exception e) {
#**#                    LogPadrao.e(e);
#**#                }
#**#            }
#**##end
#end
        } finally {
            db.endTransaction();
        }
        Serializable pks [] = new Serializable[]{
            target.${getter[$primaryKey]}()
        };
        factory.removeFromCache(${Target}.class, pks);
        return true;
    }
#foreach ($association in $oneToManyAssociations)
#**##if ($association.Nullable)
#**#
#**#    private class ${association.KeyToAPluralized}NullFkThread implements Runnable {
#**#
#**#        ${Target} target;
#**#
#**#        private ${association.KeyToAPluralized}NullFkThread(${Target} target) {
#**#            this.target = target;
#**#        }
#**#
#**#        @Override
#**#        public void run() {
#**#            Collection<Reference<${association.Klass}>> references = ${Klass}.this.factory.lookupForClass(${association.Klass}.class);
#**#            for (Reference<${association.Klass}> reference : references) {
#**#                ${association.Klass} obj = (${association.Klass})reference.get();
#**#                if (obj == null)
#**#                    continue;
#**#                if (target.equals(obj.get${association.KeyToA}()) )
#**#                    obj.set${association.KeyToA}(null);
#**#            }
#**#        }
#**#
#**#    }
#**##end
#end
