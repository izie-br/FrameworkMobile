package com.quantium.mobile.framework;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

public abstract class AndroidToSyncProvider extends ToSyncProvider {

	public AndroidToSyncProvider() {
		super();
	}

	public abstract SQLiteDatabase getDb();
	
	@Override
	public <T extends BaseGenericVO> boolean save(String idUser, DAO<T> dao,
			String id, long action) throws IOException {
		ContentValues cv = new ContentValues();
		cv.put(CLASSNAME.getName(), dao.getTable().getName());
		cv.put(ID.getName(), String.valueOf(id));
		cv.put(ACTION.getName(), action);
		cv.put(ID_USER.getName(), String.valueOf(idUser));
		try {
			Cursor cursor = getDb().query(
					TO_SYNC_TABLE.getName(),
					new String[] {},
					ID.getName().concat("=? AND ")
							.concat(ID_USER.getName().concat("=? AND "))
							.concat(ACTION.getName().concat("=?")),
					new String[] { String.valueOf(id), String.valueOf(idUser),
							String.valueOf(action) }, null, null, null);
			boolean exists = cursor.moveToNext();
			cursor.close();
			if (exists) {
				return true;
			}
			long row = getDb().insert(TO_SYNC_TABLE.getName(), null, cv);
			if (row == 0) {
				throw new IOException("No generated key was found from sqlite");
			}
		} catch (SQLiteConstraintException e) {
		}
		return true;
	}

	@Override
	public List<String> listIds(String idUser,
			DAO<? extends BaseGenericVO> dao, long action) {
		List<String> ids = new ArrayList<String>();
		Cursor c = getDb().query(TO_SYNC_TABLE.getName(), new String[] {}, String
				.format("%s=? AND %s=? AND %s=?", CLASSNAME.getName(),
						ACTION.getName(), ID_USER.getName()),
				new String[] { dao.getTable().getName(),
						String.valueOf(action), String.valueOf(idUser) }, null,
				null, null);
		while (c.moveToNext()) {
			ids.add(c.getString(c.getColumnIndexOrThrow(ID.getName())));
		}
		c.close();
		return ids;
	}

	@Override
	public boolean delete(String idUser, DAO<? extends BaseGenericVO> dao,
			String id, long action) {
		int rows = getDb().delete(TO_SYNC_TABLE.getName(), String.format(
				"%s=? AND %s=? AND %s=? AND %s=?", ID.getName(),
				CLASSNAME.getName(), ACTION.getName(), ID_USER.getName()),
				new String[] { String.valueOf(id), dao.getTable().getName(),
						String.valueOf(action), String.valueOf(idUser) });
		return rows == 1;
	}

}
