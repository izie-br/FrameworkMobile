    @Override
    public void updatePrimaryKey($Target target, Object newPrimaryKey)
            throws IOException
    {
        if (target == null)
            throw new IllegalArgumentException(
                "Target is NULL");
        if (!(target instanceof ${EditableInterface}) )
            throw new IllegalArgumentException(
                "Target is not editable");
        if (newPrimaryKey == null)
            throw new IllegalArgumentException(
                "PrimarKey is NULL");
        if (!(newPrimaryKey instanceof ${primaryKey.Klass}) )
            throw new IllegalArgumentException(
                "PrimarKey is not " + newPrimaryKey.getClass().getName() );
        if (target.getId() == null)
        	throw new IllegalArgumentException(
                "Target was not persisted");
        if (newPrimaryKey.equals(target.getId()))
        	throw new IllegalArgumentException(
                "Target and new primaryKey are the same");
        ${EditableInterface} editableTarget = (${EditableInterface})target;
        ${primaryKey.Klass} newPk = (${primaryKey.Klass})newPrimaryKey;
        ${primaryKey.Klass} oldPk = editableTarget.${getter[$primaryKey]}();
        long newIdCount = query(${Target}.${primaryKey.UpperAndUnderscores}.eq(newPk)).count();
        if (newIdCount > 0)
            throw new IOException("Id already exists");
        // Armazenando os querySets antigos, antes do reINSERT, que os atualiza
#foreach ($association in $oneToManyAssociations)
        QuerySet<${association.Klass}> ${association.KeyToAPluralized}_ =
                editableTarget.get${association.KeyToAPluralized}();
#end
#foreach ($association in $manyToManyAssociations)
        QuerySet<${association.Klass}> ${association.Pluralized}_ =
                editableTarget.get${association.Pluralized}();
#end
        this.factory.removeFromCache(${Target}.class, new Serializable[]{oldPk});
        editableTarget.setId(newPk);
#foreach ($association in $oneToManyAssociations)
        editableTarget.set${association.KeyToAPluralized}(null);
#end
#foreach ($association in $manyToManyAssociations)
        editableTarget.set${association.Pluralized}(null);
#end
        if (!save(editableTarget, Save.INSERT_IF_NOT_EXISTS))
            throw new IOException("save could not be performed, check logs");
#foreach ($association in $oneToManyAssociations)
        for (${association.Klass} item : ${association.KeyToAPluralized}_.all()) {
            item.set${association.KeyToA}(editableTarget);
            item.setLastModified(new java.util.Date());
            if (!this.factory
            .getModelFacade()
            .save(item)){
                throw new IOException("save could not be performed, check logs");
            }
        }
#end
#foreach ($association in $manyToManyAssociations)
        for (${association.Klass} item : ${association.Pluralized}_.all()) {
            with(editableTarget).add(item);
        }
#end
        ${Target} oldItem = get(oldPk);
        if (!delete(oldItem))
            throw new IOException(
                "error upon deleting old record under updateId process, check logs");
    }
