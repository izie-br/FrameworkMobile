package com.quantium.mobile.framework;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.validation.ValidationError;

public abstract class BaseModelFacade {
	private final DAOFactory daoFactory;
	private final PrimaryKeyProvider primaryKeyProvider;
	private final ToSyncProvider toSyncProvider;
	private boolean debugValidation = true;

	public BaseModelFacade(DAOFactory daoFactory,
			PrimaryKeyProvider primaryKeyProvider, ToSyncProvider toSyncProvider) {
		this.daoFactory = daoFactory;
		this.primaryKeyProvider = primaryKeyProvider;
		this.toSyncProvider = toSyncProvider;
		this.daoFactory.setModelFacade(this);
	}

	protected abstract String getLoggedUserId();

    public <T extends BaseGenericVO> boolean addToSync(Class<T> klass,
                                                       String id, long action) throws IOException {
        return toSyncProvider.save(getLoggedUserId(),
                daoFactory.getDaoFor(klass).getTable().getName(), id, action);
    }

    public <T extends BaseGenericVO> boolean addToSync(String tableName,
                                                       String id, long action) throws IOException {
        return toSyncProvider.save(getLoggedUserId(),
                tableName, id, action);
    }

    public <T extends BaseGenericVO> List<String> listTempIds(Class<T> klass)
            throws IOException {
        return listTempIds(daoFactory.getDaoFor(klass).getTable().getName());
    }

    public <T extends BaseGenericVO> List<String> listTempIds(String tableName)
            throws IOException {
        return primaryKeyProvider.listIds(tableName);
    }

    public <T extends BaseGenericVO> List<String> listTempTables()
            throws IOException {
        return primaryKeyProvider.listTables();
    }

    public <T extends BaseGenericVO> Object getIdServerById(Object tempId, Class<T> klass)
            throws IOException {
        return getIdServerById(tempId, daoFactory.getDaoFor(klass).getTable().getName());
    }

    public <T extends BaseGenericVO> Object getIdServerById(Object tempId, String tableName)
            throws IOException {
        return primaryKeyProvider.getIdServerById(tableName, tempId);
    }

	public <T extends BaseGenericVO> List<String> listToSyncIds(Class<T> klass,
			long action) throws IOException {
		return toSyncProvider.listIds(getLoggedUserId(),
				daoFactory.getDaoFor(klass).getTable().getName(), action);
	}

	public <T extends BaseGenericVO> boolean deleteToSyncId(Class<T> klass,
			String id, long action) throws IOException {
		return toSyncProvider.delete(getLoggedUserId(),
				daoFactory.getDaoFor(klass).getTable().getName(), id, action);
	}

	@SuppressWarnings("unchecked")
	public <T extends BaseGenericVO> boolean save(T obj) throws IOException {
		return save(obj, false);
	}

	@SuppressWarnings("unchecked")
	public <T extends BaseGenericVO> boolean save(T obj, boolean skipSync)
			throws IOException {
		if (obj == null) {
			return false;
		}
		boolean newObj = obj.getId() == null
				|| obj.getId().equals(getDefaultId());
		DAO<T> dao = daoFactory.getDaoFor((Class<T>) obj.getClass());
		Collection<ValidationError> validate = newValidate(obj);
		if (newObj) {
			primaryKeyProvider.generatePrimaryKey(dao.getTable().getName(), obj);
		}
		if (validate.size() == 0) {
			if (dao.save(obj, Save.INSERT_IF_NOT_EXISTS)) {
				if (skipSync) {
					return true;
				}
				if (!newObj) {
					return toSyncProvider.save(getLoggedUserId(), dao.getTable().getName(),
							obj.getId(), ToSyncProvider.SAVE);
				}
				return true;
			}
			return false;
		} else {
			if (debugValidation) {
				for (ValidationError error : validate) {
					LogPadrao.d(String.format("%s.%s -> %s", error.getColumn()
							.getTable().getName(), error.getColumn().getName(),
							error.getConstraint()));
				}
			}
			return false;
		}
	}

	protected Object getDefaultId() {
		return new Object();
	}

	@SuppressWarnings("unchecked")
	public <T extends BaseGenericVO> void updatePrimaryKey(T target,
			Object newPrimaryKey) throws IOException {
		String oldId = target.getId();
		DAO<T> dao = daoFactory.getDaoFor((Class<T>) target
				.getClass());
		((PrimaryKeyUpdater<T>) dao).updatePrimaryKey(target, newPrimaryKey);
		primaryKeyProvider.updateIdServer(dao.getTable().getName(), oldId, newPrimaryKey);
	}

