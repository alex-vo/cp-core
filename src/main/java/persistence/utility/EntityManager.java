package persistence.utility;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.HibernateUtil;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: vanstr
 * Date: 14.22.2
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */
public abstract class EntityManager<T> {

    final static Logger logger = LoggerFactory.getLogger(EntityManager.class);
    public static Statistics statistic = HibernateUtil.getSessionFactory().getStatistics();

    private TransactionWrapper transactionWrapper = new TransactionWrapper();
    private Session session;
    private String table;

    public EntityManager(String table) {
        this.table = table;

        statistic.setStatisticsEnabled(true);

        startSession();
    }

    public static String getSessionStatistic() {
        return "Session opened:" + statistic.getSessionOpenCount() + " Session closed:" + statistic.getSessionCloseCount();
    }

    public void startSession() {

        if (session == null) {
            logger.debug("Session created");
            session = HibernateUtil.getSessionFactory().openSession();
        } else {
            logger.debug("Session restored");
        }
    }

    public void closeSession(Session session) {
        if (session != null) {
            session.close();
            session = null;
        }
    }

    public void finalize() {
        closeSession(session);
    }

    public T getEntityById(Class<T> EntityClass, long id) {

        T entity = (T) session.load(EntityClass, id);

        return entity;
    }

    public List<T> getEntitiesByFields(Map<String, Object> fields) {

        List<T> list = null;
        if (fields != null && fields.size() > 0) {

            String queryString = "from " + table + " where";
            String andSeparator = " ";
            for (String key : fields.keySet()) {
                queryString += andSeparator + key + "=:" + key;
                andSeparator = " AND ";
            }

            Query query = session.createQuery(queryString);
            query.setProperties(fields);
            list = query.list();
            if (list.size() == 0) {
                list = null;
            }
            //closeSession(session);
            logger.debug("Search entities in " + table + " by fields, hql:" + query.getQueryString());
        }

        return list;
    }

    public List<T> getEntitiesWithInClause(Map<String, List<Object>> fields) {

        List<T> list = null;
        if (fields != null && fields.size() > 0) {

            String queryString = "from " + table + " where";
            String andSeparator = " ";
            for (String key : fields.keySet()) {
                queryString += andSeparator + key + " in (:" + key + ")";
                andSeparator = " AND ";
            }

            Query query = session.createQuery(queryString);
            for(Map.Entry<String, List<Object>> entry : fields.entrySet()){
                query.setParameterList(entry.getKey(), entry.getValue());
            }
            list = query.list();
            if (list.size() == 0) {
                list = null;
            }
            //closeSession(session);
            logger.debug("Search entities in " + table + " by fields, hql:" + query.getQueryString());
        }

        return list;
    }

    public boolean deleteEntity(final T entity){
        boolean result = false;
        return transactionWrapper.run(session, new AbstractExecutor() {
            @Override
            public void execute() {
                session.delete(entity);
            }
        });
    }

    public boolean deleteEntityByIDs(final List<Long> ids) {

        return transactionWrapper.run(session, new AbstractExecutor() {
            @Override
            public void execute() {
                String queryStatement = "delete from " + table;

                String commaSeparator = " ";
                String idList = "";
                for (Long key : ids) {
                    idList += commaSeparator + key;
                    commaSeparator = ",";
                }

                queryStatement += " where id in (" + idList + ") ";
                Query query = session.createQuery(queryStatement);
                query.executeUpdate();

                logger.debug("Deleted entities, hql:" + query.getQueryString());
            }
        });
    }

    public boolean deleteEntitiesByFields(Map<String, Object> fields) {

        boolean result = false;
        if (fields != null && fields.size() > 0) {

            String queryString = "delete " + table + " where";
            String andSeparator = " ";
            for (String key : fields.keySet()) {
                queryString += andSeparator + key + "=:" + key;
                andSeparator = " AND ";
            }

            final Query query = session.createQuery(queryString);
            query.setProperties(fields);
            result = transactionWrapper.run(session, new AbstractExecutor() {
                @Override
                public void execute() {
                    query.executeUpdate();
                }
            });
        }

        return result;
    }

    public boolean updateEntity(final T entity) {

        return transactionWrapper.run(session, new AbstractExecutor() {
            @Override
            public void execute() {
                session.update(entity);
            }
        });
    }

    public boolean addEntity(final T entity) {

        return transactionWrapper.run(session, new AbstractExecutor() {
            @Override
            public void execute() {
                session.save(entity);
            }
        });
    }


}
