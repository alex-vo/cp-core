package persistence.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.SongEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: vanstr
 * Date: 14.19.2
 * Time: 21:12
 * To change this template use File | Settings | File Templates.
 */
public class SongManager extends EntityManager<SongEntity> {
    final static Logger logger = LoggerFactory.getLogger(SongManager.class);
    public static final String table = SongEntity.class.getName();

    public SongManager(){
        super(table);
    }

    public SongEntity getSongById(long id) {
        return getEntityById(SongEntity.class, id);
    }

    public boolean updateSong(SongEntity song) {
        return updateEntity(song);
    }

    public List<SongEntity> getSongsByFields(Map<String, Object> fields) {
        return getEntitiesByFields(fields);
    }

    public boolean addSong(SongEntity song) {
        return addEntity(song);
    }

    public boolean deleteSongsByID(List<Long> ids) {
        return deleteEntityByIDs(ids);
    }

    public SongEntity getSongByHash(Long userId, long cloudId, String fileId) {

        Map<String, Object> fieldMap = new HashMap<String, Object>();
        fieldMap.put("cloud_id", cloudId);
        fieldMap.put("file_id", fileId);
        fieldMap.put("user_id", userId);
        List<SongEntity> list = getEntitiesByFields(fieldMap);

        SongEntity songEntity = null;
        if (list != null) {
            if (list.size() == 1) {
                songEntity = list.get(0);
            } else {
                logger.warn("incorrect return value list.size()=" + list.size());
            }
        } else {
            logger.info("Returned list=" + null);
        }

        return songEntity;
    }
}
