package persistence;

import commons.SystemProperty;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * UserEntity: user
 * Date: 6/21/13
 * Time: 7:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class HibernateUtil {
    private static final SessionFactory sessionFactory;

    final static Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

    static {
        try {
            Configuration conf = new Configuration()
                    .configure()
                    .addProperties(SystemProperty.getLocalProperties())
                    .addPackage("persistence")
                    .addAnnotatedClass(UserEntity.class)
                    .addAnnotatedClass(SongEntity.class)
                    .addAnnotatedClass(PlayListEntity.class);
            sessionFactory = conf.buildSessionFactory();

        } catch (Throwable ex) {
            logger.warn("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
