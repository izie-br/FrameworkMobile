#foreach ($field in $fields)
#if ( $field.Klass.equals("Date") )
#set ($haveDateField = true)
#if ($primaryKeys.contains($field))
#set ($hasDatePK = true)
#end##if ($primaryKeys.contains($field))
#end##if ( $field.Klass.equals("Date") )
#end##foreach ($field in $fields)
##
##
##
##
#import "JreEmulation.h"
##if ( $haveDateField && (!$editableInterface || $hasDatePK) )
#import "java/util/Date.h"
##end##if ( $haveDateField && (!$editableInterface || $hasDatePK) )
##if ($oneToManyAssociations.size() > 0 || $manyToManyAssociations.size() > 0)
@class ComQuantiumMobileFrameworkQueryQuerySet;
##end##if ($oneToManyAssociations.size() > 0 || $manyToManyAssociations.size() > 0)
##if ($implementation)
#import "GenericBean.h"
#import "${Klass}.h"
@protocol JavaUtilMap;
##elseif ($interface)
@class ComQuantiumMobileFrameworkQueryTable;
@class ComQuantiumMobileFrameworkQueryTable_Column;
#import "MapSerializable.h"
#import "GenericVO.h"
##end ($implementation)


#if ($Protocol)
@protocol ${package}${Klass} < GenericVO >
#elseif ($ProtocolImpl)
@implementation ${package}${Filename}
#elseif ($EditableProtocol)
@protocol ${package}${Filename} <${package}${Klass}> {
#elseif ($Implementation)
@implementation ${package}${Filename}
#elseif ($Interface)
@interface ${package}${Filename} : ${basePackage}GenericBean
  < ComQuantiumMobileFrameworkMapSerializable, ${package}${EditableInterfaceName}>
{
  @public
#foreach ($field in $fields)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
    id<${package}${association.Klass}> _${association.Klass}_;
#else##if !($associationForField[$field])
#if ( $field.Type.equals("String") || $field.Type.equals("Date") )
    ${Type[$field]} *${field.LowerCamel}_;
#else##if !( $field.Type.equals("String") || $field.Type.equals("Date") )
    ${Type[$field]} ${field.LowerCamel}_;
#end##if ( $field.Type.equals("String") || $field.Type.equals("Date") )
#end##if ($associationForField[$field])
#end##foreach ($field in $fields)
#foreach ($association in $toManyAssociations)
    id<ComQuantiumMobileFrameworkQuerySet> _${association.Pluralized};
#end##foreach ($association in $toManyAssociations)
}
#foreach ($field in $fields)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
@property (nonatomic, strong) id<${package}${association.Klass}> _${association.Klass};
#else##if !($associationForField[$field])
#if ( $field.Type.equals("String") || $field.Type.equals("Date") )
@property (nonatomic, copy) ${Type[$field]} *${field.LowerCamel};
#else##if !( $field.Type.equals("String") || $field.Type.equals("Date") )
@property (nonatomic, assign) ${Type[$field]} ${field.LowerCamel};
#end##if ( $field.Type.equals("String") || $field.Type.equals("Date") )
#end##if ($associationForField[$field])
#end##foreach ($field in $fields)
#end##if ($Protocol)
#if ($Interface || $Implementation)

- (id<JavaUtilMap>)toMapWithJavaUtilMap:(id<JavaUtilMap>)map #if ($Interface);#elseif ($Implementation)
{
    
}
#end##if ($Interface)

- (id)init #if ($Interface);
#elseif ($Implementation)
{
    if ((self = [super init])) {
    }
    return self;
}

#end##if ($Interface)
##
#end##if ($interface)
#if ($Implementation)
#foreach ($field in $fields)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
@dynamic _${association.Klass};
#else##if (!$association = $associationForField[$field])
@dynamic ${field.LowerCamel};
#end##if ($association = $associationForField[$field])
#end##foreach ($field in $fields)
##
###foreach ($association in $toManyAssociations)
##@dynamic _${association.Pluralized};
###end##foreach ($association in $toManyAssociations)
##
#end##if ($implementation)
#if ($Implementation || $Interface)
#set ($argCount = $fields.size() + $toManyAssociations.size())
#foreach ($field in $fields)
#set ($fieldIndex = $foreach.index + 1)
#if ($fieldIndex == 1)
- (id)initWith#else##if!($fieldIndex == 1)  Deve-se terminar com "end" ou "else" antes da
          with#end###if ($fieldIndex == 1)  quebra de linha, para concatenar com a proxima
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
##
${package}${association.Klass}: (id<${package}${association.Klass}>) new${association.Klass}
##
#else##if (!$associationForField[$field])
#if ( $field.Type.equals("String") || $field.Type.equals("Date") )
##
${TypeName[$field]}: (${Type[$field]} *) new${field.UpperCamel}
##
#else##if !( $field.Type.equals("String") || $field.Type.equals("Date") )
##
${TypeName[$field]}: (${Type[$field]}) new${field.UpperCamel}
##
#end##if ( $field.Type.equals("String") || $field.Type.equals("Date") )
#end##if ($associationForField[$field])
#end##foreach ($field in $fields)
#foreach ($association in $toManyAssociations)
#set ($fieldIndex = $fields.size() + $foreach.index + 1)
#if ($fieldIndex == 1)
- (id)initWith#else##if!($fieldIndex == 1)  Deve-se terminar com "end" ou "else" antes da
          with#end###if ($fieldIndex == 1)  quebra de linha, para concatenar com a proxima
#set ($fieldIndex = $fields.size() + $foreach.index + 1)
ComQuantiumMobileFrameworkQuerySet: (id<ComQuantiumMobileFrameworkQuerySet>) new${association.Pluralized}
#end##foreach ($association in $toManyAssociations)
#if ($Interface);#elseif ($Implementation)
{
#foreach ($field in $fields)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
    [[self _${association.Klass}] = new${association.Klass}];
#else##if (!$associationForField[$field])
    [[self ${field.LowerCamel}] = new${field.UpperCamel}];
#end##if ($associationForField[$field])
#end##foreach ($field in $fields)
#foreach ($association in $toManyAssociations)
    [[self _${association.Pluralized}] = new${association.Pluralized}];
#end##foreach ($association in $toManyAssociations)
}
#end##if ($Interface)

#end##if ($associationForField[$field])
#foreach ($field in $fields)
#if ($interface || $implementation)
#if ($field.Get)


#end##if ($interface || $implementation)
#end##if ($field.Get)
#if ( (!$associationForField[$field] && $implementation) ||
      (!$editableInterface && $field.Set && !$primaryKeys.contains($field)) ||
      ($primaryKeys.contains($field) && $editableInterface && !$associationForField[$field]) )


