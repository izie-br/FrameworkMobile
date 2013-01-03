##
## mapToObject
##
#if ($manyToOneAssociations.size() >0 )
    @SuppressWarnings("unchecked")
#end##($manyToOneAssociations.size() >0 )
    @Override
    public $Target mapToObject(Map<String, Object> map)
        throws ClassCastException
    {
        CamelCaseUtils.AnyCamelMap<Object> mapAnyCamelCase =
            new CamelCaseUtils.AnyCamelMap<Object>();
        mapAnyCamelCase.putAll(map);
        Object temp;
#foreach ($field in $fields)
#if ($field.SerializationAlias)
#set ($alias = $field.SerializationAlias)
#else##if_not_alias
#set ($alias = $field.LowerCamel)
#end##end_if_alias
#if ($primaryKeys.contains($field))
#set ($fallback = $defaultId)
#elseif (${field.Klass} == "Long" || ${field.Klass} == "Double")
#set ($fallback = "0")
#elseif (${field.Klass} == "Boolean")
#set ($fallback = "false")
#else
#set ($fallback = "null")
#end##if_primary_key
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
#set ($submap = "submapFor${association.Klass}")
        Object ${submap} = mapAnyCamelCase.get("${association.Klass}");
        ${association.Klass} _${association.Klass};
        if (${submap} != null && ${submap} instanceof Map){
            _${association.Klass} = factory.getDaoFor(${association.Klass}.class)
                .mapToObject((Map<String,Object>)${submap});
        } else {
            temp = mapAnyCamelCase.get("${alias}");
            if (temp != null && temp instanceof String)
                temp = Long.parseLong((String)temp);
            long ${field.LowerCamel} = ((temp!= null)?((Number) temp).longValue(): ${defaultId});
            DAO<${association.Klass}> dao = this.factory.getDaoFor(${association.Klass}.class);
            _${association.Klass} = (${field.LowerCamel} != ${defaultId})?
                dao.query(${association.Klass}.${association.ReferenceKey.UpperAndUnderscores}.eq((Long)${field.LowerCamel})).first():
                null;
        }
#else##if (!$associationForField[$field])
        temp = mapAnyCamelCase.get("${alias}");
#if (${field.Klass} == "Long" || ${field.Klass} == "Double")
        if (temp != null && temp instanceof String)
            temp = ${field.Klass}.parse${field.Klass}((String)temp);
        ${field.Type} _${field.LowerCamel} = ((temp!= null)?((Number) temp).${field.Type}Value(): ${fallback});
#elseif (${field.Klass} == "Boolean")
        if (temp != null && temp instanceof String){
            if (temp.equals("0"))
                temp = false;
            else if (temp.equals("1"))
                temp = true;
            else
                temp = Boolean.parseBoolean((String)temp);
        }
        ${field.Type} _${field.LowerCamel} = ((temp!= null)?((Boolean) temp): ${fallback});
#elseif (${field.Klass} == "Date")
        if (temp != null && temp instanceof String)
            temp = DateUtil.stringToDate((String)temp);
        ${field.Type} _${field.LowerCamel} = ((temp!= null)? ((${field.Klass})temp): ${fallback});
#else##if_Klass_eq_***
        ${field.Type} _${field.LowerCamel} = ((temp!= null)? ((${field.Klass})temp): ${fallback});
#end##if_Klass_eq_***
#end##if (!$associationForField[$field])
#end##foreach

#set ($argCount = $fields.size() + $toManyAssociations.size())
        ${KlassImpl} target = new ${KlassImpl}(
#foreach ($field in $fields)
#set ($columnIndex = $foreach.index)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
            _${association.Klass}#if ($foreach.count < $argCount),#end

#else##if (!$associationForField[$field])
            _${field.LowerCamel}#if ($foreach.count < $argCount),#end

#end##if ($associationForField[$field])
#end##foreach ($field in $fields)
#foreach ($association in $oneToManyAssociations)
#set ($argIndex = $fields.size() + $foreach.count)
            querySetFor${association.Pluralized}(_${association.ReferenceKey.LowerCamel})#if ($argIndex < $argCount),#end

#end##foreach ($association in $toManyAssociations)
#foreach ($association in $manyToManyAssociations)
#set ($argIndex = $fields.size() + $oneToManyAssociations.size() + $foreach.count)
#if ($association.IsThisTableA)
            querySetFor${association.Pluralized}(_${association.ReferenceA.LowerCamel})#if ($argIndex < $argCount),#end
#else
            querySetFor${association.Pluralized}(_${association.ReferenceB.LowerCamel})#if ($argIndex < $argCount),#end
#end

#end##foreach ($association in $manyToManyAssociations)
        );
        //target._daofactory = this.factory;
        return target;
    }

