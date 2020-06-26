package zhaoshuo.seckill.JpaQuery;

/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-06-23 20:22
 */
public interface DynamicQuery {

    void save(Object entity);

    void update(Object entity);

    <T> void delete(Class<T> entityClass, Object entityid);

    <T> void delete(Class<T> entityClass, Object[] entityids);
    /**
     * 执行nativeSql统计查询
     * @param nativeSql
     * @param params 占位符参数(例如?1)绑定的参数值
     * @return 统计条数
     */
    Object nativeQueryObject(String nativeSql, Object... params);



    int nativeExecuteUpdate(String nativeSql, Object... params);
}
