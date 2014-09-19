package persistence.utility;

import persistence.UserEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Process CURD activities relatively User table
 * UserEntity: vanstr
 * Date: 13.7.7
 * Time: 18:26
 * User table manager
 */
public class UserManager extends EntityManager<UserEntity> {

    public static final String table =  UserEntity.class.getName();

    public UserManager(){
        super(table);
    }

    public void finalize(){
        super.finalize();
    }

    public boolean updateUser(final UserEntity user) {
        return updateEntity(user);
    }


    public List<UserEntity> getUsersByFields(Map<String, Object> fields) {
        return getEntitiesByFields(fields);
    }

    public List<UserEntity> getUsersByField(String key, Object value) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(key, value);
        return getEntitiesByFields(params);
    }

    public Long addUser(final UserEntity user) {
        if(addEntity(user)){
            return user.getId();
        }else{
            return null;
        }
    }

    public boolean deleteUsersByIDs(final List<Long> ids) {
        return deleteEntityByIDs(ids);
    }

    public UserEntity getUserById(long id) {
        return getEntityById(UserEntity.class, id);
    }

}
