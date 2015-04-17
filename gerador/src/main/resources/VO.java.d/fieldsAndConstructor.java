    public static final long publicSerialVersionUID = ${serialVersionUID};
    private static final long serialVersionUID = publicSerialVersionUID;

#foreach ($field in $fields)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#    private ${association.Klass} ${association.KeyToA}_;
#**##else
#******#    private ${field.Type} ${field.LowerCamel};
#**##end
#end
##
#foreach ($association in $oneToManyAssociations)
#**#    QuerySet<${association.Klass}> ${association.KeyToAPluralized}_;
#end
#foreach ($association in $manyToManyAssociations)
#**#    QuerySet<${association.Klass}> ${association.Pluralized}_;
#end

    public ${Filename}(){}
