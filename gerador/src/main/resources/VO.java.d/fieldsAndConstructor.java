    public final static long publicSerialVersionUID = ${serialVersionUID};
    private final static long serialVersionUID = publicSerialVersionUID;

#foreach ($field in $fields)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#    ${association.Klass} _${association.KeyToA};
#**##else
#******#    ${field.Type} ${field.LowerCamel};
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

    public ${Filename}(${constructorArgsDecl})
    {
#foreach ($field in $fields)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#        this._${association.KeyToA} = _${association.KeyToA};
#**##else
#******#        this.${field.LowerCamel} = _${field.LowerCamel};
#**##end
#end
#foreach ($association in $oneToManyAssociations)
#**#        this._${association.KeyToAPluralized} = _${association.KeyToAPluralized};
#end
#foreach ($association in $manyToManyAssociations)
#**#        this._${association.Pluralized} = _${association.Pluralized};
#end
    }

