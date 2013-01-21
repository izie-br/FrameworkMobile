    private final static long serialVersionUID = ${serialVersionUID};

#foreach ($field in $fields)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#    ${association.Klass} _${association.KeyToA};
#**##else
#******#    ${field.Type} ${field.LowerCamel}#if ($field.PrimaryKey) = ${defaultId}#end;
#**##end
#end
##
#foreach ($association in $toManyAssociations)
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
#foreach ($association in $toManyAssociations)
#**#        this._${association.Pluralized} = _${association.Pluralized};
#end
    }

