    @Override
    public Collection<ValidationError> validate () {
        Collection<ValidationError> errors = new ArrayList <ValidationError>();
#foreach ($field in $NotNull)
#**##if (!$field.PrimaryKey)
#******##if ($associationForField[$field])
#**********##set ($association = $associationForField[$field])
#**********#        if (this._${association.KeyToA} == null ||
#**********#            this._${association.KeyToA}.get${association.ReferenceKey.UpperCamel}() == ${defaultId})
#**********#        {
#**********#            errors.add (new ValidationError (
#**********#                    ${field.UpperAndUnderscores},
#**********#                    Constraint.notNull()));
#**********#        }
#******##elseif ($field.Klass.equals("String") || $field.Klass.equals("Date"))
#**********#        if (this.${field.LowerCamel} == null) {
#**********#            errors.add (new ValidationError (
#**********#                    ${field.UpperAndUnderscores},
#**********#                    Constraint.notNull()));
#**********#        }
#******##end
#**##end
#end
##
## Max
##
#foreach ($field in $MaxConstraints.keySet())
#**##if (!$associationForField[$field])
#******##set($value = $MaxConstraints[$field].Value)
#******##if ($field.Klass.equals("String"))
#**********#         if (this.${field.LowerCamel} != null &&
#**********#             this.${field.LowerCamel}.length() > $value)
#**********#        {
#**********#            errors.add (new ValidationError (
#**********#                    ${field.UpperAndUnderscores},
#**********#                    Constraint.max(${value})));
#**********#        }
#******##elseif ($field.Klass.equals("Long"))
#**********#        if (this.${field.LowerCamel} > ${value}L) {
#**********#            errors.add (new ValidationError (
#**********#                    ${field.UpperAndUnderscores},
#**********#                    Constraint.max(${value}L)));
#**********#        }
#******##end
#**##end
#end
##
## Min
##
#foreach ($field in $MinConstraints.keySet())
#**##if (!$associationForField[$field])
#******##set($value = $MinConstraints[$field].Value)
#******##if ($field.Klass.equals("String"))
#**********#         if (this.${field.LowerCamel} != null &&
#**********#             this.${field.LowerCamel}.length() < ${value})
#**********#        {
#**********#            errors.add (new ValidationError (
#**********#                    ${field.UpperAndUnderscores},
#**********#                    Constraint.min((int)${value})));
#**********#        }
#******##elseif ($field.Klass.equals("Long"))
#**********#        if (this.${field.LowerCamel} < ${value}L) {
#**********#            errors.add (new ValidationError (
#**********#                    ${field.UpperAndUnderscores},
#**********#                    Constraint.min(${value}L)));
#**********#        }
#******##end
#**##end
#end
##
## Length
##
#foreach ($field in $LengthConstraints.keySet())
#**##if (!$associationForField[$field])
#******##set($value = $LengthConstraints[$field].Value)
#******##if ($field.Klass.equals("String"))
#**********#         if (this.${field.LowerCamel} != null &&
#**********#             this.${field.LowerCamel}.length() != ${value})
#**********#        {
#**********#            errors.add (new ValidationError (
#**********#                    ${field.UpperAndUnderscores},
#**********#                    Constraint.length((int)${value})));
#**********#        }
#******##end
#**##end
#end
##
##
        return errors;
    }
