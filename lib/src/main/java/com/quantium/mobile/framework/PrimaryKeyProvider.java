package com.quantium.mobile.framework;

import java.io.IOException;
import java.util.List;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.validation.Constraint;

public abstract class PrimaryKeyProvider {

	public static final Table SYNC_TABLE = new Table("fmvv_sync");
	public static final Table.Column<String> ID = SYNC_TABLE.addColumn(
			String.class, "id", Constraint.primaryKey());
	public static final Table.Column<String> ID_SERVER = SYNC_TABLE.addColumn(
			String.class, "id_server");
	public static final Table.Column<String> CLASSNAME = SYNC_TABLE.addColumn(
			String.class, "classname");

	public abstract String sequenceNextFor(Table table) throws IOException;

	public abstract List<String> listIds(DAO<? extends BaseGenericVO> dao)
			throws IOException;

	public <T extends BaseGenericVO> boolean generatePrimaryKey(DAO<T> dao, T obj)
			throws IOException {
		if (dao == null)
			throw new IllegalArgumentException("null");
		obj.setId(sequenceNextFor(dao.getTable()));
		return true;
	}

	public abstract boolean delete(DAO<? extends BaseGenericVO> dao, String id)
			throws IOException;

}
