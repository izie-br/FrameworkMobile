    @Override
    public void updateWithMap(${Target} target, Map<String,Object> map) {
        ValueParser parser = this.factory.getValueParser();
        CamelCaseUtils.AnyCamelMap<Object> mapAnyCamelCase =
            new CamelCaseUtils.AnyCamelMap<Object>();
        mapAnyCamelCase.putAll(map);
        Object temp;

#foreach ($field in $fields)
#**##if ($field.SerializationAlias)
#******##set ($alias = $field.SerializationAlias)
#**##else
#******##set ($alias = $field.LowerCamel)
#**##end
#**###
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#        if (mapAnyCamelCase.containsKey("${alias}") ||
#******#            mapAnyCamelCase.containsKey("${association.KeyToA}"))
#******#        {
#******#            DAO<${association.Klass}> _${association.KeyToA}dao = this.factory.getDaoFor(${association.Klass}.class);
#******#            ${association.Klass} _${association.KeyToA} = parser.extractAssociation(
#******#                    mapAnyCamelCase, _${association.KeyToA}dao, "${association.KeyToA}", "${alias}");
#******#            target.set${association.KeyToA}(_${association.KeyToA});
#******#        }
#**##elseif (!$field.PrimaryKey)
#******#        if (mapAnyCamelCase.containsKey("${alias}")) {
#******#            temp = mapAnyCamelCase.get("${alias}");
#******#            ${field.Type} _${field.LowerCamel} = parser.${field.Klass}FromMap(temp);
#******#            target.set${field.UpperCamel}(_${field.LowerCamel});
#******#        }
#**##end
#end
    }
