package com.quantium.mobile.framework.libjdbctest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.quantium.mobile.framework.BaseGenericVO;
import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.PrimaryKeyProvider;
import com.quantium.mobile.framework.libjdbctest.db.DB;
import com.quantium.mobile.framework.libjdbctest.db.MyJdbcDAOFactory;
import com.quantium.mobile.framework.query.Table;

public class JdbcPrimaryKeyProvider extends PrimaryKeyProvider {

	@Override
	public String sequenceNextFor(Table table) throws IOException {
		String tableName = table.getName();
		Connection conn = null;
		try {
			conn = DB.getConnection();
			String sql = "INSERT INTO " + SYNC_TABLE.getName() + " ("
					+ CLASSNAME.getName() + ") VALUES (?)";
			PreparedStatement stm = conn.prepareStatement(sql);
			stm.setString(1, tableName);
			int value = stm.executeUpdate();
			if (value != 1)
				throw new IOException("Insert returned " + value);
			ResultSet rs = stm.getGeneratedKeys();
			Long l = null;
			if (rs.next()) {
				l = rs.getLong(1);
				if (l == null)
					throw new IOException(
							"No generated key was found in ResultSet");
			} else {
				throw new IOException("No generated key");
			}
			return String.valueOf(l);
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
	public List<String> listIds(String className)
			throws IOException {
		List<String> ids = new ArrayList<String>();
		Connection conn = null;
		try {
			conn = DB.getConnection();
			String sql = "SELECT * FROM " + SYNC_TABLE.getName() + " WHERE "
					+ CLASSNAME.getName() + "=?";
			PreparedStatement stm = conn.prepareStatement(sql);
			stm.setString(1, className);
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
	public <T extends BaseGenericVO> Object getIdServerById(DAO<T> dao,
			Object id) throws IOException {
		String tableName = dao.getTable().getName();
		String idServer = null;
		Connection conn = null;
		try {
			conn = DB.getConnection();
			String sql = "SELECT * FROM " + SYNC_TABLE.getName() + " WHERE "
					+ CLASSNAME.getName() + "=? AND "+ID.getName() +" =? ";
			PreparedStatement stm = conn.prepareStatement(sql);
			stm.setString(1, tableName);
			stm.setObject(2, id);
			ResultSet rs = stm.executeQuery();
			while (rs.next()) {
				idServer = rs.getString(1);
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
		return idServer;
	}

	@Override
	public <T extends BaseGenericVO> boolean delete(DAO<T> dao, String id)
			throws IOException {
		Connection conn = null;
		try {
			conn = DB.getConnection();
			String sql = "DELETE FROM " + SYNC_TABLE.getName() + " WHERE "
					+ ID.getName() + "=?";
			PreparedStatement stm = conn.prepareStatement(sql);
			stm.setString(1, id);
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

	@Override
	public <T extends BaseGenericVO> void updateIdServer(DAO<T> dao,
			Object oldId, Object newPrimaryKey) throws IOException {
		Connection conn = null;
		try {
			conn = DB.getConnection();
			String sql = "UPDATE " + SYNC_TABLE.getName() + "SET "+ID_SERVER.getName()+" =? WHERE "
					+ ID.getName() + "=?";
			PreparedStatement stm = conn.prepareStatement(sql);
			stm.setString(1, newPrimaryKey.toString());
			stm.setString(2, oldId.toString());
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
	}

    @Override
    public List<String> listTables() throws IOException {
        List<String> ids = new ArrayList<String>();
        Connection conn = null;
        try {
            conn = DB.getConnection();
            String sql = "SELECT "+ CLASSNAME.getName() +" FROM " + SYNC_TABLE.getName() ;
            PreparedStatement stm = conn.prepareStatement(sql);
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

}
