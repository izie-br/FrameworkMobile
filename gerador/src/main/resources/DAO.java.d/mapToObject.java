##
## mapToObject
##
    @Override
    public $Target mapToObject(Map<String, Object> map)
        throws ClassCastException, IOException
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
#******#        DAO<${association.Klass}> ${association.KeyToA}AGdao = this.factory.getDaoFor(${association.Klass}.class);
#******#        ${association.Klass} ${association.KeyToA}AG = parser.extractAssociation(
#******#                mapAnyCamelCase, ${association.KeyToA}AGdao, "${association.KeyToA}", "${alias}");
#**##else
#******#        temp = mapAnyCamelCase.get("${alias}");
#******#        ${field.Type} ${field.LowerCamel}AG = parser.${field.Klass}FromMap(temp);
#**##end
#end

        ${Target} target = new${Target}(${constructorArgs});
        return target;
    }
