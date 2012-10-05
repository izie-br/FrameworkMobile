package $package;

import java.util.Map;
import com.quantium.mobile.framework.MapSerializable;
import com.quantium.mobile.framework.Session;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.test.GenericBean;

public class $Class extends GenericBean implements MapSerializable<${Class}>{

    public final static Table _TABLE = new Table("${table}");
#foreach ($field in $fields)
    public final static Table.Column<${field.Klass}> ${field.UpperAndUnderscores} = _TABLE.addColumn(${field.Klass}.class, "${field.LowerAndUnderscores}");
#end

#foreach ($field in $fields)
    ${field.Type} ${field.LowerCamel}#if ($primaryKeys.contains($field)) = ${defaultId}#end;
#end

    Session _session;
    private final static long serialVersionUID = ${serialVersionUID};

#foreach ($field in $fields)
    public ${field.Type} get${field.UpperCamel}(){
        return ${field.LowerCamel};
    }

    public void get${field.UpperCamel}(${field.Type} ${field.LowerCamel}){
        this.${field.LowerCamel} = ${field.LowerCamel};
        triggerObserver("${field.LowerAndUnderscores}");
    }

#end
    public Map<String, Object> toMap(Map<String, Object> map) {
#foreach ($field in $fields)
#if (primaryKeys.contains($field)
        if (${field.LowerCamel} != ${defaultId}) {
            map.put("#{field.LowerAndUnderscores}", ${field.LowerCamel});
        }
#else
        map.put("#{field.LowerAndUnderscores}", ${field.LowerCamel});
#end
        return map;
    }

    public Author mapToObject(Map<String, Object> map)
        throws ClassCastException
    {
        CamelCaseUtils.AnyCamelMap<Object> mapAnyCamelCase =
            new CamelCaseUtils.AnyCamelMap<Object>();
        mapAnyCamelCase.putAll(map);
        Author obj = clone();
        Object temp;
#foreach ($field in $fields)
#if ($primaryKeys.contains($field))
#set ($fallback = $defaultId)
#else
#set ($fallback = "this.${field.LowerCamel}")
#end
        temp = mapAnyCamelCase.get("${field.LowerCamel}");
#if (${field.Class} == "Long" || ${field.Class} == "Double")
        obj.${field.LowerCamel} = ((temp!= null)?((Number) temp).${field.Type}Value(): ${fallback});
#elseif (${field.Class} == "Boolean")
        obj.${field.LowerCamel} = ((temp!= null)?((Boolean) temp): ${fallback});
#else
        obj.${field.LowerCamel} = ((temp!= null)? ((${field.Class})temp): ${fallback});
#end##if
#end##foreach
        return obj;
    }
}
