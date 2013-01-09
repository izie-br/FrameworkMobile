package $package;

#if ( $haveDateField && (!$editableInterface || $hasDatePK) )
#**#import java.util.Date;
#end
#if ($oneToManyAssociations.size() > 0 || $manyToManyAssociations.size() > 0)
#**#import com.quantium.mobile.framework.query.QuerySet;
#end
#if ($implementation)
#**#import ${basePackage}.GenericBean;
#**#import java.util.Map;
#elseif ($interface)
#**#import com.quantium.mobile.framework.MapSerializable;
#**#import com.quantium.mobile.framework.query.Table;
#**#import ${basePackage}.GenericVO;
#end


#if ($implementation)
#**#public class ${Filename} extends GenericBean implements ${EditableInterface}
#elseif ($interface)
#**#public interface ${Klass} extends GenericVO, MapSerializable<${Klass}>
#elseif ($editableInterface)
#**#public interface ${Filename} extends ${Klass}
#end
{

#if ($interface)
#**##parse("VO.java.d/tableAndColumns.java")
#**#
#end
##
#if ($implementation)
#**##parse("VO.java.d/fieldsAndConstructor.java")
#**#
#**##parse("VO.java.d/interfaceMethodsImpl.java")
#end
##
#foreach ($field in $fields)
#**##if ( ($interface || $implementation) && $field.Get)
#******#    public ${field.Type} ${getter[$field]} () #if($implementation){
#******#        return ${field.LowerCamel};
#******#    }#else;#end
#******#
#******#
#**##end
#**##if ( (!$associationForField[$field] && $implementation) ||
          (!$editableInterface && $field.Set && !$primaryKeys.contains($field)) ||
          ($primaryKeys.contains($field) && $editableInterface && !$associationForField[$field])
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
#**##end
#**##if (!$primaryKeys.contains($association.ForeignKey) || $implementation || $editableInterface )
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

}
