package com.quantium.mobile.framework.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.query.Table.Column;
import com.quantium.mobile.framework.utils.StringUtil;

public abstract class JdbcQuerySet<T> implements QuerySet<T>, Cloneable {
	private Q q;
	private String orderBy;

//	private String groupBy;
//	private String having;

	private int limit;
	private int offset;


	protected abstract T cursorToObject(ResultSet cursor);

	protected abstract Table.Column<?>[] getColunas();

	protected abstract Connection getConnection();

	public QuerySet<T> orderBy(Table.Column<?> column, Q.OrderByAsc asc){
		JdbcQuerySet<T> obj = this.clone();
		if (column != null) {
			// O ordenamento usa "NULLS FIRST" para ter comportamento
			//   compativel com SQLite
			obj.orderBy =
					( (this.orderBy == null) ? "" : this.orderBy + ",") +
					" " + column.getTable().getName() +
					"." + column.getName() +
					" " + asc.toString() +
					" NULLS FIRST";
		}
		return obj;
	}

	public QuerySet<T> orderBy(Table.Column<?> column){
		return orderBy(column, Q.ASC);
	}

	public QuerySet<T> limit (int limit){
		JdbcQuerySet<T> obj = this.clone();
		if (limit > 0)
			obj.limit = limit;
		return obj;
	}

	public QuerySet<T> offset (int offset){
		JdbcQuerySet<T> obj = this.clone();
		if (offset > 0)
			obj.offset = offset;
		return obj;
	}

	public QuerySet<T> filter(Q q){
		if (q==null)
			return this;
		JdbcQuerySet<T> obj = this.clone();
		if (this.q==null)
			obj.q = q;
		else
			obj.q = this.q.and (q);
		return obj;
	}


	public List<T> all(){
		List<T> all = new ArrayList<T>();
		ResultSet cursor = null;
		try{
			cursor = getCursor();
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

	public T first(){
		if(limit<0)
			limit = 1;
		ResultSet cursor = null;
		try{
			cursor = getCursor();
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

	/**
	 * Retorna o cursor, para uso em cursor adapter, etc.
	 * @return cursor
	 */
	public ResultSet getCursor() throws java.sql.SQLException {
		String limitStr =
				(limit>0 && offset>0) ?
						String.format("LIMIT %d OFFSET %d", offset,limit):
				(limit>0) ?
						String.format("LIMIT %d", limit):
				(offset>0) ?
						String.format("OFFSET %d,", offset):
				// limit <= 0 && offset <= 0
						"";

		if (this.q == null) {
			this.q = new Q (getTable());
		}

		ArrayList<Object> listArg = new ArrayList<Object>();
		Column<?>[] colunas = getColunas();
		String qstr = new QH2DialectProvider(q).select(colunas, listArg);

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

			if (arg == null) {
				stm.setNull(index, typeFor(colunas[i].getKlass()));
			} else if (arg instanceof Long || arg instanceof Integer ||
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

	private static int typeFor(Class<?> klass){
		if (klass == Long.class || klass == Integer.class ||
		    klass ==   Short.class) {
			return java.sql.Types.INTEGER;
		} else if (klass == String.class){
			return java.sql.Types.VARCHAR;
		} else if (klass == Date.class) {
			return java.sql.Types.TIMESTAMP;
		} else if (klass == Boolean.class) {
			return java.sql.Types.BOOLEAN;
		} else if (klass == Double.class || klass == Float.class ){
			return java.sql.Types.DOUBLE;
		} else {
			throw new RuntimeException(
					"classe " +klass.getSimpleName() + " nao mapeada");
		}
	}

	@Override
	protected JdbcQuerySet<T> clone() {
		try {
			@SuppressWarnings("unchecked")
			JdbcQuerySet<T> obj = (JdbcQuerySet<T>)super.clone();
			return obj;
		} catch (CloneNotSupportedException e) {
			// Impossivel a menos que a excecao seja explicitamente criada
			//    ou que esta classe deixe de implementar Cloenable
			throw new RuntimeException(StringUtil.getStackTrace(e));
		}
	}

}
