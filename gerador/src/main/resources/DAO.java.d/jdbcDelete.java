    @Override
    public boolean delete(${Target} target) throws IOException {
        if (target.${getter[$primaryKey]}() == ${defaultId}) {
            return false;
        }
        try {
            PreparedStatement stm;
#foreach ($association in $oneToManyAssociations)
#**##if ($association.Nullable)
#**#            stm = getStatement(
#**#                    "UPDATE ${association.Table.Name} SET " +
#**#                    "${association.ForeignKey.LowerAndUnderscores}=NULL " +
#**#                    "WHERE ${association.ForeignKey.LowerAndUnderscores}=?");
#**#            try {
#**#                stm.set${association.ReferenceKey.Klass}(1, target.${getter[$association.ReferenceKey]}());
#**#                stm.executeUpdate();
#**#            } catch (java.sql.SQLException e) {
#**#                throw new RuntimeException(StringUtil.getStackTrace(e));
#**#            }
#**#            Runnable ${association.KeyToAPluralized}AGNullFkThread =
#**#                    new ${association.KeyToAPluralized}NullFkThread(target);
#**#            //${association.Klass}_NullFkThread.start();
#**##else
#**#            DAO<${association.Klass}> daoFor${association.Klass} = (DAO<${association.Klass}>) factory.getDaoFor(${association.Klass}.class);
#**#            for (${association.Klass} obj: target.get${association.KeyToAPluralized}().all()) {
#**#                daoFor${association.Klass}.delete(obj);
#**#            }
#**##end
#end
##
#foreach ($association in $manyToManyAssociations)
#**#            {
#**##if($association.isThisTableA)
#**#                final String manyToManySqlDelete = "DELETE FROM ${association.JoinTable} WHERE ${association.KeyToA.LowerAndUnderscores} = ?";
#**##else
#**#                final String manyToManySqlDelete = "DELETE FROM ${association.JoinTable} WHERE ${association.KeyToB.LowerAndUnderscores} = ?";
#**##end
#**#                stm = getStatement(manyToManySqlDelete);
#**#                try {
#**##if($association.isThisTableA)
#**#                    stm.set${association.ReferenceA.Klass}(1, target.${getter[$association.ReferenceA]}());
#**##else
#**#                    stm.set${association.ReferenceB.Klass}(1, target.${getter[$association.ReferenceB]}());
#**##end
#**#                    stm.executeUpdate();
#**#                } catch (java.sql.SQLException e) {
#**#                    throw new IOException(StringUtil.getStackTrace(e));
#**#                }
#**#            }
#end
            stm = getStatement("DELETE FROM ${table} WHERE ${primaryKey.LowerAndUnderscores}=?");
            int affected;
            try {
                stm.setString(1, target.${getter[$primaryKey]}());
                affected = stm.executeUpdate();
            } catch (java.sql.SQLException e) {
                throw new IOException(StringUtil.getStackTrace(e));
            }
            if (affected == 0) {
                return false;
            }
#foreach ($relation in $oneToManyAssociations)
#**##if ($relation.Nullable)
#**#            if (${relation.KeyToAPluralized}AGNullFkThread != null) {
#**#                try {
#**#                    //${relation.Klass}_NullFkThread.join();
#**#                    ${relation.KeyToAPluralized}AGNullFkThread.run();
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
            target.${getter[$primaryKey]}(),
        };
        factory.removeFromCache(${Target}.class, pks);
        return true;
    }
##
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
#**#            Collection<Reference<${association.Klass}>> references =
#**#                    ${Klass}.this.factory.lookupForClass(${association.Klass}.class);
#**#            for (Reference<${association.Klass}> reference : references) {
#**#                ${association.Klass} obj = (${association.Klass})reference.get();
#**#                if (obj == null)
#**#                    continue;
#**#                if (target.equals(obj.get${association.KeyToA}()) )
#**#                    obj.set${association.KeyToA}(null);
#**#            }
#**#        }
#**#    }
#**##end
#end
