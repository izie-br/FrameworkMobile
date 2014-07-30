package com.quantium.mobile.framework.libjdbctest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.quantium.mobile.framework.BaseGenericVO;
import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.ToSyncProvider;
import com.quantium.mobile.framework.libjdbctest.db.DB;
import com.quantium.mobile.framework.libjdbctest.db.MyJdbcDAOFactory;

public class JdbcToSyncProvider extends ToSyncProvider {

	@Override
	public List<String> listIds(String idUser,
                                String tableName, long action) throws IOException {
		List<String> ids = new ArrayList<String>();
		Connection conn = null;
		try {
			conn = DB.getConnection();
			String sql = "SELECT * FROM " + TO_SYNC_TABLE.getName() + " WHERE "
					+ CLASSNAME.getName() + "=? and " + ACTION.getName()
					+ "=? and " + ID_USER.getName() + "=?";
			PreparedStatement stm = conn.prepareStatement(sql);
			stm.setString(1, tableName);
			stm.setLong(2, action);
			stm.setString(3, idUser);
			ResultSet rs = stm.executeQuery();
			while (rs.next()) {
				ids.add(rs.getString(1));
			}
		} catch (java.sql.SQLException e) {
			throw new IOException(e);
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (java.sql.SQLException e) {
				throw new IOException(e);
			}
		}
		return ids;
	}

	@Override
	public <T extends BaseGenericVO> boolean save(String idUser, String tableName,
			String id, long action) throws IOException {
		Connection conn = null;
		try {
			conn = DB.getConnection();
			String sql = "INSERT INTO " + TO_SYNC_TABLE.getName() + " ("
					+ CLASSNAME.getName() + "," + ID.getName() + ","
					+ ACTION.getName() + "," + ID_USER.getName()
					+ ") VALUES (?, ?, ?, ?)";
			PreparedStatement stm = conn.prepareStatement(sql);
			stm.setString(1, tableName);
			stm.setString(2, id);
			stm.setLong(3, action);
			stm.setString(4, idUser);
			int value = stm.executeUpdate();
			if (value != 1)
				throw new IOException("Insert returned " + value);
			if(stm.getUpdateCount() <= 0){
				throw new IOException("No sync inserted");
			}
			return true;
		} catch (java.sql.SQLException e) {
			throw new IOException(e);
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (java.sql.SQLException e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	public boolean delete(String idUser, String tableName,
			String id, long action) throws IOException {
		Connection conn = null;
		try {
			conn = DB.getConnection();
			String sql = "DELETE FROM " + TO_SYNC_TABLE.getName() + " WHERE "
					+ ID.getName() + "=? and " + ACTION.getName() + "=? and "
					+ ID_USER.getName() + "=?";
			PreparedStatement stm = conn.prepareStatement(sql);
			stm.setString(1, id);
			stm.setLong(2, action);
			stm.setString(3, idUser);
			stm.execute();
		} catch (java.sql.SQLException e) {
			throw new IOException(e);
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (java.sql.SQLException e) {
				throw new IOException(e);
			}
		}
		return true;
	}

}
