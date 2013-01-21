##
##TODO mandar este trecho abaixo para um helper
##
#set ($hasMutableAssociations = $manyToManyAssociations.size() > 0)
#foreach ($association in $oneToManyAssociations)
#**##if (!$association.ForeignKey.PrimaryKey)
#****##set ($hasMutableAssociations = true)
#****##break
#**##end
#end
##
##
    @Override
    public ToManyDAO with(${Target} obj){
#if (!$hasMutableAssociations)
         throw new UnsupportedOperationException();
#else
         return new ${Target}ToManyDAO(obj);
#end
    }
##
#if ( !($manyToManyAssociations.size() == 0) || !($oneToManyAssociations.size() == 0) )
#**#
#**#    private class ${Target}ToManyDAO implements ToManyDAO {
#**#
#**#        private $Target target;
#**#
#**#        private ${Target}ToManyDAO(${Target} target){
#**#            this.target = target;
#**#        }
#**#
#**#        @Override
#**#        public boolean add(Object obj) throws IOException{
#**##foreach ($association in $oneToManyAssociations)
#******##if (!$association.ForeignKey.PrimaryKey)
#******#            if (obj instanceof ${association.Klass}){
#******#                ${association.Klass} objCast = ((${association.Klass})obj);
#******#                objCast.set${association.KeyToA}(this.target);
#******#                return factory.getDaoFor(${association.Klass}.class).save(objCast);
#******#            }
#******##end
#**##end
#**###
#**##foreach ($association in $manyToManyAssociations)
#**#            if (obj instanceof ${association.Klass}){
#**#                ${association.Klass} objCast = ((${association.Klass})obj);
#**#                return add${association.Klass}To${Target}(objCast, target);
#**#            }
#**##end
#**#            throw new IllegalArgumentException(obj.getClass().getName());
#**#
#**#        }
#**#
#**#        @Override
#**#        public boolean remove(Object obj) throws IOException {
#**##foreach ($association in $oneToManyAssociations)
#******#            if (obj instanceof ${association.Klass}){
#******#                ${association.Klass} objCast = ((${association.Klass})obj);
#******##if ($association.Nullable)
#******#                objCast.set${association.KeyToA}(null);
#******#                return factory.getDaoFor(${association.Klass}.class).save(objCast);
#******##else
#******#                return factory.getDaoFor(${association.Klass}.class).delete(objCast);
#******##end
#******#            }
#**##end
#**###
#**##foreach ($association in $manyToManyAssociations)
#**#            if (obj instanceof ${association.Klass}){
#**#                ${association.Klass} objCast = ((${association.Klass})obj);
#**#                return remove${association.Klass}From${Target}(objCast, target);
#**#            }
#**##end
#**#            throw new IllegalArgumentException(obj.getClass().getName());
#**#        }
#**#    }
#end
