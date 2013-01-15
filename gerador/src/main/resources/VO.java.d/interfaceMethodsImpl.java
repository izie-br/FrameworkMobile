    @Override
    public void toMap(Map<String, Object> map) {
#foreach ($field in $fields)
#**##if ($field.SerializationAlias)
#******##set ($alias = $field.SerializationAlias)
#**##else
#******##set ($alias = $field.LowerAndUnderscores)
#**##end
#**###
#**##set ($association = false)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#        $field.Type $field.LowerCamel = (_${association.Klass} == null)? 0 : _${association.Klass}.get${association.ReferenceKey.UpperCamel}();
#**##end
#**##if ($field.PrimaryKey || $association)
#******#        if (${field.LowerCamel} != ${defaultId}) {
#******#            map.put("${alias}", ${field.LowerCamel});
#******#        }
#**##else
#******#        map.put("${alias}", ${field.LowerCamel});
#**##end
#end
    }

    @Override
    public int hashCodeImpl() {
        int value = 1;
#foreach ($field in $fields)
#**##if (!$associationForField[$field])
#******##if ($field.Klass.equals("Boolean"))
#**********#        value += (${field.LowerCamel}) ? 1 : 0;
#******##elseif ($field.Klass.equals("Integer") || $field.Klass.equals("Long") || $field.Klass.equals("Double"))
#**********#        value +=(int) ${field.LowerCamel};
#******##else
#**********#        value *= (${field.LowerCamel} == null) ? 1 : ${field.LowerCamel}.hashCode();
#******##end
#**##end
#end
        return value;
    }

    @Override
    public boolean equalsImpl(Object obj) {
        if (obj == null) {
            return false;
        }
        if ( !(obj instanceof ${Klass}) ) {
            return false;
        }
        ${Klass} other = ((${Klass}) obj);
#foreach ($field in $fields)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******##set ($otherAssoc = "other${association.Klass}")
#******#        ${association.Klass} ${otherAssoc} = other.get${association.Klass}();
#******#        if (_${association.Klass} == null){
#******#            if (${otherAssoc} != null)
#******#                return false;
#******#        } else {
#******#            if (${otherAssoc} == null)
#******#                return false;
#******#            if (_${association.Klass}.get${association.ReferenceKey.UpperCamel}() != ${otherAssoc}.get${association.ReferenceKey.UpperCamel}())
#******#                return false;
#******#        }
#**##else
#******##if ($field.Klass.equals("Boolean") || $field.Klass.equals("Long")|| $field.Klass.equals("Integer") || $field.Klass.equals("Double"))
#**********#        if(${field.LowerCamel} != other.${getter[$field]}())
#**********#            return false;
#******##else
#**********#        if( ( ${field.LowerCamel}==null)? (other.${getter[$field]}() != null) :  !${field.LowerCamel}.equals(other.${getter[$field]}()) )
#**********#            return false;
#******##end
#**##end
#end
        return true;
    }
