package com.quantium.mobile.framework.libjdbctest;

import java.sql.Connection;

import com.quantium.mobile.framework.BaseModelFacade;
import com.quantium.mobile.framework.libjdbctest.db.DB;
import com.quantium.mobile.framework.libjdbctest.gen.JdbcDAOFactory;

public class MemDaoFactory extends JdbcDAOFactory {

	private Connection connection;
    private BaseModelFacade baseModelFacade;

    public MemDaoFactory(){
        baseModelFacade = new BaseModelFacade(this, new JdbcPrimaryKeyProvider(), new JdbcToSyncProvider()) {

            @Override
            protected String getLoggedUserId() {
                return "1";
            }
        };
    }

    @Override
    public BaseModelFacade getModelFacade() {
        return baseModelFacade;
    }

    @Override
	public Connection getConnection() {
		if (connection == null)
			connection = DB.getConnection();
		return connection;
	}
}
