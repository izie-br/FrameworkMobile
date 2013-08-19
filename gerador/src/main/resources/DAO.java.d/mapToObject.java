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
#******#        DAO<${association.Klass}> _${association.KeyToA}dao = this.factory.getDaoFor(${association.Klass}.class);
#******#        ${association.Klass} _${association.KeyToA} = parser.extractAssociation(
#******#                mapAnyCamelCase, _${association.KeyToA}dao, "${association.KeyToA}", "${alias}");
#**##else
#******#        temp = mapAnyCamelCase.get("${alias}");
#******#        ${field.Type} _${field.LowerCamel} = parser.${field.Klass}FromMap(temp);
#**##end
#end

        ${Target} target = new${Target}(${constructorArgs});
        return target;
    }
