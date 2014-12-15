package com.quantium.mobile.framework.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.query.BaseQuerySet;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.utils.ValueParser;

public abstract class JdbcQuerySet<T> extends BaseQuerySet<T> {

	public JdbcQuerySet(ValueParser parser) {
		super(parser);
	}

	protected abstract T cursorToObject(ResultSet cursor);

	protected abstract Connection getConnection();

    @Override
    public List<T> all(){
        List<T> all = new ArrayList<T>();
        ResultSet cursor = null;
        try{
            cursor = getCursor(getColumns (), false);
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
    public Set<T> allUnique(){
        Set<T> all = new HashSet<T>();
        ResultSet cursor = null;
        try{
            cursor = getCursor(getColumns (), true);
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
			cursor = getCursor(getColumns (), true);
			if(cursor.next())
				return cursorToObject(cursor);
		} catch (java.sql.SQLException e) {
			throw new RuntimeException(e);
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
			cursor = getCursor(Arrays.asList ("count(*)"), false);
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
    public ResultSet getCursor(List<?> selection, boolean distinct) throws java.sql.SQLException {
        if (this.q == null) {
            this.q = new Q (getTable());
        }

        ArrayList<Object> listArg = new ArrayList<Object>();
        String qstr = new QH2DialectProvider(q, parser)
                .limit(this.limit)
                .offset(this.offset)
                .orderBy(this.orderClauses)
                .select(selection, listArg, distinct);

        Connection conn = getConnection();
        PreparedStatement stm = conn.prepareStatement(qstr);
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

    @Override
    public List<T> groupBy(Q.GroupByClause groupByClause, Table.Column<?>... selection) {
        List<T> all = new ArrayList<T>();
        ResultSet cursor = null;
        try{
            cursor = getCursor(Arrays.asList(selection), groupByClause);
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

    private ResultSet getCursor(List<Table.Column<?>> selection, Q.GroupByClause groupByClause) throws java.sql.SQLException {
        if (this.q == null) {
            this.q = new Q (getTable());
        }
        List<Object>  list = new ArrayList<Object>();
        list.addAll(selection);
        list.add(groupByClause.getFunction().getName().concat("(").concat(groupByClause.getColumn().getName()).concat(")").concat(" as ").concat(groupByClause.getColumn().getName()));
        ArrayList<Object> listArg = new ArrayList<Object>();
        String qstr = new QH2DialectProvider(q, parser)
                .limit(this.limit)
                .offset(this.offset)
                .orderBy(this.orderClauses)
                .groupBy(selection)
                .select(list, listArg, false);
        Connection conn = getConnection();
        PreparedStatement stm = conn.prepareStatement(qstr);
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

    @Override
    public <U> Set<U> selectDistinct(Table.Column<U> column) {
        try{
            ResultSet resultSet = getCursor(Arrays.asList(String.format("distinct(%s)", column.getTable().getName().concat(".").concat(column.getName()))), false);
            Set<U> set = new HashSet<U>(resultSet.getFetchSize());
            while (resultSet.next()){
                if (column.getKlass().isAssignableFrom(String.class)) {
                    set.add((U) resultSet.getString(1));
                } else if (column.getKlass().isAssignableFrom(Double.class)) {
                    set.add((U) new Double(resultSet.getDouble(1)));
                } else if (column.getKlass().isAssignableFrom(Long.class)) {
                    set.add((U) new Long(resultSet.getLong(1)));
                } else if (column.getKlass().isAssignableFrom(Boolean.class)) {
                    set.add((U) new Boolean(resultSet.getBoolean(1)));
                }
            }
            resultSet.close();
            return set;
        } catch (java.sql.SQLException e) {
            LogPadrao.e(e);
            throw new RuntimeException(e);
        }
    }
}
