    public final static Table _TABLE = Table.create ("${table}")
#foreach ($field in $fields)
#**#            .addColumn(${field.Klass}.class, "${field.LowerAndUnderscores}",${constraints[$field.Constraints]})
#end
            .get ();
#foreach ($field in $fields)
#**#    public final static Table.Column<${field.Klass}> ${field.UpperAndUnderscores} = _TABLE.findColumn(${field.Klass}.class, "${field.LowerAndUnderscores}");
#end
##
#foreach ($association in $manyToManyAssociations)
#**##if ($association.IsThisTableA)
#******#    public final static Table _${association.JoinTableUpper} = Table.create ("${association.JoinTable}")
#******#            .addColumn(${association.KeyToB.Klass}.class, "${association.KeyToB.LowerAndUnderscores}")
#******#            .addColumn(${association.KeyToA.Klass}.class, "${association.KeyToA.LowerAndUnderscores}")
#******#            .get ();
#******#    public final static Table.Column<${association.KeyToB.Klass}> _${association.JoinTableUpper}_${association.KeyToB.UpperAndUnderscores} =
#******#        _${association.JoinTableUpper}.findColumn(${association.KeyToB.Klass}.class, "${association.KeyToB.LowerAndUnderscores}");
#******#    public final static Table.Column<${association.KeyToA.Klass}> _${association.JoinTableUpper}_${association.KeyToA.UpperAndUnderscores} =
#******#        _${association.JoinTableUpper}.findColumn(${association.KeyToA.Klass}.class, "${association.KeyToA.LowerAndUnderscores}");
#**##else
#******#    public final static Table _${association.JoinTableUpper} = ${association.Klass}._${association.JoinTableUpper};
#******#    public final static Table.Column<${association.KeyToB.Klass}> _${association.JoinTableUpper}_${association.KeyToB.UpperAndUnderscores} =
#******#        ${association.Klass}._${association.JoinTableUpper}_${association.KeyToB.UpperAndUnderscores};
#******#    public final static Table.Column<${association.KeyToA.Klass}> _${association.JoinTableUpper}_${association.KeyToA.UpperAndUnderscores} =
#******#        ${association.Klass}._${association.JoinTableUpper}_${association.KeyToA.UpperAndUnderscores};
#**##end
#end
