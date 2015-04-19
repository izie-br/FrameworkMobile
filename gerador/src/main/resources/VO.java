package $package;
/*
 * Copyright (c) 2014 Izie.
 *
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.quantium.mobile.framework.MapSerializable;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.validation.ValidationError;
import com.quantium.mobile.framework.validation.Constraint;
import ${basePackage}.GenericBean;
import ${basePackage}.GenericVO;

${Imports}

@SuppressWarnings("unused")
#if ($implementation)
#**#public abstract class ${Filename}
#**#    extends GenericBean
#**#    implements ${EditableInterface} {
#elseif ($interface)
#**#public interface ${Filename}
#**#    extends GenericVO, MapSerializable {
#elseif ($editableInterface)
#**#public interface ${Filename}
#**#    extends ${Klass} {
#end

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
