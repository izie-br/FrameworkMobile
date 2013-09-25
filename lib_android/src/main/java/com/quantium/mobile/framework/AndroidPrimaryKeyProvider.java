package com.quantium.mobile.framework;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.quantium.mobile.framework.query.Table;

public abstract class AndroidPrimaryKeyProvider extends PrimaryKeyProvider {

	public AndroidPrimaryKeyProvider() {
		super();
	}
	
	public abstract SQLiteDatabase getDb();

	@Override
	public String sequenceNextFor(Table table) throws IOException {
		ContentValues cv = new ContentValues();
		cv.put(CLASSNAME.getName(), table.getName());
		long id = getDb().insert(SYNC_TABLE.getName(), null, cv);
		if (id == 0) {
			throw new IOException("No generated key was found from sqlite");
		}
		return String.valueOf(id);
	}

	@Override
	public List<String> listIds(DAO<? extends BaseGenericVO> dao) {
		List<String> ids = new ArrayList<String>();
		Cursor c = getDb().query(SYNC_TABLE.getName(), new String[] {}, String
				.format("%s=?", CLASSNAME.getName()), new String[] { dao
				.getTable().getName() }, null, null, null);
		while (c.moveToNext()) {
			ids.add(c.getString(c.getColumnIndexOrThrow(ID.getName())));
		}
		c.close();
		return ids;
	}

	@Override
	public <T extends BaseGenericVO> Object getIdServerById(DAO<T> dao,
			Object id) throws IOException {
		String idServer = null;
		Cursor c = getDb().query(SYNC_TABLE.getName(), new String[] {},
				String.format("%s=? AND %s=?", CLASSNAME.getName(), ID.getName()),
				new String[] { dao.getTable().getName(), id.toString() }, null,
				null, null);
		while (c.moveToNext()) {
			idServer = c.getString(c.getColumnIndexOrThrow(ID.getName()));
		}
		c.close();
		return idServer;
	}

	@Override
	public <T extends BaseGenericVO> boolean delete(DAO<T> dao, String id)
			throws IOException {
		if (listIds(dao).contains(id)) {
			int rows = getDb().delete(SYNC_TABLE.getName(),
					String.format("%s=?", ID.getName()),
					new String[] { String.valueOf(id) });
			return rows == 1;
		}
		return false;
	}

	@Override
	public <T extends BaseGenericVO> void updateIdServer(DAO<T> dao,
			Object oldId, Object newPrimaryKey) {
		ContentValues values = new ContentValues();
		values.put(ID_SERVER.getName(), newPrimaryKey.toString());
		getDb().update(SYNC_TABLE.getName(), values,
				String.format("%s = ?", ID.getName()),
				new String[] { oldId.toString() });
	}

}
