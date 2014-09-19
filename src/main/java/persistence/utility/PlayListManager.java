package persistence.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.PlayListEntity;

/**
 * Created with IntelliJ IDEA.
 * User: alex
 * Date: 7/10/14
 * Time: 11:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlayListManager extends EntityManager<PlayListEntity> {
    final static Logger logger = LoggerFactory.getLogger(PlayListManager.class);
    public static final String table = PlayListEntity.class.getName();

    public PlayListManager() {
        super(table);
    }

    public Long addPlayList(final PlayListEntity playListEntity) {
        if(addEntity(playListEntity)){
            return playListEntity.getId();
        }else{
            return null;
        }
    }
}
