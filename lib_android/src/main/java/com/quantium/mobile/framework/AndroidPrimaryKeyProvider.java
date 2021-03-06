package com.quantium.mobile.framework;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AndroidPrimaryKeyProvider extends PrimaryKeyProvider {

    public AndroidPrimaryKeyProvider() {
        super();
    }

    public abstract SQLiteDatabase getDb();

    @Override
    public String sequenceNextFor(String tableName) throws IOException {
        ContentValues cv = new ContentValues();
        cv.put(CLASSNAME.getName(), tableName);
        long id = getDb().insert(SYNC_TABLE.getName(), null, cv);
        if (id == 0) {
            throw new IOException("No generated key was found from sqlite");
        }
        return String.valueOf(id);
    }

    @Override
    public List<String> listIds(String className) {
        List<String> ids = new ArrayList<String>();
        Cursor c = getDb().query(SYNC_TABLE.getName(), new String[]{}, String
                .format("%s=?", CLASSNAME.getName()), new String[]{className}, null, null, null);
        while (c.moveToNext()) {
            ids.add(c.getString(c.getColumnIndexOrThrow(ID.getName())));
        }
        c.close();
        return ids;
    }

    @Override
    public <T extends BaseGenericVO> Object getIdServerById(String tableName,
                                                            Object id) throws IOException {
        String idServer = null;
        Cursor c = getDb().query(SYNC_TABLE.getName(), new String[]{},
                String.format("%s=? AND %s=?", CLASSNAME.getName(), ID.getName()),
                new String[]{tableName, id.toString()}, null,
                null, null);
        while (c.moveToNext()) {
            idServer = c.getString(c.getColumnIndexOrThrow(ID_SERVER.getName()));
        }
        c.close();
        return idServer;
    }

    @Override
    public <T extends BaseGenericVO> boolean delete(String tableName, String id)
            throws IOException {
        if (listIds(tableName).contains(id)) {
            int rows = getDb().delete(SYNC_TABLE.getName(),
                    String.format("%s=?", ID.getName()),
                    new String[]{String.valueOf(id)});
            return rows == 1;
        }
        return false;
    }

    @Override
    public <T extends BaseGenericVO> void updateIdServer(String tableName,
                                                         Object oldId, Object newPrimaryKey) {
        ContentValues values = new ContentValues();
        values.put(ID_SERVER.getName(), newPrimaryKey.toString());
        getDb().update(SYNC_TABLE.getName(), values,
                String.format("%s = ?", ID.getName()),
                new String[]{oldId.toString()});
    }


    @Override
    public List<String> listTables() throws IOException {
        List<String> ids = new ArrayList<String>();
        Cursor c = getDb().query(SYNC_TABLE.getName(), new String[]{}, null, null, null, null, null);
        while (c.moveToNext()) {
            ids.add(c.getString(c.getColumnIndexOrThrow(CLASSNAME.getName())));
        }
        c.close();
        return ids;
    }


}
