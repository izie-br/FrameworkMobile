package com.quantium.mobile.framework.test;

import android.database.sqlite.SQLiteDatabase;
import com.quantium.mobile.framework.test.db.DB;
import com.quantium.mobile.framework.test.gen.SQLiteDAOFactory;

public class SessionFacade extends BaseModelFacade {

    public SessionFacade() {
        super(new SQLiteDAOFactory() {

            @Override
            public SQLiteDatabase getDb() {
                return DB.getDb();
            }
        }, new AndroidPrimaryKeyProvider() {

            @Override
            public SQLiteDatabase getDb() {
                return DB.getDb();
            }
        }, new AndroidToSyncProvider() {

            @Override
            public SQLiteDatabase getDb() {
                return DB.getDb();
            }
        });
    }

    @Override
    protected String getLoggedUserId() {
        return null;
    }

    @Override
    public <T extends BaseGenericVO> T refresh(Class<T> clazz, String id) throws Throwable {
        return null;
    }

}
