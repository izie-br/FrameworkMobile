##
## mapToObject
##
    @Override
    public $Target mapToObject(Map<String, Object> map)
        throws ClassCastException
    {
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
#******#        DAO<${association.Klass}> ${association.KeyToA}_dao = this.factory.getDaoFor(${association.Klass}.class);
#******#        ${association.Klass} ${association.KeyToA}_ = parser.extractAssociation(
#******#                mapAnyCamelCase, ${association.KeyToA}_dao, "${association.KeyToA}", "${alias}");
#**##else
#******#        temp = mapAnyCamelCase.get("${alias}");
#******#        ${field.Type} ${field.LowerCamel}_ = parser.${field.Klass}FromMap(temp);
#**##end
#end

        ${Target} target = new${Target}(${constructorArgs});
        return target;
    }
