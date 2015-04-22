#foreach ($field in $fields)
#**###
#**### O metodo getter nao deve existir se houver uma associacao one-to-many
#**### em que ele eh a FK, ou se "Set" for marcado como false
#**###
#**##if ( ($interface || $implementation) &&
          $field.Get && !$associationForField[$field]
        )
#******#    public ${field.Type} ${getter[$field]} ()#if($implementation){
#******##if ($field.Type == "Date")
#******#        if (${field.LowerCamel} == null) {
#******#            return null;
#******#        } else {
#******#            return new Date(${field.LowerCamel}.getTime());
#******#        }
#******##else
#******#        return ${field.LowerCamel};
#******##end
#******#    }#else;#end
#******#
#******#
#**##end
#**###
#**### O metodo setter nao deve ser gerado se houver uma associacao one-to-many
#**### em que ele eh a FK
#**### Se for marcado como chave primaria, ou "Set" como false, deve existir na
#**### implementacao e na interface editavel.
#**###
#**##if ( !$associationForField[$field] && (
            $implementation ||
            ($interface && $field.Set && !$field.PrimaryKey) ||
            ($field.PrimaryKey && $editableInterface)
          )
        )
#******#    public void set${field.UpperCamel}(
#******#        ${field.Type} ${field.LowerCamel})#if ($implementation) {
#******##if ($field.Type == "Date")
#******#        if (${field.LowerCamel} == null) {
#******#            this.${field.LowerCamel} = null;
#******#        } else {
#******#            this.${field.LowerCamel} = new Date(${field.LowerCamel}.getTime());
#******#        }
#******##else
#******#        this.${field.LowerCamel} = ${field.LowerCamel};
#******##end
#******#        triggerObserver("${field.LowerAndUnderscores}");
#******#    }#else;#end
#******#
#******#
#**##end
#end
##
#foreach ($association in $manyToOneAssociations)
#**##if ($interface || $implementation)
#******#    public ${association.Klass} get${association.KeyToA}() #if ($implementation){
#******#        return ${association.KeyToA}AG;
#******#    }#else;#end
#******#
#******#
#******#    public void set${association.KeyToA}(${association.Klass} obj) #if ($implementation) {
#******#        ${association.KeyToA}AG = obj;
#******#    }#else;#end
#******#
#******#
#**##end
#end
##
#foreach ($association in $oneToManyAssociations)
#**##if ($interface || $implementation)
#******#    public QuerySet<${association.Klass}> get${association.KeyToAPluralized}() #if ($implementation) {
#******#        return ${association.KeyToAPluralized}AG;
#******#    }#else;#end
#******#
#******#
#**##end
#**##if ($implementation || $editableInterface)
#******#    public void set${association.KeyToAPluralized}(
#******#        QuerySet<${association.Klass}> querySet) #if ($implementation) {
#******#        this.${association.KeyToAPluralized}AG = querySet;
#******#    }#else;#end
#******#
#******#
#**##end
#end
#foreach ($association in $manyToManyAssociations)
#**##if ($interface || $implementation)
#******#    public QuerySet<${association.Klass}> get${association.Pluralized}() #if ($implementation) {
#******#        return ${association.Pluralized}AG;
#******#    }#else;#end
#******#
#******#
#**##end
#**##if ($implementation || $editableInterface)
#******#    public void set${association.Pluralized}(
#******#        QuerySet<${association.Klass}> querySet) #if ($implementation) {
#******#        this.${association.Pluralized}AG = querySet;
#******#    }#else;#end
#******#
#******#
#**##end
#end
