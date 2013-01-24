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
#**********#                    new Constraint (Constraint.NOT_NULL)));
#**********#        }
#******##elseif ($field.Klass.equals("String") || $field.Klass.equals("Date"))
#**********#        if (this.${field.LowerCamel} == null) {
#**********#            errors.add (new ValidationError (
#**********#                    ${field.UpperAndUnderscores},
#**********#                    new Constraint (Constraint.NOT_NULL)));
#**********#        }
#******##end
#**##end
#end
        return errors;
    }