	@SuppressWarnings("unchecked")
	public <T extends BaseGenericVO> void updateWithMap(T target,
			Map<String, Object> map) throws IOException {
		daoFactory.getDaoFor((Class<T>) target.getClass()).updateWithMap(
				target, map);
	}

	@SuppressWarnings("unchecked")
	public <T extends BaseGenericVO> boolean delete(T obj, boolean hardDelete)
			throws IOException {
		if (obj == null) {
			return false;
		}
		DAO<T> dao = daoFactory.getDaoFor((Class<T>) obj.getClass());
		if (primaryKeyProvider.delete(dao.getTable().getName(), obj.getId())) {
			if (hardDelete) {
				return dao.delete(obj);
			} else {
				obj.setInactivatedAt(new Date());
				return dao.save(obj);
			}
		} else {
			toSyncProvider.delete(getLoggedUserId(), dao.getTable().getName(), obj.getId(),
					ToSyncProvider.SAVE);
			if (toSyncProvider.save(getLoggedUserId(), dao.getTable().getName(), obj.getId(),
					ToSyncProvider.DELETE)) {
				if (hardDelete) {
					return dao.delete(obj);
				} else {
					obj.setInactivatedAt(new Date());
					return dao.save(obj);
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public <T extends BaseGenericVO> boolean delete(T obj) throws IOException {
		return delete(obj, false);
	}

	public <T extends BaseGenericVO> T get(Class<T> clazz, Object id) {
		return daoFactory.getDaoFor(clazz).get(id);
	}

	@SuppressWarnings("unchecked")
	public <T extends BaseGenericVO> T get(T prototype, Object id) {
		return (T) daoFactory.getDaoFor(prototype.getClass()).get(id);
	}

	public <T extends BaseGenericVO> T mapToObject(Class<T> clazz,
			Map<String, Object> map) {
		return daoFactory.getDaoFor(clazz).mapToObject(map);
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<ValidationError> newValidate(T obj)
			throws IOException {
		return daoFactory.getDaoFor((Class<T>) obj.getClass()).validate(obj);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Collection<ValidationError> validate(T obj)
			throws IOException {
		return daoFactory.getDaoFor((Class<T>) obj.getClass()).validate(obj);
	}

	@SuppressWarnings("unchecked")
	public <T extends BaseGenericVO> boolean withAdd(T with, Object add)
			throws IOException {
		if (save((BaseGenericVO) add)) {
			return daoFactory.getDaoFor((Class<T>) with.getClass()).with(with)
					.add(add);
		} else {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends BaseGenericVO> boolean withRemove(T with, Object remove)
			throws IOException {
		return daoFactory.getDaoFor((Class<T>) with.getClass()).with(with)
				.remove(remove);
	}

	public <T extends BaseGenericVO> QuerySet<T> query(Class<T> clazz,
			boolean showInactives) {
		DAO<T> dao = daoFactory.getDaoFor(clazz);
		QuerySet<T> query = dao.query();
		if (!showInactives) {
			Table.Column<Date> columnInactivatedAt = getColumnByName(clazz,
					"INACTIVATED_AT", Date.class);
			if (columnInactivatedAt == null) {
				LogPadrao.e(new IllegalArgumentException(
						"columnInactivatedAt can not be null!"));
			} else {
				query = dao.query(columnInactivatedAt.isNull());
			}
		}
		return query;
	}

	public <T extends BaseGenericVO> QuerySet<T> query(Class<T> clazz) {
		return query(clazz, false);
	}

	public <T extends BaseGenericVO> QuerySet<T> query(Class<T> clazz, Q q,
			boolean showInactives) {
		return query(clazz, showInactives).filter(q);
	}

	public <T extends BaseGenericVO> QuerySet<T> query(Class<T> clazz, Q q) {
		return query(clazz, q, false);
	}

	protected <T extends BaseGenericVO> DAO<T> daoFor(Class<T> klass) {
		return daoFactory.getDaoFor(klass);
	}

	@SuppressWarnings("unchecked")
	public <T> Table.Column<T> getColumnByName(Class<?> clazz,
			String columnName, Class<T> columnClass) {
		Table.Column<T> column = null;
		try {
			column = (Table.Column<T>) clazz.getField(columnName).get(null);
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		}
		return column;
	}
	
	public DAOFactory getDaoFactory(){
		return daoFactory;
	}

    public abstract <T extends BaseGenericVO> T refresh(Class<T> clazz, String id) throws Throwable;
}
