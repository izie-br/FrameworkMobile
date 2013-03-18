#foreach ($association in $manyToManyAssociations)
#**#    public boolean add${association.Klass}To${Target}(${association.Klass} obj, $Target target) throws IOException {
#**#        long value;
#**#        try {
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
#**#            PreparedStatement stm = getStatement(
#**#                "SELECT count(*) FROM ${association.JoinTable} WHERE " +
#**#                    "${association.KeyToA.LowerAndUnderscores}=? AND " +
#**#                    "${association.KeyToB.LowerAndUnderscores}=?");
#**#            stm.set${association.KeyToA.Klass}(1, idA);
#**#            stm.set${association.KeyToB.Klass}(2, idB);
#**#            ResultSet rs = stm.executeQuery();
#**#            // conferir se ja existe outros registros
#**#            if (!rs.next()) {
#**#                throw new IOException("Erro ao mover o cursor ao adicionar many-to-many");
#**#            } else if (rs.getLong(1) != 0) {
#**#                return false;
#**#            }
#**#            stm = getStatement(
#**#                "INSERT INTO ${association.JoinTable} (" +
#**#                    "${association.KeyToA.LowerAndUnderscores}," +
#**#                    "${association.KeyToB.LowerAndUnderscores}" +
#**#                ") VALUES (?,?)");
#**#            stm.set${association.KeyToA.Klass}(1, idA);
#**#            stm.set${association.KeyToB.Klass}(2, idB);
#**#            value = stm.executeUpdate();
#**#        } catch (java.sql.SQLException e){
#**#            throw new IOException(StringUtil.getStackTrace(e));
#**#        }
#**#        return (value > 0);
#**#    }
#**#
#**#
#**#    public boolean remove${association.Klass}From${Target}(${association.Klass} obj, $Target target) throws IOException {
#**#        try {
#**##if (${association.IsThisTableA})
#******#            ${association.KeyToA.Klass} idA = target.${getter[$association.ReferenceA]}();
#******#            ${association.KeyToB.Klass} idB = obj.${getter[$association.ReferenceB]}();
#******#            if (idA == ${defaultId} || idB == ${defaultId}) {
#******#                return false;
#******#            }
#**##else
#******#            ${association.KeyToA.Klass} idA = obj.${getter[$association.ReferenceA]}();
#******#            ${association.KeyToB.Klass} idB = target.${getter[$association.ReferenceB]}();
#******#            if (idA == ${defaultId} || idB == ${defaultId}) {
#******#                return false;
#******#            }
#**##end
#**#            PreparedStatement stm = getStatement(
#**#                "DELETE FROM ${association.JoinTable} WHERE " +
#**#                "${association.KeyToA.LowerAndUnderscores} = ? AND " +
#**#                "${association.KeyToB.LowerAndUnderscores} = ?" +
#**#                "LIMIT 1");
#**#            stm.set${association.KeyToA.Klass}(1, idA);
#**#            stm.set${association.KeyToB.Klass}(2, idB);
#**#            long affected = stm.executeUpdate();
#**#            return (affected == 1);
#**#        } catch (java.sql.SQLException e) {
#**#            throw new IOException(StringUtil.getStackTrace(e));
#**#        }
#**#    }
#end
