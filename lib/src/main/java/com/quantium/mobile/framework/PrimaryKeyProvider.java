package com.quantium.mobile.framework;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.validation.Constraint;

import java.io.IOException;
import java.util.List;

public abstract class PrimaryKeyProvider {

    public static final Table SYNC_TABLE = new Table("fmvv_sync");
    public static final Table.Column<String> ID = SYNC_TABLE.addColumn(
            String.class, "id", Constraint.primaryKey());
    public static final Table.Column<String> ID_SERVER = SYNC_TABLE.addColumn(
            String.class, "id_server");
    public static final Table.Column<String> CLASSNAME = SYNC_TABLE.addColumn(
            String.class, "classname");

    public abstract String sequenceNextFor(String tableName) throws IOException;

    public abstract <T extends BaseGenericVO> Object getIdServerById(String tableName, Object id) throws IOException;

    public <T extends BaseGenericVO> boolean generatePrimaryKey(String tableName, T obj)
            throws IOException {
        obj.setId(sequenceNextFor(tableName));
        return true;
    }

    public abstract <T extends BaseGenericVO> boolean delete(String tableName, String id)
            throws IOException;

    public abstract <T extends BaseGenericVO> void updateIdServer(String tableName, Object oldId, Object newPrimaryKey) throws IOException;

    public abstract List<String> listTables() throws IOException;

    public abstract List<String> listIds(String className)
            throws IOException;
}
