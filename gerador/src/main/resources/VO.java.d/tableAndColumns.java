    public final static Table _TABLE = new Table ("${table}");
#foreach ($field in $fields)
#**##set ($constraintsVarargs = ${Constraints[$field.Constraints]})
#**##if (!$constraintsVarargs.equals(""))
#******##set ($constraintsVarargs = ", new Constraint[]{" + $constraintsVarargs + "}")
#**##end
#**#    public final static Table.Column<${field.Klass}> ${field.UpperAndUnderscores} = _TABLE.addColumn(${field.Klass}.class, "${field.LowerAndUnderscores}"${constraintsVarargs});
#end
##
#foreach ($association in $manyToManyAssociations)
#**##if ($association.IsThisTableA)
#******#    public final static Table _${association.JoinTableUpper} = new Table ("${association.JoinTable}");
#******#    public final static Table.Column<${association.KeyToB.Klass}> _${association.JoinTableUpper}_${association.KeyToB.UpperAndUnderscores} =
#******#        _${association.JoinTableUpper}.addColumn(${association.KeyToB.Klass}.class, "${association.KeyToB.LowerAndUnderscores}");
#******#    public final static Table.Column<${association.KeyToA.Klass}> _${association.JoinTableUpper}_${association.KeyToA.UpperAndUnderscores} =
#******#        _${association.JoinTableUpper}.addColumn(${association.KeyToA.Klass}.class, "${association.KeyToA.LowerAndUnderscores}");
#**##else
#******#    public final static Table _${association.JoinTableUpper} = ${association.Klass}._${association.JoinTableUpper};
#******#    public final static Table.Column<${association.KeyToB.Klass}> _${association.JoinTableUpper}_${association.KeyToB.UpperAndUnderscores} =
#******#        ${association.Klass}._${association.JoinTableUpper}_${association.KeyToB.UpperAndUnderscores};
#******#    public final static Table.Column<${association.KeyToA.Klass}> _${association.JoinTableUpper}_${association.KeyToA.UpperAndUnderscores} =
#******#        ${association.Klass}._${association.JoinTableUpper}_${association.KeyToA.UpperAndUnderscores};
#**##end
#end
