    @Override
    public Collection<ValidationError> validate (${Target} target) {
        Collection<ValidationError> errors = new ArrayList<ValidationError>();
##TODO teste de unique
###foreach ($fields in $Uniques)
###**##if ( ($fields.size() > 1) ||
##          ($fields.size() == 1 && !$fields[0].PrimaryKey))
###******##foreach ($field in $fields)
###**********##if ($associationForField[$field])
###**************##set ($association = $associationForField[$field])
###**************#        ${association.Klass} _${association.KeyToA} = target.get${association.KeyToA}();
###**************#        ${field.Klass} _${field.LowerAndUndercores} = _${association.KeyToA} == null ? null : _${association.KeyToA}.${getter[$association.ReferenceKey]} ();
###**********##else
###**********##end
###******##end
###**##end
###end
        return errors;
    }
