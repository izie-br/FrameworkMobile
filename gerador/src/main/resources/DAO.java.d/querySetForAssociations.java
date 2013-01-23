##
## one-to-many
##
#foreach ($association in $oneToManyAssociations)
    public QuerySet<${association.Klass}> querySetFor${association.KeyToAPluralized}(
        ${association.ReferenceKey.Type} ${association.ReferenceKey.LowerCamel}
    ) {
        return factory.getDaoFor(${association.Klass}.class).query(
            ${association.Klass}.${association.ForeignKey.UpperAndUnderscores}.eq(${association.ReferenceKey.LowerCamel}));
    }

#end
##
## many-to-many
##
#foreach ($association in $manyToManyAssociations)
#**#    public QuerySet<${association.Klass}> querySetFor${association.Pluralized}(
#**##if ($association.IsThisTableA)
#**#        ${association.ReferenceA.Type} ${association.ReferenceA.LowerCamel}
#**##else
#**#        ${association.ReferenceB.Type} ${association.ReferenceB.LowerCamel}
#**##end
#**#    ) {
#**#        return factory.getDaoFor(${association.Klass}.class)
#**##if ($association.IsThisTableA)
#**#            .query(
#**#                (${association.Klass}.${association.ReferenceB.UpperAndUnderscores}.eq(${Target}._${association.JoinTableUpper}_${association.KeyToB.UpperAndUnderscores}))
#**#                .and( ${Target}._${association.JoinTableUpper}_${association.KeyToA.UpperAndUnderscores}.eq(${association.ReferenceA.LowerCamel}) ));
#**##else
#**#            .query(
#**#                (${association.Klass}.${association.ReferenceA.UpperAndUnderscores}.eq(${Target}._${association.JoinTableUpper}_${association.KeyToA.UpperAndUnderscores}))
#**#                .and( ${Target}._${association.JoinTableUpper}_${association.KeyToB.UpperAndUnderscores}.eq(${association.ReferenceB.LowerCamel}) ));
#**##end
#**#    }
#**#
#end
