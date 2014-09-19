package ejb;

import structure.PlayList;
import structure.Song;

import javax.ejb.Remote;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * UserEntity: user
 * Date: 7/5/13
 * Time: 11:01 AM
 * To change this template use File | Settings | File Templates.
 */

@Remote
public interface ContentBeanRemote {

    public static final Integer DROPBOX_CLOUD_ID = 1;
    public static final Integer DRIVE_CLOUD_ID = 2;
    

    public String getFileSrc(Long userId, Integer cloudId, String fileId);

    public PlayList getPlayList(Long userId);

    public boolean saveSongMetadata(Song song, Long userId);

    public long addPlayList(Long userId, PlayList playList);

    public List<PlayList> getPlayLists(Long userId);

    public PlayList getPlayList(Long userId, Long playListId);

    public boolean deletePlayList(Long playListId);

}