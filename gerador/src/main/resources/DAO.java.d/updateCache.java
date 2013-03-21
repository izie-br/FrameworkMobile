    public boolean updateCache($Target target) {
        Serializable pks [] = new Serializable[]{
            target.${getter[$primaryKey]}()
        };
        $Target cacheItem = (${Target})factory.cacheLookup(${Target}.class, pks);
        if (cacheItem == null)
            return false;
        if (cacheItem == target) {
            return true;
        } else {
            if (cacheItem instanceof ${EditableInterface}) {
                $EditableInterface editable = (${EditableInterface})cacheItem;
#foreach ($field in $fields)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#               ${association.Klass} _${association.KeyToA} = target.get${association.KeyToA}();
#******#               ${association.Klass} _cache${association.KeyToA} = editable.get${association.KeyToA}();
#******#               if ( _${association.KeyToA} == null) {
#******#                   if (_cache${association.KeyToA} != null) {
#******#                       editable.set${association.KeyToA}(null);
#******#                   }
#******#               } else {
#******#                   if ( !(((${field.Klass})_${association.KeyToA}.${getter[$association.ReferenceKey]}()).equals(((${field.Klass})_cache${association.KeyToA}.${getter[$association.ReferenceKey]}())))) {
#******#                       editable.set${association.KeyToA}(this.factory.getDaoFor(${association.Klass}.class).get(_${association.KeyToA}.${getter[$association.ReferenceKey]}()));
#******#                   }
#******#               }
#**##elseif (!$field.PrimaryKey)
#******#               ${field.Klass} _${field.LowerCamel} = target.${getter[$field]}();
#******#               ${field.Klass} _cache${field.LowerCamel} = editable.${getter[$field]}();
#******#               boolean _${field.LowerCamel}Changed = ( _${field.LowerCamel} == null)?
#******#                       (_cache${field.LowerCamel} != null) :
#******#                       !(_cache${field.LowerCamel}.equals(_${field.LowerCamel}));
#******#               if (_${field.LowerCamel}Changed) {
#******#                   editable.set${field.UpperCamel}(_${field.LowerCamel});
#******#               }
#**##end
#end
                return true;
            } else {
                LogPadrao.e("${table} nao editavel em cache");
                return false;
            }
        }
    }
