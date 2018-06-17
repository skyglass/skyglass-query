package skyglass.data.service.generic;

import java.io.Serializable;
import java.util.List;

public interface IBaseGenericService {

    public <T> T findById(Class<T> type, Serializable id);

    public <T> T findByName(Class<T> type, String name);

    public <T> T[] findByIds(Class<T> type, Serializable... ids);

    public <T> T getReference(Class<T> type, Serializable id);

    public <T> T[] getReferences(Class<T> type, Serializable... ids);

    public Object save(Object entity);

    public boolean[] save(Object... entities);

    public boolean remove(Object entity);

    public void remove(Object... entities);

    public <T> boolean removeById(Class<T> type, Serializable id);

    public <T> void removeByIds(Class<T> type, Serializable... ids);

    public <T> List<T> findAll(Class<T> type);

    public <T> int countAll(Class<T> type);

    public boolean isAttached(Object entity);

    public void refresh(Object... entities);

    public void flush();

    public <T> void deleteAll(Class<T> type);

}
