#foreach ($association in $manyToManyAssociations)
#**#    public boolean add${association.Klass}To${Target}(${association.Klass} obj, $Target target) throws IOException {
#**#        long value;
#**##if (${association.IsThisTableA})
#******#        if (target.${getter[$association.ReferenceA]}() == ${defaultId}) {
#******#            return false;
#******#        }
#******#        PreparedStatement stm = getStatement(
#******#            "INSERT INTO ${association.JoinTable} (" +
#******#                "${association.KeyToA.LowerAndUnderscores}," +
#******#                "${association.KeyToB.LowerAndUnderscores}" +
#******#            ") VALUES (?,?)");
#******#        try {
#******#            stm.set${association.KeyToA.Klass}(1, target.${getter[$association.ReferenceA]}());
#******#            stm.set${association.KeyToB.Klass}(2, obj.${getter[$association.ReferenceB]}());
#**##else
#******#        if (target.${getter[$association.ReferenceB]}() == ${defaultId}) {
#******#            return false;
#******#        }
#******#        PreparedStatement stm = getStatement(
#******#            "INSERT INTO ${association.JoinTable} (" +
#******#                "${association.KeyToA.LowerAndUnderscores}," +
#******#                "${association.KeyToB.LowerAndUnderscores}" +
#******#            ") VALUES (?,?)");
#******#        try {
#******#            stm.set${association.KeyToA.Klass}(1, obj.${getter[$association.ReferenceA]}());
#******#            stm.set${association.KeyToB.Klass}(2, target.${getter[$association.ReferenceB]}());
#**##end
#**#            value = stm.executeUpdate();
#**#        } catch (java.sql.SQLException e){
#**#            throw new IOException(StringUtil.getStackTrace(e));
#**#        }
#**#        return (value > 0);
#**#    }
#**#
#**#
#**#    public boolean remove${association.Klass}From${Target}(${association.Klass} obj, $Target target) throws IOException {
#**##if (${association.IsThisTableA})
#******#        if (target.${getter[$association.ReferenceA]}() == ${defaultId}) {
#******#            return false;
#******#        }
#******#        PreparedStatement stm = getStatement(
#******#            "DELETE FROM ${association.JoinTable} WHERE " +
#******#            "${association.KeyToA.LowerAndUnderscores} = ? AND " +
#******#            "${association.KeyToB.LowerAndUnderscores} = ?" +
#******#            "LIMIT 1");
#******#        try {
#******#            stm.set${association.KeyToA.Klass}(1, target.${getter[$association.ReferenceA]}());
#******#            stm.set${association.KeyToB.Klass}(2, obj.${getter[$association.ReferenceB]}());
#**##else
#******#        if (target.${getter[$association.ReferenceB]}() == ${defaultId}) {
#******#            return false;
#******#        }
#******#        PreparedStatement stm = getStatement(
#******#            "DELETE FROM ${association.JoinTable} WHERE " +
#******#            "${association.KeyToA.LowerAndUnderscores} = ? AND " +
#******#            "${association.KeyToB.LowerAndUnderscores} = ?" +
#******#            "LIMIT 1");
#******#        try {
#******#            stm.set${association.KeyToA.Klass}(1, obj.${getter[$association.ReferenceA]}());
#******#            stm.set${association.KeyToB.Klass}(2, target.${getter[$association.ReferenceB]}());
#**##end
#**#            long affected = stm.executeUpdate();
#**#            return (affected == 1);
#**#        } catch (java.sql.SQLException e) {
#**#            throw new IOException(StringUtil.getStackTrace(e));
#**#        }
#**#    }
#end
