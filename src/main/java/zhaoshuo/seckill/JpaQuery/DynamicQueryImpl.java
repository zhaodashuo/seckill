package zhaoshuo.seckill.JpaQuery;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-06-23 20:23
 */
@Repository
public class DynamicQueryImpl implements DynamicQuery {
    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public void save(Object entity) {
        entityManager.persist(entity);
    }

    @Override
    public void update(Object entity) {
        entityManager.merge(entity);
    }

    @Override
    public <T> void delete(Class<T> entityClass, Object entityid) {
            delete(entityClass,new Object[]{entityid});
    }

    @Override
    public <T> void delete(Class<T> entityClass, Object[] entityids) {
        for(Object id :entityids){
            entityManager.remove(entityManager.getReference(entityClass,id));
        }
    }

    private Query createNativeQuery(String sql,Object ... objects){
        Query query = entityManager.createNativeQuery(sql);
        if(objects!=null&&objects.length>0){
            for(int i=0;i<objects.length;i++){
                query.setParameter(i+1,objects[i]);
            }
        }
        return query;
    }


    @Override
    public Object nativeQueryObject(String nativeSql, Object... params) {
        Query nativeQuery = createNativeQuery(nativeSql, params);
        return  nativeQuery.getSingleResult();
    }

    @Override
    public int nativeExecuteUpdate(String nativeSql, Object... params) {
        Query nativeQuery = createNativeQuery(nativeSql, params);
        int i = nativeQuery.executeUpdate();
        return i;
    }
}
