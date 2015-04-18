    public static final long publicSerialVersionUID = ${serialVersionUID};
    private static final long serialVersionUID = publicSerialVersionUID;

#foreach ($field in $fields)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#    private ${association.Klass} ${association.KeyToA}AG;
#**##else
#******#    private ${field.Type} ${field.LowerCamel};
#**##end
#end
##
#foreach ($association in $oneToManyAssociations)
#**#    QuerySet<${association.Klass}> ${association.KeyToAPluralized}AG;
#end
#foreach ($association in $manyToManyAssociations)
#**#    QuerySet<${association.Klass}> ${association.Pluralized}AG;
#end

    public ${Filename}(){}
