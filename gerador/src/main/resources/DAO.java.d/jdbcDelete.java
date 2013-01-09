    @Override
    public boolean delete(${Target} target) throws IOException {
        if (${nullPkCondition}) {
            return false;
        }
        try {
            PreparedStatement stm;
#foreach ($association in $oneToManyAssociations)
#**##if ($association.Nullable)
#**#            stm = getStatement(
#**#                    "UPDATE ${association.Table} SET " +
#**#                    "${association.ForeignKey.LowerAndUnderscores}=NULL " +
#**#                    "WHERE ${association.ForeignKey.LowerAndUnderscores}=?");
#**#            try {
#**#                stm.set${association.ReferenceKey.Klass}(1, target.${getter[$association.ReferenceKey]}());
#**#                stm.executeUpdate();
#**#            } catch (java.sql.SQLException e) {
#**#                throw new RuntimeException(StringUtil.getStackTrace(e));
#**#            }
#**#            Runnable _${association.Klass}NullFkThread =
#**#                    new ${association.Klass}NullFkThread(target);
#**#            //_${association.Klass}NullFkThread.start();
#**##else
#**#            DAO<${association.Klass}> daoFor${association.Klass} = (DAO<${association.Klass}>)factory.getDaoFor(${association.Klass}.class);
#**#            for (${association.Klass} obj: target.get${association.Pluralized}().all()) {
#**#                daoFor${association.Klass}.delete(obj);
#**#            }
#**##end
#end
##
#foreach ($association in $manyToManyRelation)
            stm = getStatement("DELETE FROM ${association.ThroughTable} WHERE ${association.ThroughReferenceKey.LowerAndUnderscores} = ?");
            try {
                stm.set${association.ReferenceKey.Klass}(1, target.${getter[$association.ReferenceKey.UpperCamel]}());
                stm.executeUpdate();
            } catch (java.sql.SQLException e) {
                throw new IOException(StringUtil.getStackTrace(e));
            }
#end
            stm = getStatement("DELETE FROM ${table} WHERE ${queryByPrimaryKey}");
            int affected;
            try {
#foreach ($field in $primaryKeys)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#                stm.set${field.Klass}(
#******#                        ${foreach.count},
#******#                        (target.get${association.Klass}() == null) ?
#******#                            0 :
#******#                            target.get${association.Klass}().get${association.ReferenceKey.UpperCamel}());
#**##else
#******##if ($field.Klass.equals("Date") )
#******#                stm.setTimestamp(
#******#                        ${foreach.count},
#******#                        (target.${getter[$field]}() == null) ?
#******#                            null :
#******#                            new java.sql.Timestamp(target.${getter[$field]}().getTime()));
#******##else
#******#                 stm.set${field.Klass}(${foreach.count}, target.${getter[$field]}());
#******##end
#**##end
#end
                affected = stm.executeUpdate();
            } catch (java.sql.SQLException e) {
                throw new IOException(StringUtil.getStackTrace(e));
            }
            if (affected == 0) {
                return false;
            }
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
            //db.endTransaction();
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
        factory.removeFromCache(${Target}.class, pks);
        return true;
    }
##
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
#**#            Collection<Reference<${association.Klass}>> references =
#**#                    ${Klass}.this.factory.lookupForClass(${association.Klass}.class);
#**#            for (Reference<${association.Klass}> reference : references) {
#**#                ${association.Klass} obj = (${association.Klass})reference.get();
#**#                if (obj == null)
#**#                    continue;
#**#                if (target.equals(obj.get${Target}()) )
#**#                    obj.set${Target}(null);
#**#            }
#**#        }
#**#    }
#**##end
#end
