    @Override
    public boolean delete(${Target} target) throws IOException {
        if (${nullPkCondition}) {
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
#**#                "${relation.Table}", contentValues,
#**#                "${relation.ForeignKey.LowerAndUnderscores} = ?",
#**#                new String[] {((${relation.ForeignKey.Klass}) target.${getter[$relation.ReferenceKey]}()).toString()});
#**#           Runnable _${relation.Klass}NullFkThread =
#**#               new ${relation.Klass}NullFkThread(target);
#**#           //_${relation.Klass}NullFkThread.start();
#**#
#**##else
#**#            DAO<${relation.Klass}> daoFor${relation.Klass} = (DAO<${relation.Klass}>)factory.getDaoFor(${relation.Klass}.class);
#**#            for (${relation.Klass} obj: target.get${relation.Pluralized}().all()) {
#**#                daoFor${relation.Klass}.delete(obj);
#**#            }
#**##end
#end
##
#foreach ($relation in $manyToManyRelation)
            db.delete("${relation.ThroughTable}", "${relation.ThroughReferenceKey.LowerAndUnderscores} = ?",
                      new String[] {((${relation.ReferenceKey.Klass}) target.${getter[$relation.ReferenceKey]}()).toString()});
#end
            int affected;
            try {
                affected = db.delete(
                    "${table}",
                    "${queryByPrimaryKey}",
                    ${primaryKeysArgs});
            } catch (SQLException e) {
                throw new IOException(StringUtil.getStackTrace(e));
            }
            if (affected == 0) {
                return false;
            }
            db.setTransactionSuccessful();
#foreach ($relation in $oneToManyAssociations)
#**##if ($relation.Nullable)
#**#            if (_${relation.Klass}NullFkThread != null) {
#**#                try {
#**#                    // _${relation.Klass}NullFkThread.join();
#**#                    _${relation.Klass}NullFkThread.run();
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
#**#    private class ${association.Klass}NullFkThread implements Runnable {
#**#
#**#        ${Target} target;
#**#
#**#        private ${association.Klass}NullFkThread(${Target} target) {
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
#**#                if (target.equals(obj.get${Target}()) )
#**#                    obj.set${Target}(null);
#**#            }
#**#        }
#**#
#**#    }
#**##end
#end
