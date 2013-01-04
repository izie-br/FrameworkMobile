package $package;

#if ( $haveDateField && (!$editableInterface || $hasDatePK) )
import java.util.Date;
#end##if ( $haveDateField && (!$editableInterface || $hasDatePK) )
#if ($oneToManyAssociations.size() > 0 || $manyToManyAssociations.size() > 0)
import com.quantium.mobile.framework.query.QuerySet;
#end##if ($oneToManyAssociations.size() > 0 || $manyToManyAssociations.size() > 0)
#if ($implementation)
import ${basePackage}.GenericBean;
import java.util.Map;
#elseif ($interface)
import com.quantium.mobile.framework.MapSerializable;
import com.quantium.mobile.framework.query.Table;
import ${basePackage}.GenericVO;
#end


#if ($implementation)
public class ${Filename} extends GenericBean implements ${EditableInterface}
#elseif ($interface)
public interface ${Klass} extends GenericVO, MapSerializable<${Klass}>
#elseif ($editableInterface)
public interface ${Filename} extends ${Klass}
#end
{

#if ($interface)
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

#end##if ($interface)
##
##
##
#if ($implementation)
#foreach ($field in $fields)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
    ${association.Klass} _${association.Klass};
#else##if (!$association = $associationForField[$field])
    ${field.Type} ${field.LowerCamel}#if ($primaryKeys.contains($field)) = ${defaultId}#end;
#end##if ($association = $associationForField[$field])
#end##foreach ($field in $fields)

#foreach ($association in $toManyAssociations)
    QuerySet<${association.Klass}> _${association.Pluralized};
#end##foreach ($association in $toManyAssociations)

    private final static long serialVersionUID = ${serialVersionUID};

    public ${Filename}(){}

#set ($argCount = $fields.size() + $toManyAssociations.size())
    public ${Filename}(${constructorArgsDecl})
    {
#foreach ($field in $fields)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
        this._${association.Klass} = _${association.Klass};
#else##if (!$associationForField[$field])
        this.${field.LowerCamel} = _${field.LowerCamel};
#end##if ($associationForField[$field])
#end##foreach ($field in $fields)
#foreach ($association in $toManyAssociations)
        this._${association.Pluralized} = _${association.Pluralized};
#end##foreach ($association in $toManyAssociations)
    }

#end##if ($implementation)
#foreach ($field in $fields)
#if ($interface || $implementation)
#if ($field.Get)
    public ${field.Type} ${getter[$field]} () #if($implementation){
        return ${field.LowerCamel};
    }#else;#end


#end##if ($interface || $implementation)
#end##if ($field.Get)
#if ( (!$associationForField[$field] && $implementation) ||
      (!$editableInterface && $field.Set && !$primaryKeys.contains($field)) ||
      ($primaryKeys.contains($field) && $editableInterface && !$associationForField[$field]) )
    public void set${field.UpperCamel}(${field.Type} ${field.LowerCamel}) #if ($implementation){
        this.${field.LowerCamel} = ${field.LowerCamel};
        triggerObserver("${field.LowerAndUnderscores}");
    }#else;#end


#end##if ( generate_setter )
#end##foreach
#foreach ($association in $manyToOneAssociations)
#if ($interface || $implementation)
    public ${association.Klass} get${association.Klass}() #if ($implementation){
        return _${association.Klass};
    }#else;#end


#end##if ($interface || $implementation)
#if (!$primaryKeys.contains($association.ForeignKey) || $implementation || $editableInterface )
    public void set${association.Klass}(${association.Klass} obj) #if ($implementation) {
        _${association.Klass} = obj;
    }#else;#end


#end##if (!$primaryKeys.contains($association.ForeignKey) || $implementation || $editableInterface )
#end##foreach ($association in $manyToOneAssociations)
#foreach ($association in $toManyAssociations)
#if ($interface || $implementation)
    public QuerySet<${association.Klass}> get${association.Pluralized}() #if ($implementation) {
        return _${association.Pluralized};
    }#else;#end


#end##if ($interface || $implementation)
#if ($implementation || $editableInterface)
    public void set${association.Pluralized}(QuerySet<${association.Klass}> querySet) #if ($implementation) {
        this._${association.Pluralized} = querySet;
    }#else;#end


#end##if ($implementation || $editableInterface)
#end
#if ($implementation)
    @Override
    public void toMap(Map<String, Object> map) {
#foreach ($field in $fields)
#if ($field.SerializationAlias)
#set ($alias = $field.SerializationAlias)
#else##if_not_alias
#set ($alias = $field.LowerAndUnderscores)
#end##end_if_alias
#set ($association = false)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
        $field.Type $field.LowerCamel = (_${association.Klass} == null)? 0 : _${association.Klass}.get${association.ReferenceKey.UpperCamel}();
#end##if ($associationForField[$field])
#if ($primaryKeys.contains($field) || $association)
        if (${field.LowerCamel} != ${defaultId}) {
            map.put("${alias}", ${field.LowerCamel});
        }
#else##if_primary_key
        map.put("${alias}", ${field.LowerCamel});
#end##if_primary_key
#end##foreach
    }

    @Override
    public int hashCodeImpl() {
        int value = 1;
#foreach ($field in $fields)
#if (!$associationForField[$field])
#if ($field.Klass.equals("Boolean"))
        value += (${field.LowerCamel}) ? 1 : 0;
#elseif ($field.Klass.equals("Integer") || $field.Klass.equals("Long") || $field.Klass.equals("Double"))
        value +=(int) ${field.LowerCamel};
#else
        value *= (${field.LowerCamel} == null) ? 1 : ${field.LowerCamel}.hashCode();
#end##if(field.Klass.equals(*))
#end##if (!$associationForField[$field])
#end##foreach
        return value;
    }

    @Override
    public boolean equalsImpl(Object obj) {
        if (obj == null) {
            return false;
        }
        if ( !(obj instanceof ${Klass}) ) {
            return false;
        }
        ${Klass} other = ((${Klass}) obj);
#foreach ($field in $fields)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
#set ($otherAssoc = "other${association.Klass}")
        ${association.Klass} ${otherAssoc} = other.get${association.Klass}();
        if (_${association.Klass} == null){
            if (${otherAssoc} != null)
                return false;
        } else {
            if (${otherAssoc} == null)
                return false;
            if (_${association.Klass}.get${association.ReferenceKey.UpperCamel}() != ${otherAssoc}.get${association.ReferenceKey.UpperCamel}())
                return false;
        }
#else##if (!$associationForField[$field])
#if ($field.Klass.equals("Boolean") || $field.Klass.equals("Long")|| $field.Klass.equals("Integer") || $field.Klass.equals("Double"))
        if(${field.LowerCamel} != other.${getter[$field]}())
            return false;
#else
        if( ( ${field.LowerCamel}==null)? (other.${getter[$field]}() != null) :  !${field.LowerCamel}.equals(other.${getter[$field]}()) )
            return false;
#end##if(field.Klass.equals(*))
#end##if ($associationForField[$field])
#end##foreach
        return true;
    }

#end##if ($implementation)
}
