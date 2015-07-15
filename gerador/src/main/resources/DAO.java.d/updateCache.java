    public boolean updateCache($Target target) throws IOException {
        Serializable pks [] = new Serializable[]{
            target.${getter[$primaryKey]}()
        };
        $Target cacheItem =
            (${Target}) 
                factory.cacheLookup(${Target}.class, pks);
        if (cacheItem == null){
            return false;
        }
        if (cacheItem == target) {
            return true;
        } else {
            if (cacheItem instanceof ${EditableInterface}) {
                $EditableInterface editable = (${EditableInterface}) cacheItem;
#foreach ($field in $fields)
#**##if ($associationForField[$field])
#******##set ($association = $associationForField[$field])
#******#               ${association.Klass} ${association.KeyToA}AG = target.get${association.KeyToA}();
#******#               ${association.Klass} cache${association.KeyToA}AG = editable.get${association.KeyToA}();
#******#               if (${association.KeyToA}AG == null) {
#******#                   if (cache${association.KeyToA}AG != null) {
#******#                       editable.set${association.KeyToA}(null);
#******#                   }
#******#               } else {
#******#                   if (cache${association.KeyToA}AG == null ||
#******#                            !(((${field.Klass}) ${association.KeyToA}AG
#******#                                    .${getter[$association.ReferenceKey]}())
#******#                                    .equals(((${field.Klass}) cache${association.KeyToA}AG
#******#                                    .${getter[$association.ReferenceKey]}())))) {
#******#                       editable.set${association.KeyToA}(
#******#                            this.factory.getDaoFor(${association.Klass}.class).get(${association.KeyToA}AG.${getter[$association.ReferenceKey]}()));
#******#                   }
#******#               }
#**##elseif (!$field.PrimaryKey)
#******#               ${field.Klass} ${field.LowerCamel}AG = target.${getter[$field]}();
#******#               ${field.Klass} cache${field.LowerCamel}AG = editable.${getter[$field]}();
#******#               boolean ${field.LowerCamel}AGChanged = (${field.LowerCamel}AG == null) ?
#******#                       (cache${field.LowerCamel}AG != null) :
#******#                       !(${field.LowerCamel}AG.equals(cache${field.LowerCamel}AG));
#******#               if (${field.LowerCamel}AGChanged) {
#******#                   editable.set${field.UpperCamel}(${field.LowerCamel}AG);
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
