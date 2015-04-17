    public boolean updateCache($Target target) throws IOException {
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
#******#               ${association.Klass} ${association.KeyToA}_ = target.get${association.KeyToA}();
#******#               ${association.Klass} _cache${association.KeyToA} = editable.get${association.KeyToA}();
#******#               if (${association.KeyToA}_ == null) {
#******#                   if (_cache${association.KeyToA} != null) {
#******#                       editable.set${association.KeyToA}(null);
#******#                   }
#******#               } else {
#******#                   if (_cache${association.KeyToA} == null ||
#******#                            !(((${field.Klass})${association.KeyToA}_.${getter[$association.ReferenceKey]}()).equals(((${field.Klass})_cache${association.KeyToA}.${getter[$association.ReferenceKey]}())))) {
#******#                       editable.set${association.KeyToA}(
#******#                            this.factory.getDaoFor(${association.Klass}.class).get(${association.KeyToA}_.${getter[$association.ReferenceKey]}()));
#******#                   }
#******#               }
#**##elseif (!$field.PrimaryKey)
#******#               ${field.Klass} ${field.LowerCamel}_ = target.${getter[$field]}();
#******#               ${field.Klass} _cache${field.LowerCamel} = editable.${getter[$field]}();
#******#               boolean ${field.LowerCamel}_Changed = (${field.LowerCamel}_ == null) ?
#******#                       (_cache${field.LowerCamel} != null) :
#******#                       !(${field.LowerCamel}_.equals(_cache${field.LowerCamel}));
#******#               if (${field.LowerCamel}_Changed) {
#******#                   editable.set${field.UpperCamel}(${field.LowerCamel}_);
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
