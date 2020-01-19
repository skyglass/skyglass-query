package skyglass.data.service.generic;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import skyglass.data.service.generic.jpa.BaseJpaGenericService;

public abstract class BaseGenericService extends BaseJpaGenericService implements IBaseGenericService {

	@PersistenceContext
	protected EntityManager entityManager;

	@Override
	public <T> int countAll(Class<T> type) {
		return _count(type);
	}

	@Override
	public <T> T findByName(Class<T> type, String name) {
		return _findByName(type, name);
	}

	@Override
	public <T> T findById(Class<T> type, Serializable id) {
		return _getById(type, id);
	}

	@Override
	public <T> T[] findByIds(Class<T> type, Serializable... ids) {
		return _getByIds(type, ids);
	}

	@Override
	public <T> List<T> findAll(Class<T> type) {
		return _all(type);
	}

	@Override
	public void flush() {
		_flush();
	}

	@Override
	public <T> T getReference(Class<T> type, Serializable id) {
		return _load(type, id);
	}

	@Override
	public <T> T[] getReferences(Class<T> type, Serializable... ids) {
		return _load(type, ids);
	}

	@Override
	public boolean isAttached(Object entity) {
		return _sessionContains(entity);
	}

	@Override
	public void refresh(Object... entities) {
		_refresh(entities);
	}

	@Override
	public boolean remove(Object entity) {
		return _deleteEntity(entity);
	}

	@Override
	public void remove(Object... entities) {
		_deleteEntities(entities);
	}

	@Override
	public <T> boolean removeById(Class<T> type, Serializable id) {
		return _deleteById(type, id);
	}

	@Override
	public <T> void removeByIds(Class<T> type, Serializable... ids) {
		_deleteById(type, ids);
	}

	@Override
	public Object save(Object entity) {
		return _saveOrUpdateIsNew(entity);
	}

	@Override
	public boolean[] save(Object... entities) {
		return _saveOrUpdateIsNew(entities);
	}

	@Override
	public <T> void deleteAll(Class<T> type) {
		Query q = entityManager.createQuery("delete" + generateFromClause(type));
		q.executeUpdate();
	}

	protected <T> String generateFromClause(Class<T> type) {
		StringBuilder sb = new StringBuilder(" from ");
		sb.append(type.getName());
		return sb.toString();
	}

	protected <T> String generateInsertIntoClause(Class<T> type) {
		StringBuilder sb = new StringBuilder("INSERT INTO ");
		sb.append(type.getName());
		return sb.toString();
	}
}
