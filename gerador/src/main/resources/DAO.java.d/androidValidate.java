    @Override
    public Collection<ValidationError> validate (${Target} target) {
        Collection<ValidationError> errors = new ArrayList<ValidationError>();
        errors.addAll(target.validate ());
##TODO teste de unique
#foreach ($fields in $Uniques)
#**##if ( ($fields.size() > 1) ||
          ($fields.size() == 1 && !$fields[0].PrimaryKey))
#******#        {
#******#            int qty = query ((
#******#                (${Target}.${primaryKey.UpperAndUnderscores}) .ne (target.${getter[$primaryKey]} ())
#******##foreach ($field in $fields)
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
#******#            )).all ().size ();
#******#            if (qty > 0) {
#******##if ($fields.size() == 1)
#**********#                errors.add (new ValidationError (
#**********#                    ${Target}.${fields[0].UpperAndUnderscores},
#**********#                    Constraint.unique()
#**********#                ));
#******##else
#**********#                errors.add (new ValidationError (null, new Constraint (
#**********#                    Constraint.UNIQUE,
#**********##foreach ($field in $fields)
#**************#                    ${Target}.${field.UpperAndUnderscores}#if ($foreach.count < $fields.size()),#end
#**********##end
#**********#                )));
#******##end
#******#            }
#******#        }
#**##end
#end
        return errors;
    }
