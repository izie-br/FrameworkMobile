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
#**##if ($field.PrimaryKey)
#******##set ($fallback = $defaultId)
#**##elseif (${field.Klass} == "Long" || ${field.Klass} == "Double")
#******##set ($fallback = "0")
#**##elseif (${field.Klass} == "Boolean")
#******##set ($fallback = "false")
#**##else
#******##set ($fallback = "null")
#**##end
#**###
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******##set ($submap = "submapFor${association.KeyToA}")
#******#        Object ${submap} = mapAnyCamelCase.get("${association.KeyToA}");
#******#        ${association.Klass} _${association.KeyToA} = null;
#******#        if (${submap} != null && ${submap} instanceof Map){
#******#            @SuppressWarnings("unchecked")
#******#            Map<String,Object> submap = (Map<String,Object>)${submap};
#******#            DAO<${association.Klass}> dao = this.factory.getDaoFor(${association.Klass}.class);
#******#            _${association.KeyToA} = dao.mapToObject(submap);
#******#        } else {
#******#            temp = mapAnyCamelCase.get("${alias}");
#******#            ${field.Type} _${field.LowerCamel} = parser.parse${field.Klass}(temp);
#******#            if (_${field.LowerCamel} != ${defaultId}) {
#******#                DAO<${association.Klass}> dao = this.factory.getDaoFor(${association.Klass}.class);
#******#                _${association.KeyToA} = dao.get(_${field.LowerCamel});
#******#            }
#******#        }
#**##else
#******#        temp = mapAnyCamelCase.get("${alias}");
#******#        ${field.Type} _${field.LowerCamel} = parser.parse${field.Klass}(temp);
#**##end
#end

        ${Target} target = new${Target}(${constructorArgs});
        return target;
    }