#end##if ( generate_setter )
#end##foreach
#foreach ($association in $manyToOneAssociations)
#if ($Protocol || $Interface || $Implementation)
- (id<${package}${association.Klass}>) get${association.Klass} #if ($implementation){
        return [self _${association.Klass}];
    }#else;#end


#end##if ($Protocol || $Interface || $Implementation)
#if ( (!$primaryKeys.contains($association.ForeignKey) && $Protocol) ||
      $Interface || $Implementation || $EditableProtocol )
- (void) set${association.Klass}With${package}${association.Klass}: (id<${package}${association.Klass}>)new${association.Klass} #if ($Implementation) {
        [[self _${association.Klass}] = new${association.Klass}];
    }#else;#end


#end##if ( generate_many-to-one-setter)
#end##foreach ($association in $manyToOneAssociations)
#foreach ($association in $toManyAssociations)
#if ($Protocol || $Interface || $Implementation)
- (id<ComQuantiumMobileFrameworkQueryQuerySet>) get${association.Pluralized} #if ($Implementation) {
        return [self _${association.Pluralized}];
}#else;#end


#end##if ($interface || $implementation)
#if ($Interface || $Implementation || $EditableInterface)
- (void) set${association.Pluralized}WithComQuantiumMobileFrameworkQueryQuerySet: (id<ComQuantiumMobileFrameworkQueryQuerySet>) querySet #if ($Implementation){
    self _${association.Pluralized} = querySet;
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
##
##
## Implementacao da classe com campos estaticos de tabela e colunas
## A interface encotra-se abaixo
##
##
#if ($ProtocolImpl)
  static ComQuantiumMobileFrameworkQueryTable *_TABLE_;
#foreach ($field in $fields)
  static ComQuantiumMobileFrameworkQueryTable_Column *${field.UpperAndUnderscores}_;
#end##foreach ($field in $fields)

+ (ComQuantiumMobileFrameworkQueryTable *)_TABLE {
    return _TABLE;
}
#foreach ($field in $fields)

+ (ComQuantiumMobileFrameworkQueryTable_Column *)${field.UpperAndUnderscores}{
    return ${field.UpperAndUnderscores}_;
}
#end

+ (void)initialize {
  _TABLE_ = [[ComQuantiumMobileFrameworkQueryTable alloc] initWithNSString:@"${table}"];
#foreach ($field in $fields)
  ${field.UpperAndUnderscores}_ =
    ((ComQuantiumMobileFrameworkQueryTable_Column *) [
      _TABLE_ addColumnWithIOSClass: [
        IOSClass classWithClass:[JavaLangLong class]]
      withNSString:@"${field.LowerAndUnderscores}"]);
#end##foreach ($field in $fields)
}
#end##if ($ProtocolImpl)
@end
##
##
## INterface da classe com campos estaticos de tabela e colunas
##
##
#if ($Protocol)

@interface ${package}${Filename} : NSObject {
}
+ (ComQuantiumMobileFrameworkQueryTable *)_TABLE;
#foreach ($field in $fields)
+ (ComQuantiumMobileFrameworkQueryTable_Column *)${field.UpperAndUnderscores};
#end##foreach ($field in $fields)
@end
#end##if ($Protocol)
