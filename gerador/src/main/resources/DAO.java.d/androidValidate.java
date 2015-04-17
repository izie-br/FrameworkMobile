    @Override
    public Collection<ValidationError> validate (${Target} target) throws IOException {
        Collection<ValidationError> errors = new ArrayList<ValidationError>();
        errors.addAll(target.validate ());
##TODO teste de unique
#foreach ($uniqueFields in $Uniques)
#**##if ( ($uniqueFields.size() > 1) ||
          ($uniqueFields.size() == 1 && !$uniqueFields[0].PrimaryKey))
#******#        {
#******#            long qty = query ((
#******#                (${Target}.${primaryKey.UpperAndUnderscores}) .ne (target.${getter[$primaryKey]} ())
#******##foreach ($field in $uniqueFields)
#**********##if ($associationForField[$field])
#**************##set ($association = $associationForField[$field])
#**************#            ).and (
#**************#                (${Target}.${field.UpperAndUnderscores}).eq (
#**************#                    (target.get${association.KeyToA}() == null)?
#**************#                        null :
#**************#                        target.get${association.KeyToA} ().${getter[$association.ReferenceKey]} ())
#**********##else
#**************#            ).and (
#**************#                (${Target}.${field.UpperAndUnderscores}) .eq (target.${getter[$field]} ())
#**********##end
#******##end
#******#            ).and(${Target}.INACTIVATED_AT.isNull())).count ();
#******#            if (qty > 0) {
#******##if ($uniqueFields.size() == 1)
#**********#                errors.add (new ValidationError (
#**********#                    ${Target}.${uniqueFields[0].UpperAndUnderscores},
#**********#                    Constraint.unique()
#**********#                ));
#******##else
#**********#                errors.add (new ValidationError (null, new Constraint (
#**********#                    Constraint.UNIQUE,
#**********##foreach ($field in $uniqueFields)
#**************#                    ${Target}.${field.UpperAndUnderscores}#if ($foreach.count < $uniqueFields.size()),#end
#**********##end
#**********#                )));
#******##end
#******#            }
#******#        }
#**##end
#end
        return errors;
    }
