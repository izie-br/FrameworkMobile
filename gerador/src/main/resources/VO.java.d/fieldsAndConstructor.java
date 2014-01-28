    public final static long publicSerialVersionUID = ${serialVersionUID};
    private final static long serialVersionUID = publicSerialVersionUID;

#foreach ($field in $fields)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#    private ${association.Klass} _${association.KeyToA};
#**##else
#******#    private ${field.Type} ${field.LowerCamel};
#**##end
#end
##
#foreach ($association in $oneToManyAssociations)
#**#    QuerySet<${association.Klass}> _${association.KeyToAPluralized};
#end
#foreach ($association in $manyToManyAssociations)
#**#    QuerySet<${association.Klass}> _${association.Pluralized};
#end

    public ${Filename}(){}
