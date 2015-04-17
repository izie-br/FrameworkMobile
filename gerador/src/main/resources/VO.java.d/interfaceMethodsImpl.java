    @Override
    public void toMap(Map<String, Object> map) {
#foreach ($field in $fields)
#**##if ($field.SerializationAlias)
#******##set ($alias = $field.SerializationAlias)
#**##else
#******##set ($alias = $field.LowerCamel)
#**##end
#**###
#**##set ($association = false)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#        $field.Type $field.LowerCamel = (${association.KeyToA}_ == null) ? null : ${association.KeyToA}_.get${association.ReferenceKey.UpperCamel}();
#**##end
#**##if ($field.PrimaryKey || $association)
#******#        if (${field.LowerCamel} != null) {
#******#            map.put("${alias}", ${field.LowerCamel});
#******#        }
#**##else
#******#        map.put("${alias}", ${field.LowerCamel});
#**##end
#end
    }

    @Override
    public int hashCodeImpl() {
        int value_ = 1;
#foreach ($field in $fields)
#**##if (!$associationForField[$field])
#******##if ($field.Klass.equals("Boolean"))
#**********#        value_ += (${field.LowerCamel}) ? 1 : 0;
#******##elseif ($field.Klass.equals("Integer") || $field.Klass.equals("Long") || $field.Klass.equals("Double"))
#**********#        value_ += (int) ${field.LowerCamel};
#******##else
#**********#        value_ *= (${field.LowerCamel} == null) ? 1 : ${field.LowerCamel}.hashCode();
#******##end
#**##end
#end
        return value_;
    }

    @Override
    public boolean equalsImpl(Object obj) {
        if (obj == null) {
            return false;
        }
        if ( !(obj instanceof ${Filename}) ) {
            return false;
        }
        ${Filename} other = ((${Filename}) obj);
#foreach ($field in $fields)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******##set ($otherAssoc = "other${association.KeyToA}")
#******#        ${association.Klass} ${otherAssoc} = other.get${association.KeyToA}();
#******#        if (${association.KeyToA}_ == null){
#******#            if (${otherAssoc} != null) {
#******#                return false;
#******#            }
#******#        } else {
#******#            if (${otherAssoc} == null) {
#******#                return false;
#******#            }
#******#            if (${association.KeyToA}_.get${association.ReferenceKey.UpperCamel}() != null && !${association.KeyToA}_.get${association.ReferenceKey.UpperCamel}().equals(${otherAssoc}.get${association.ReferenceKey.UpperCamel}())) {
#******#                return false;
#******#            }
#******#        }
#**##else
#******##if ($field.Klass.equals("Boolean") || $field.Klass.equals("Long")|| $field.Klass.equals("Integer")|| $field.Klass.equals("Double"))
#**********#        if (${field.LowerCamel} != other.${getter[$field]}()) {
#**********#            return false;
#**********#        }
#******##else
#**********#        if ((${field.LowerCamel} == null) ?
#**********#                (other.${getter[$field]}() != null) :
#**********#                !${field.LowerCamel}.equals(other.${getter[$field]}())) {
#**********#            return false;
#**********#        }
#******##end
#**##end
#end
        return true;
    }

