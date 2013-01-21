##
## mapToObject
##
#if ($manyToOneAssociations.size() >0 )
#**#    @SuppressWarnings("unchecked")
#end
    @Override
    public $Target mapToObject(Map<String, Object> map)
        throws ClassCastException
    {
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
#******#        Object ${submap} = mapAnyCamelCase.get("${association.Klass}");
#******#        ${association.Klass} _${association.KeyToA};
#******#        if (${submap} != null && ${submap} instanceof Map){
#******#            _${association.KeyToA} = factory.getDaoFor(${association.Klass}.class)
#******#                .mapToObject((Map<String,Object>)${submap});
#******#        } else {
#******#            temp = mapAnyCamelCase.get("${alias}");
#******#            if (temp != null && temp instanceof String)
#******#                temp = Long.parseLong((String)temp);
#******#            long ${field.LowerCamel} = ((temp!= null)?((Number) temp).longValue(): ${defaultId});
#******#            DAO<${association.Klass}> dao = this.factory.getDaoFor(${association.Klass}.class);
#******#            _${association.KeyToA} = (${field.LowerCamel} != ${defaultId})?
#******#                dao.query(${association.Klass}.${association.ReferenceKey.UpperAndUnderscores}.eq((Long)${field.LowerCamel})).first():
#******#                null;
#******#        }
#**##else
#******#        temp = mapAnyCamelCase.get("${alias}");
#******##if (${field.Klass} == "Long" || ${field.Klass} == "Double")
#**********#        if (temp != null && temp instanceof String)
#**********#            temp = ${field.Klass}.parse${field.Klass}((String)temp);
#**********#        ${field.Type} _${field.LowerCamel} = ((temp!= null)?((Number) temp).${field.Type}Value(): ${fallback});
#******##elseif (${field.Klass} == "Boolean")
#**********#        if (temp != null && temp instanceof String){
#**********#            if (temp.equals("0"))
#**********#                temp = false;
#**********#            else if (temp.equals("1"))
#**********#                temp = true;
#**********#            else
#**********#                temp = Boolean.parseBoolean((String)temp);
#**********#        }
#**********#        ${field.Type} _${field.LowerCamel} = ((temp!= null)?((Boolean) temp): ${fallback});
#******##elseif (${field.Klass} == "Date")
#**********#        if (temp != null && temp instanceof String)
#**********#            temp = DateUtil.stringToDate((String)temp);
#**********#        ${field.Type} _${field.LowerCamel} = ((temp!= null)? ((${field.Klass})temp): ${fallback});
#******##else
#**********#        ${field.Type} _${field.LowerCamel} = ((temp!= null)? ((${field.Klass})temp): ${fallback});
#******##end
#**##end
#end

        ${KlassImpl} target = new ${KlassImpl}(${constructorArgs});
        return target;
    }
