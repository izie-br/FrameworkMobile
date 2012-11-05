#if ($oneToManyAssociations.size() > 0)
#set ($createProxy = true)
#end##($oneToManyAssociations.size() > 0)
#foreach ($field in $fields)
##
#if ( $field.Klass.equals("Date") )
#set ($haveDateField = true)
#break
#end##if
#end##foreach
##
package $package;

#if ($createProxy)
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
#end##if($createProxy)
import java.util.Map;
#if ( $haveDateField )
import java.util.Date;
#end##if ( $haveDateField )
import com.quantium.mobile.framework.MapSerializable;
import com.quantium.mobile.framework.DAOFactory;
import com.quantium.mobile.framework.LazyProxy;
#if ($oneToManyAssociations.size() > 0 || $manyToManyAssociations.size() > 0)
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.DAO;
#end
import com.quantium.mobile.framework.query.Table;
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
## Conferir se o campo eh uma chave estrangeira
##   Se sim, criar uma instancia da classe associada
##   Se nao, criar apenas o campo
#set ($fieldIsForeignKey = false)
#foreach ($association in $manyToOneAssociations)
#if ($association.ForeignKey.equals($field))
#set ($fieldIsForeignKey = true)
    ${association.Klass} _${association.Klass};
#end##if($association.ForeignKey.equals($field))
#end##($association in $manyToOneAssociations)
#if (!$fieldIsForeignKey)
    ${field.Type} ${field.LowerCamel}#if ($primaryKeys.contains($field)) = ${defaultId}#end;
#end##if (!$fieldIsForeignKey)
#end##foreach ($field in $fields)

    public DAOFactory _daofactory;
    private final static long serialVersionUID = ${serialVersionUID};

    public ${Klass}(){}

    public ${Klass}(
#foreach ($field in $fields)
#set ($fieldIndex = $foreach.index + 1)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
        ${association.Klass} _${association.Klass}#if ($fieldIndex != $fields.size()), #end

#else##if (!$associationForField[$field])
        ${field.Type} ${field.LowerCamel}#if ($fieldIndex != $fields.size()), #end

#end##if ($associationForField[$field])
#end##foreach ($field in $fields)
)
    {
#foreach ($field in $fields)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
        this._${association.Klass} = _${association.Klass};
#else##if (!$associationForField[$field])
        this.${field.LowerCamel} = ${field.LowerCamel};
#end##if ($associationForField[$field])
#end##foreach ($field in $fields)
    }

#foreach ($field in $fields)
#if ($field.Get)
    public ${field.Type}#if ($field.Klass.equals("Boolean")) is#else get#end${field.UpperCamel}(){
        return ${field.LowerCamel};
    }

#end
#if ($field.Set && !$primaryKeys.contains($field) )
    public void set${field.UpperCamel}(${field.Type} ${field.LowerCamel}){
        this.${field.LowerCamel} = ${field.LowerCamel};
        triggerObserver("${field.LowerAndUnderscores}");
    }

#end##($field.Set && !primaryKeys.contains($field) )
#end##foreach
#foreach ($association in $oneToManyAssociations)
    public QuerySet<${association.Klass}> get${association.Pluralized}(){
        if (this._daofactory == null)
            return null;
        return ((DAO<${association.Klass}>)_daofactory.getDaoFor(${association.Klass}.class)).query(
            ${association.Klass}.${association.ForeignKey.UpperAndUnderscores}.eq(${association.ReferenceKey.LowerCamel}));
    }

#end
#foreach ($association in $manyToOneAssociations)
    public ${association.Klass} get${association.Klass}(){
        return _${association.Klass};
    }

#if (!$primaryKeys.contains($association.ForeignKey))
    public void set${association.Klass}(${association.Klass} obj){
        _${association.Klass} = obj;
    }
#end##if (!$primaryKeys.contains($association.ForeignKey))

#end##foreach ($association in $manyToOneAssociations)
#foreach ($association in $manyToManyAssociations)
    public QuerySet<${association.Klass}> get${association.Pluralized}(){
        if (this._daofactory == null)
            return null;
        return ((DAO<${association.Klass}>)_daofactory.getDaoFor(${association.Klass}.class))
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
        if (other instanceof LazyProxy)
            ((LazyProxy)other).load();
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
        if(${field.LowerCamel} != other.${field.LowerCamel})
            return false;
#else
        if( ( ${field.LowerCamel}==null)? (other.${field.LowerCamel} != null) :  !${field.LowerCamel}.equals(other.${field.LowerCamel}) )
            return false;
#end##if(field.Klass.equals(*))
#end##if ($associationForField[$field])
#end##foreach
        return true;
    }
#if ($createProxy)

    @SuppressWarnings("serial")
    public static class Proxy extends ${Klass} implements LazyProxy {
        DAOFactory _daofactory;
        boolean _proxy_loaded;

#foreach ($field in $fields)
#if (!$primaryKeys.contains($field))
#if ($field.Get)
        public ${field.Type}#if ($field.Klass.equals("Boolean")) is#else get#end${field.UpperCamel}(){
            if (!_proxy_loaded)
                load();
            return super#if ($field.Klass.equals("Boolean")) .is#else .get#end${field.UpperCamel}();
        }

#end
#if ($field.Set)
        public void set${field.UpperCamel}(${field.Type} ${field.LowerCamel}){
            if (!_proxy_loaded)
                load();
            super.set${field.UpperCamel}(${field.LowerCamel});
        }

#end##if_is_Set
#end##(!$primaryKeys.contains($field))
#end##foreach
#foreach ($association in $manyToOneAssociations)
        public ${association.Klass} get${association.Klass}(){
            if (!_proxy_loaded)
                load();
            return super.get${association.Klass}();
        }

        public void set${association.Klass}(${association.Klass} assoc){
            if (!_proxy_loaded)
                load();
            super.set${association.Klass}(assoc);
        }

#end
        public void toMap(Map<String, Object> map) {
            if (!_proxy_loaded)
                load();
            super.toMap(map);
        }

        public int hashCodeImpl() {
            if (!_proxy_loaded)
                load();
            return super.hashCodeImpl();
        }

        public boolean equals(Object obj) {
            if (!_proxy_loaded)
                load();
            return super.equalsImpl(obj);
        }

        private void writeObject(ObjectOutputStream oos) throws IOException {
            if (!_proxy_loaded)
                load();
            oos.defaultWriteObject();
        }

        private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException
        {
            if (!_proxy_loaded)
                load();
            ois.defaultReadObject();
        }

        public void load(){
            $Klass temp = ((DAO<${Klass}>)_daofactory.getDaoFor(${Klass}.class)).query(
#foreach ($field in $primaryKeys)
              #if ($foreach.index != 0).and#else    #end (${Klass}.${field.UpperAndUnderscores}.eq(${field.LowerCamel}))
#end##foreach
            ).first();
            if (temp == null)
                throw new RuntimeException();
#foreach ($field in $fields)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
            this._${association.Klass} = temp._${association.Klass};
#elseif (!$primaryKeys.contains($field))##if ($associationForField[$field])
            this.${field.LowerCamel} = temp.${field.LowerCamel};
#end##if ($associationForField[$field])
#end##foreach
            _proxy_loaded = true;
        }
    }
#end##if($createProxy)

}
