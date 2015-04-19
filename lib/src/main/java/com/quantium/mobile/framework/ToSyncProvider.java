package com.quantium.mobile.framework;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.validation.Constraint;

import java.io.IOException;
import java.util.List;

public abstract class ToSyncProvider {

    public static final long SAVE = 1;
    public static final long DELETE = 2;

    public static final Table TO_SYNC_TABLE = new Table("fmvv_to_sync");
    public static final Table.Column<String> ID = TO_SYNC_TABLE.addColumn(
            String.class, "id", Constraint.primaryKey());
    public static final Table.Column<String> CLASSNAME = TO_SYNC_TABLE
            .addColumn(String.class, "classname");
    public static final Table.Column<String> ID_USER = TO_SYNC_TABLE.addColumn(
            String.class, "id_user");
    public static final Table.Column<Long> ACTION = TO_SYNC_TABLE.addColumn(
            Long.class, "action");

    public abstract List<String> listIds(String idUser,
                                         String tableName, long action) throws IOException;

    public abstract <T extends BaseGenericVO> boolean save(String idUser,
                                                           String tableName, String id, long action) throws IOException;

    public abstract boolean delete(String idUser,
                                   String tableName, String id, long action)
            throws IOException;

}
