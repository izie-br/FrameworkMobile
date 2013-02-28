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
#foreach ($field in $MaxConstraints.keySet())
#**##set($maxValue = $MaxConstraints[$field].Value)
#**##if ($field.Klass.equals("String"))
#******#         if (this.${field.LowerCamel} != null &&
#******#             this.${field.LowerCamel}.length() > $maxValue)
#******#        {
#******#            errors.add (new ValidationError (
#******#                    ${field.UpperAndUnderscores},
#******#                    Constraint.max((int)${maxValue})));
#******#        }
#**##end
#end
#foreach ($field in $MinConstraints.keySet())
#**##set($maxValue = $MinConstraints[$field].Value)
#**##if ($field.Klass.equals("String"))
#******#         if (this.${field.LowerCamel} != null &&
#******#             this.${field.LowerCamel}.length() > $maxValue)
#******#        {
#******#            errors.add (new ValidationError (
#******#                    ${field.UpperAndUnderscores},
#******#                    Constraint.min((int)${maxValue})));
#******#        }
#**##end
#end
#foreach ($field in $LengthConstraints.keySet())
#**##set($maxValue = $LengtConstraints[$field].Value)
#**##if ($field.Klass.equals("String"))
#******#         if (this.${field.LowerCamel} != null &&
#******#             this.${field.LowerCamel}.length() > $maxValue)
#******#        {
#******#            errors.add (new ValidationError (
#******#                    ${field.UpperAndUnderscores},
#******#                    Constraint.length((int)${maxValue})));
#******#        }
#**##end
#end
        return errors;
    }
