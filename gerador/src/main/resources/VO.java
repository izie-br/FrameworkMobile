package $package;

import java.util.Map;
#foreach ($field in $fields)
#if ( $field.Klass.equals("Date") )
import java.util.Date;
#break
#end##if
#end##foreach
import com.quantium.mobile.framework.MapSerializable;
import com.quantium.mobile.framework.DAOFactory;
#if ($oneToManyAssociations.size() > 0 || $manyToManyAssociations.size() > 0)
import com.quantium.mobile.framework.query.QuerySet;
#end
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.utils.CamelCaseUtils;
import ${basePackage}.GenericBean;

public class $Klass extends GenericBean implements MapSerializable<${Klass}>{

    public final static Table _TABLE = new Table("${table}");
#foreach ($field in $fields)
    public final static Table.Column<${field.Klass}> ${field.UpperAndUnderscores} = _TABLE.addColumn(${field.Klass}.class, "${field.LowerAndUnderscores}");
#end
#foreach ($association in $manyToManyAssociations)
#if ($association.IsThisTableA)
    public final static Table _${association.JoinTableUpper} = new Table("${association.JoinTable}");
    public final static Table.Column<${association.KeyToB.Klass}> _${association.JoinTableUpper}_${association.KeyToB.UpperAndUnderscores} =
        _${association.JoinTableUpper}.addColumn(${association.KeyToB.Klass}.class, "${association.KeyToB.LowerAndUnderscores}");
    public final static Table.Column<${association.KeyToA.Klass}> _${association.JoinTableUpper}_${association.KeyToA.UpperAndUnderscores} =
        _${association.JoinTableUpper}.addColumn(${association.KeyToA.Klass}.class, "${association.KeyToA.LowerAndUnderscores}");
#else
    public final static Table _${association.JoinTableUpper} = ${association.Klass}._${association.JoinTableUpper};
    public final static Table.Column<${association.KeyToB.Klass}> _${association.JoinTableUpper}_${association.KeyToB.UpperAndUnderscores} =
        ${association.Klass}._${association.JoinTableUpper}_${association.KeyToB.UpperAndUnderscores};
    public final static Table.Column<${association.KeyToA.Klass}> _${association.JoinTableUpper}_${association.KeyToA.UpperAndUnderscores} =
        ${association.Klass}._${association.JoinTableUpper}_${association.KeyToA.UpperAndUnderscores};
#end
#end

#foreach ($field in $fields)
    ${field.Type} ${field.LowerCamel}#if ($primaryKeys.contains($field)) = ${defaultId}#end;
#end

    DAOFactory _daofactory;
    private final static long serialVersionUID = ${serialVersionUID};

#foreach ($field in $fields)
    public ${field.Type} get${field.UpperCamel}(){
        return ${field.LowerCamel};
    }

    public void set${field.UpperCamel}(${field.Type} ${field.LowerCamel}){
        this.${field.LowerCamel} = ${field.LowerCamel};
        triggerObserver("${field.LowerAndUnderscores}");
    }

#end
#foreach ($association in $oneToManyAssociations)
    public QuerySet<${association.Klass}> get${association.Pluralized}(){
        if (this._daofactory == null)
            return null;
        return ((${association.Klass}DAO)_daofactory.getDaoFor(${association.Klass}.class)).query(
            ${association.Klass}.${association.ForeignKey.UpperAndUnderscores}.eq(${association.ReferenceKey.LowerCamel}));
    }

#end
#foreach ($association in $manyToOneAssociations)
    public ${association.Klass} get${association.Klass}(){
        if (this._daofactory == null)
            return null;
        return ((${association.Klass}DAO)_daofactory.getDaoFor(${association.Klass}.class))
            .query(${association.Klass}.${association.ReferenceKey.UpperAndUnderscores}.eq(${association.ForeignKey.LowerCamel}))
            .first();
    }

