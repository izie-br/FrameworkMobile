package com.quantium.mobile.framework;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.quantium.mobile.framework.query.Table;

public class AndroidPrimaryKeyProvider extends PrimaryKeyProvider {

	private SQLiteDatabase db;
	
	public AndroidPrimaryKeyProvider(SQLiteDatabase db) {
		super();
		this.db = db;
	}
	
	@Override
	public String sequenceNextFor(Table table) throws IOException {
		ContentValues cv = new ContentValues();
		cv.put(CLASSNAME.getName(), table.getName());
		long id = db.insert(SYNC_TABLE.getName(), null, cv);
		if (id == 0) {
			throw new IOException("No generated key was found from sqlite");
		}
		return String.valueOf(id);
	}

	@Override
	public List<String> listIds(DAO<? extends BaseGenericVO> dao) {
		List<String> ids = new ArrayList<String>();
		Cursor c = db.query(SYNC_TABLE.getName(), new String[] {}, String
				.format("%s=?", CLASSNAME.getName()), new String[] { dao
				.getTable().getName() }, null, null, null);
		while (c.moveToNext()) {
			ids.add(c.getString(c.getColumnIndexOrThrow(ID.getName())));
		}
		c.close();
		return ids;
	}

	@Override
	public boolean delete(DAO<? extends BaseGenericVO> dao, String id) {
		if (listIds(dao).contains(id)) {
			int rows = db.delete(SYNC_TABLE.getName(),
					String.format("%s=?", ID.getName()),
					new String[] { String.valueOf(id) });
			return rows == 1;
		}
		return false;
	}

}
