    private class ${Target}ToManyDAO implements ToManyDAO {

        private $Target target;

        private ${Target}ToManyDAO(${Target} target){
            this.target = target;
        }

        @Override
        public boolean add(Object obj) throws IOException{
#foreach ($association in $oneToManyAssociations)
#**##if (!$association.ForeignKey.PrimaryKey)
#**#            if (obj instanceof ${association.Klass}){
#**#                ${association.Klass} objCast = ((${association.Klass})obj);
#**#                objCast.set${Target}(this.target);
#**#                return factory.getDaoFor(${association.Klass}.class).save(objCast);
#**#            }
#**##end
#end
##
#foreach ($association in $manyToManyAssociations)
            if (obj instanceof ${association.Klass}){
                ${association.Klass} objCast = ((${association.Klass})obj);
                return add${association.Klass}To${Target}(objCast, target);
            }
#end
            throw new IllegalArgumentException(obj.getClass().getName());

        }

        @Override
        public boolean remove(Object obj) throws IOException {
#foreach ($association in $oneToManyAssociations)
#**#            if (obj instanceof ${association.Klass}){
#**#                ${association.Klass} objCast = ((${association.Klass})obj);
#**##if ($association.Nullable)
#**#                objCast.set${Target}(null);
#**#                return factory.getDaoFor(${association.Klass}.class).save(objCast);
#**##else
#**#                return factory.getDaoFor(${association.Klass}.class).delete(objCast);
#**##end
#**#            }
#end
##
#foreach ($association in $manyToManyAssociations)
            if (obj instanceof ${association.Klass}){
                ${association.Klass} objCast = ((${association.Klass})obj);
                return remove${association.Klass}From${Target}(objCast, target);
            }
#end
            throw new IllegalArgumentException(obj.getClass().getName());
        }
    }
