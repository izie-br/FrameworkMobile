package com.quantium.mobile.framework.libjdbctest;

import com.quantium.mobile.framework.BaseGenericVO;
import com.quantium.mobile.framework.PrimaryKeyProvider;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class JdbcPrimaryKeyProvider extends PrimaryKeyProvider {

    private final Connection connection;

    public JdbcPrimaryKeyProvider(Connection connection) {
        this.connection = connection;
    }

    @Override
    public String sequenceNextFor(String tableName) throws IOException {
        try {
            String sql = "INSERT INTO " + SYNC_TABLE.getName() + " ("
                    + CLASSNAME.getName() + ") VALUES (?)";
            PreparedStatement stm = connection.prepareStatement(sql);
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
        }
    }

    @Override
    public List<String> listIds(String className)
            throws IOException {
        List<String> ids = new ArrayList<String>();
        try {
            String sql = "SELECT * FROM " + SYNC_TABLE.getName() + " WHERE "
                    + CLASSNAME.getName() + "=?";
            PreparedStatement stm = connection.prepareStatement(sql);
            stm.setString(1, className);
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                ids.add(rs.getString(1));
            }
        } catch (java.sql.SQLException e) {
            throw new IOException(e);
        } finally {
        }
        return ids;
    }

    @Override
    public <T extends BaseGenericVO> Object getIdServerById(String tableName,
                                                            Object id) throws IOException {
        String idServer = null;
        try {
            String sql = "SELECT * FROM " + SYNC_TABLE.getName() + " WHERE "
                    + CLASSNAME.getName() + "=? AND " + ID.getName() + " =? ";
            PreparedStatement stm = connection.prepareStatement(sql);
            stm.setString(1, tableName);
            stm.setObject(2, id);
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                idServer = rs.getString(1);
            }
        } catch (java.sql.SQLException e) {
            throw new IOException(e);
        } finally {
        }
        return idServer;
    }

    @Override
    public <T extends BaseGenericVO> boolean delete(String tableName, String id)
            throws IOException {
        try {
            String sql = "DELETE FROM " + SYNC_TABLE.getName() + " WHERE "
                    + ID.getName() + "=?";
            PreparedStatement stm = connection.prepareStatement(sql);
            stm.setString(1, id);
            stm.execute();
        } catch (java.sql.SQLException e) {
            throw new IOException(e);
        } finally {
        }
        return true;
    }

    @Override
    public <T extends BaseGenericVO> void updateIdServer(String tableName,
                                                         Object oldId, Object newPrimaryKey) throws IOException {
        try {
            String sql = "UPDATE " + SYNC_TABLE.getName() + "SET " + ID_SERVER.getName() + " =? WHERE "
                    + ID.getName() + "=?";
            PreparedStatement stm = connection.prepareStatement(sql);
            stm.setString(1, newPrimaryKey.toString());
            stm.setString(2, oldId.toString());
            stm.execute();
        } catch (java.sql.SQLException e) {
            throw new IOException(e);
        } finally {
        }
    }

    @Override
    public List<String> listTables() throws IOException {
        List<String> ids = new ArrayList<String>();
        try {
            String sql = "SELECT " + CLASSNAME.getName() + " FROM " + SYNC_TABLE.getName();
            PreparedStatement stm = connection.prepareStatement(sql);
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                ids.add(rs.getString(1));
            }
        } catch (java.sql.SQLException e) {
            throw new IOException(e);
        } finally {
        }
        return ids;
    }

}
