package com.quantium.mobile.framework;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.validation.ValidationError;

public abstract class BaseModelFacade {
	private final DAOFactory daoFactory;
	private final PrimaryKeyProvider primaryKeyProvider;
	private final ToSyncProvider toSyncProvider;
	private boolean debugValidation = true;

	public static final int FORM_CODE_DINHEIRO = 1;
	public static final int FORM_CODE_CHEQUE = 2;
	public static final int FORM_CODE_CARTAO = 3;

	public BaseModelFacade(DAOFactory daoFactory,
			PrimaryKeyProvider primaryKeyProvider, ToSyncProvider toSyncProvider) {
		this.daoFactory = daoFactory;
		this.primaryKeyProvider = primaryKeyProvider;
		this.toSyncProvider = toSyncProvider;
	}

	protected abstract String getLoggedUserId();

	public final <T extends BaseGenericVO> boolean addToSync(Class<T> klass, String id,
			long action) throws IOException {
		return toSyncProvider.save(daoFactory.getDaoFor(klass),
				getLoggedUserId(), id, action);
	}

	public final <T extends BaseGenericVO> List<String> listTempIds(Class<T> klass)
			throws IOException {
		return primaryKeyProvider.listIds(daoFactory.getDaoFor(klass));
	}

	public final <T extends BaseGenericVO> List<String> listToSyncIds(Class<T> klass,
			long action) throws IOException {
		return toSyncProvider.listIds(daoFactory.getDaoFor(klass),
				getLoggedUserId(), action);
	}

	public final <T extends BaseGenericVO> boolean deleteToSyncId(Class<T> klass,
			String id, long action) throws IOException {
		return toSyncProvider.delete(daoFactory.getDaoFor(klass),
				getLoggedUserId(), id, action);
	}

	public final <T extends BaseGenericVO> boolean deleteTempId(Class<T> klass,
			String id) throws IOException {
		return primaryKeyProvider.delete(daoFactory.getDaoFor(klass), id);
	}

	@SuppressWarnings("unchecked")
	public final <T extends BaseGenericVO> boolean save(T obj) throws IOException {
		if (obj == null) {
			return false;
		}
		Collection<ValidationError> validate = newValidate(obj);
		if (getDefaultId().equals(obj.getId())) {
			primaryKeyProvider.generatePrimaryKey(
					daoFactory.getDaoFor((Class<T>) obj.getClass()), obj);
		}
		if (validate.size() == 0) {
			return daoFactory.getDaoFor((Class<T>) obj.getClass()).save(obj,
					Save.INSERT_IF_NOT_EXISTS);
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
	public final <T extends BaseGenericVO> void updatePrimaryKey(T target,
			Object newPrimaryKey) throws IOException {
		((PrimaryKeyUpdater<T>) daoFactory.getDaoFor((Class<T>) target
				.getClass())).updatePrimaryKey(target, newPrimaryKey);
	}

	@SuppressWarnings("unchecked")
	public final <T extends BaseGenericVO> void updateWithMap(T target,
			Map<String, Object> map) throws IOException {
		daoFactory.getDaoFor((Class<T>) target.getClass()).updateWithMap(
				target, map);
	}

	@SuppressWarnings("unchecked")
	public final <T extends BaseGenericVO> boolean delete(T obj) throws IOException {
		if (obj == null) {
			return false;
		}
		DAO<T> dao = daoFactory.getDaoFor((Class<T>) obj.getClass());
		if (primaryKeyProvider.delete(dao, obj.getId())) {
			return dao.delete(obj);
		} else {
			toSyncProvider.delete(dao, obj.getId(), getLoggedUserId(),
					ToSyncProvider.SAVE);
			if (toSyncProvider.save(dao, getLoggedUserId(), obj.getId(),
					ToSyncProvider.DELETE)) {
				return dao.delete(obj);
			}
		}
		return false;
	}

	public final <T extends BaseGenericVO> T get(Class<T> clazz, Object id) {
		return daoFactory.getDaoFor(clazz).get(id);
	}

	@SuppressWarnings("unchecked")
	public final <T extends BaseGenericVO> T get(T prototype, Object id) {
		return (T) daoFactory.getDaoFor(prototype.getClass()).get(id);
	}

	public final <T extends BaseGenericVO> T mapToObject(Class<T> clazz,
			Map<String, Object> map) {
		return daoFactory.getDaoFor(clazz).mapToObject(map);
	}

	@SuppressWarnings("unchecked")
	public final <T> Collection<ValidationError> newValidate(T obj)
			throws IOException {
		return daoFactory.getDaoFor((Class<T>) obj.getClass()).validate(obj);
	}

	@SuppressWarnings("unchecked")
	public final <T extends BaseGenericVO> boolean withAdd(T with, Object add)
			throws IOException {
		if (save((BaseGenericVO) add)) {
			return daoFactory.getDaoFor((Class<T>) with.getClass()).with(with)
					.add(add);
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public final <T extends BaseGenericVO> boolean withRemove(T with, Object remove)
			throws IOException {
		return daoFactory.getDaoFor((Class<T>) with.getClass()).with(with)
				.remove(remove);
	}

	public final <T extends BaseGenericVO> QuerySet<T> query(Class<T> clazz) {
		return daoFactory.getDaoFor(clazz).query();
	}

	public final <T extends BaseGenericVO> QuerySet<T> query(Class<T> clazz, Q q) {
		return daoFactory.getDaoFor(clazz).query(q);
	}

	protected final <T extends BaseGenericVO> DAO<T> daoFor(Class<T> klass) {
		return daoFactory.getDaoFor(klass);
	}

}