    public void set${association.Klass}(${association.Klass} obj){
        ${association.ReferenceKey.Type} key = obj.get${association.ReferenceKey.UpperCamel}();
        if (key == ${defaultId})
            return;
        this.${association.ForeignKey.LowerCamel} = key;
    }

#end
#foreach ($association in $manyToManyAssociations)
    public QuerySet<${association.Klass}> get${association.Pluralized}(){
        if (this._daofactory == null)
            return null;
        return ((${association.Klass}DAO)_daofactory.getDaoFor(${association.Klass}.class))
#if ($association.IsThisTableA)
            .query(
                (${association.Klass}.${association.ReferenceB.UpperAndUnderscores}.eq(_${association.JoinTableUpper}_${association.KeyToB.UpperAndUnderscores}))
                .and( _${association.JoinTableUpper}_${association.KeyToA.UpperAndUnderscores}.eq(${association.ReferenceA.LowerCamel}) ));
#else
            .query(
                (${association.Klass}.${association.ReferenceA.UpperAndUnderscores}.eq(_${association.JoinTableUpper}_${association.KeyToA.UpperAndUnderscores}))
                .and( _${association.JoinTableUpper}_${association.KeyToB.UpperAndUnderscores}.eq(${association.ReferenceB.LowerCamel}) ));
#end
    }

#end
    @Override
    public Map<String, Object> toMap(Map<String, Object> map) {
#foreach ($field in $fields)
#if ($field.SerializationAlias)
#set ($alias = $field.SerializationAlias)
#else##if_not_alias
#set ($alias = $field.LowerAndUnderscores)
#end##end_if_alias
#if ($primaryKeys.contains($field))
        if (${field.LowerCamel} != ${defaultId}) {
            map.put("${alias}", ${field.LowerCamel});
        }
#else##if_primary_key
        map.put("${alias}", ${field.LowerCamel});
#end##if_primary_key
#end##foreach
        return map;
    }

    @Override
    public $Klass mapToObject(Map<String, Object> map)
        throws ClassCastException
    {
        CamelCaseUtils.AnyCamelMap<Object> mapAnyCamelCase =
            new CamelCaseUtils.AnyCamelMap<Object>();
        mapAnyCamelCase.putAll(map);
        $Klass obj = clone();
        Object temp;
#foreach ($field in $fields)
#if ($field.SerializationAlias)
#set ($alias = $field.SerializationAlias)
#else##if_not_alias
#set ($alias = $field.LowerCamel)
#end##end_if_alias
#if ($primaryKeys.contains($field))
#set ($fallback = $defaultId)
#else##if_primary_key
#set ($fallback = "this.${field.LowerCamel}")
#end##if_primary_key
        temp = mapAnyCamelCase.get("${alias}");
#if (${field.Klass} == "Long" || ${field.Klass} == "Double")
        obj.${field.LowerCamel} = ((temp!= null)?((Number) temp).${field.Type}Value(): ${fallback});
#elseif (${field.Klass} == "Boolean")
        obj.${field.LowerCamel} = ((temp!= null)?((Boolean) temp): ${fallback});
#else##if_Klass_eq_***
        obj.${field.LowerCamel} = ((temp!= null)? ((${field.Klass})temp): ${fallback});
#end##if_Klass_eq_***
#end##foreach
        return obj;
    }

    @Override
    public $Klass clone() {
        $Klass obj;
        try {
            obj = ((${Klass}) super.clone());
#foreach ($field in $fields)
#if ($field.Klass == "Date")
            obj.${field.LowerCamel} = (${field.LowerCamel} == null)? null: new Date(${field.LowerCamel}.getTime());
#end##if
#end##foreach
        } catch (CloneNotSupportedException e) {
            return null;
        }
        return obj;
    }

    @Override
    public int hashCode() {
        int value = 1;
#foreach ($field in $fields)
#if ($field.Klass.equals("Boolean"))
        value += (${field.LowerCamel}) ? 1 : 0;
#elseif ($field.Klass.equals("Integer") || $field.Klass.equals("Long") || $field.Klass.equals("Double"))
        value +=(int) ${field.LowerCamel};
#else
        value *= (${field.LowerCamel} == null) ? 1 : ${field.LowerCamel}.hashCode();
#end##if
#end##foreach
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if ( !(obj instanceof ${Klass}) ) {
            return false;
        }
        ${Klass} other = ((${Klass}) obj);
#foreach ($field in $fields)
#if ($field.Klass.equals("Boolean") || $field.Klass.equals("Long")|| $field.Klass.equals("Integer") || $field.Klass.equals("Double"))
        if(${field.LowerCamel} != other.${field.LowerCamel})
            return false;
#else
        if( ( ${field.LowerCamel}==null)? (other.${field.LowerCamel} != null) :  !${field.LowerCamel}.equals(other.${field.LowerCamel}) )
            return false;
#end##if
#end##foreach
        return true;
    }

}