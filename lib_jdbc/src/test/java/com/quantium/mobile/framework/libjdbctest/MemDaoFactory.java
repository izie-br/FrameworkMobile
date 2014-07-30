package com.quantium.mobile.framework.libjdbctest;

import java.sql.Connection;

import com.quantium.mobile.framework.BaseModelFacade;
import com.quantium.mobile.framework.libjdbctest.db.DB;
import com.quantium.mobile.framework.libjdbctest.gen.JdbcDAOFactory;

public abstract class MemDaoFactory extends JdbcDAOFactory {

	private Connection connection;

	@Override
	public Connection getConnection() {
		if (connection == null)
			connection = DB.getConnection();
		return connection;
	}

    public abstract BaseModelFacade getFacade();
}
