package com.quantium.mobile.framework.libjdbctest;

import java.sql.Connection;

import com.quantium.mobile.framework.BaseGenericVO;
import com.quantium.mobile.framework.BaseModelFacade;
import com.quantium.mobile.framework.libjdbctest.db.DB;
import com.quantium.mobile.framework.libjdbctest.gen.JdbcDAOFactory;

public class MemDaoFactory extends JdbcDAOFactory {

	public static Connection connection;
    private BaseModelFacade baseModelFacade;

    public MemDaoFactory(){
        baseModelFacade = new BaseModelFacade(this, new JdbcPrimaryKeyProvider(getConnection()), new JdbcToSyncProvider(getConnection())) {

            @Override
            protected String getLoggedUserId() {
                return "1";
            }

            @Override
            public <T extends BaseGenericVO> T refresh(Class<T> clazz, String id) throws Throwable {
                return null;
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
