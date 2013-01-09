#foreach ($field in $fields)
#**##if ( ($interface || $implementation) && $field.Get)
#******#    public ${field.Type} ${getter[$field]} () #if($implementation){
#******#        return ${field.LowerCamel};
#******#    }#else;#end
#******#
#******#
#**##end
#**##if ( (!$associationForField[$field] && $implementation) ||
          (!$editableInterface && $field.Set && !$field.PrimaryKey) ||
          ($field.PrimaryKey && $editableInterface && !$associationForField[$field])
        )
#******#    public void set${field.UpperCamel}(${field.Type} ${field.LowerCamel}) #if ($implementation){
#******#        this.${field.LowerCamel} = ${field.LowerCamel};
#******#        triggerObserver("${field.LowerAndUnderscores}");
#******#    }#else;#end
#******#
#******#
#**##end
#end
##
#foreach ($association in $manyToOneAssociations)
#**##if ($interface || $implementation)
#******#    public ${association.Klass} get${association.Klass}() #if ($implementation){
#******#        return _${association.Klass};
#******#    }#else;#end
#******#
#******#
#******#    public void set${association.Klass}(${association.Klass} obj) #if ($implementation) {
#******#        _${association.Klass} = obj;
#******#    }#else;#end
#******#
#******#
#**##end
#end
##
#foreach ($association in $toManyAssociations)
#**##if ($interface || $implementation)
#******#    public QuerySet<${association.Klass}> get${association.Pluralized}() #if ($implementation) {
#******#        return _${association.Pluralized};
#******#    }#else;#end
#******#
#******#
#**##end
#**##if ($implementation || $editableInterface)
#******#    public void set${association.Pluralized}(QuerySet<${association.Klass}> querySet) #if ($implementation) {
#******#        this._${association.Pluralized} = querySet;
#******#    }#else;#end
#******#
#******#
#**##end
#end
