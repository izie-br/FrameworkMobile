package $package;

#if ( $haveDateField && (!$editableInterface || $hasDatePK) )
#**#import java.util.Date;
#end
#if ($oneToManyAssociations.size() > 0 || $manyToManyAssociations.size() > 0)
#**#import com.quantium.mobile.framework.query.QuerySet;
#end
#if ($implementation)
#**##if ($NotNull.size() > 0)
#******#import com.quantium.mobile.framework.validation.Constraint;
#**##end
#**#import ${basePackage}.GenericBean;
#**#import java.util.ArrayList;
#**#import java.util.Collection;
#**#import java.util.Map;
#**#import com.quantium.mobile.framework.validation.ValidationError;
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
#**##parse("VO.java.d/validate.java")
#**#
#**##parse("VO.java.d/interfaceMethodsImpl.java")
#end
##
#parse("VO.java.d/gettersAndSetters.java")

}
