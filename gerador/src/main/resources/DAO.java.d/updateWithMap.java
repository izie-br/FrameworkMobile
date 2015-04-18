    @Override
    public void updateWithMap(${Target} target, Map<String,Object> map) throws IOException {
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
#******#            DAO<${association.Klass}> ${association.KeyToA}AGdao = this.factory.getDaoFor(${association.Klass}.class);
#******#            ${association.Klass} ${association.KeyToA}AG = parser.extractAssociation(
#******#                    mapAnyCamelCase, ${association.KeyToA}AGdao, "${association.KeyToA}", "${alias}");
#******#            target.set${association.KeyToA}(${association.KeyToA}AG);
#******#        }
#**##elseif (!$field.PrimaryKey)
#******#        if (mapAnyCamelCase.containsKey("${alias}")) {
#******#            temp = mapAnyCamelCase.get("${alias}");
#******#            ${field.Type} ${field.LowerCamel}AG = parser.${field.Klass}FromMap(temp);
#******#            target.set${field.UpperCamel}(${field.LowerCamel}AG);
#******#        }
#**##end
#end
    }
