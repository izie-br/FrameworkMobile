package com.quantium.mobile.framework.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.query.BaseQuerySet;
import com.quantium.mobile.framework.query.Q;

public abstract class JdbcQuerySet<T> extends BaseQuerySet<T> {

	protected abstract T cursorToObject(ResultSet cursor);

	protected abstract Connection getConnection();

	@Override
	protected String nullsFirstOrderingClause() {
		return "NULLS FIRST";
	}

	@Override
	public List<T> all(){
		List<T> all = new ArrayList<T>();
		ResultSet cursor = null;
		try{
			cursor = getCursor(getColumns ());
			while(cursor.next())
				all.add(cursorToObject(cursor));
		} catch (java.sql.SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (cursor != null)
					cursor.close();
			} catch (SQLException e) {
				LogPadrao.e(e);
			}
		}
		return all;
	}

	@Override
	public T first(){
		ResultSet cursor = null;
		try{
			cursor = getCursor(getColumns ());
			if(cursor.next())
				return cursorToObject(cursor);
		} catch (java.sql.SQLException e) {
			LogPadrao.e(e);
			return null;
		} 
		finally{
			try {
				if (cursor != null)
					cursor.close();
			} catch (SQLException e) {
				LogPadrao.e(e);
			}
		}
		return null;
	}

	@Override
	public long count() {
		ResultSet cursor = null;
		try{
			cursor = getCursor(Arrays.asList ("count(*)"));
			if(cursor.next())
				return cursor.getLong (1);
		} catch (java.sql.SQLException e) {
			LogPadrao.e(e);
			return -1;
		} 
		finally{
			try {
				if (cursor != null)
					cursor.close();
			} catch (SQLException e) {
				LogPadrao.e(e);
			}
		}
		throw new RuntimeException();
	}

	/**
	 * Retorna o cursor, para uso em cursor adapter, etc.
	 * @return cursor
	 */
	public ResultSet getCursor(List<?> selection) throws java.sql.SQLException {
		if (limit <= 0)
			limit = -1;
		String limitStr =
				(this.offset > 0) ?
						String.format("LIMIT %d OFFSET %d", limit, offset) :
				(this.limit > 0) ?
						String.format("LIMIT %d", limit):
				//(limit == 0 && offset == 0) ?
						"";

		if (this.q == null) {
			this.q = new Q (getTable());
		}

		ArrayList<Object> listArg = new ArrayList<Object>();
		String qstr = new QH2DialectProvider(q).select(selection, listArg);

		String orderByLocal = orderBy == null  ? "" : (" ORDER BY " + orderBy + " ");
		Connection conn = getConnection();
		PreparedStatement stm = conn.prepareStatement(
				qstr +
				orderByLocal +
				" " +limitStr
		);
		for (int i = 0; i< listArg.size(); i++){
			Object arg = listArg.get(i);
			int index = i+1;

			if (arg instanceof Long || arg instanceof Integer ||
			    arg instanceof Short)
			{
				stm.setLong(index, ((Number)arg).longValue());
			} else if (arg instanceof String ){
				stm.setString(index, arg.toString());
			} else if (arg instanceof Date) {
				stm.setTimestamp(index,
						new java.sql.Timestamp(((Date)arg).getTime()) );
			} else if (arg instanceof Boolean) {
				stm.setBoolean(index, (Boolean)arg );
			} else if (arg instanceof Double || arg instanceof Float){
				stm.setDouble(index, ((Number)arg).doubleValue() );
			} else {
				throw new RuntimeException(
						"classe " + arg.getClass().getSimpleName() +
						" nao mapeada");
			}
		}
		ResultSet rs = stm.executeQuery();
		return rs;
	}

}
