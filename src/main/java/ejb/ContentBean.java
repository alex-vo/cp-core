package ejb;

import cloud.DriveFileFetcher;
import cloud.Dropbox;
import cloud.DropboxFileFetcher;
import cloud.GDrive;
import commons.FileFetcher;
import commons.SongMetadataPopulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.PlayListEntity;
import persistence.SongEntity;
import persistence.UserEntity;
import persistence.utility.PlayListManager;
import persistence.utility.SongManager;
import persistence.utility.UserManager;
import structure.PlayList;
import structure.Song;
import structure.SongMetadata;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * UserEntity: vanstr
 * Date: 13.6.7
 * Time: 21:45
 * To change this template use File | Settings | File Templates.
 */

@Stateless
@Remote(ContentBeanRemote.class)
public class ContentBean implements ContentBeanRemote {

    final static Logger logger = LoggerFactory.getLogger(ContentBean.class);

    public String getFileSrc(Long userId, Integer cloudId, String fileId) {
        String file = null;
        UserManager manager = new UserManager();
        UserEntity user = manager.getUserById(userId);
        try {
            if (DROPBOX_CLOUD_ID.equals(cloudId)) {
                String accessTokenKey = user.getDropboxAccessKey();
                Dropbox drop = new Dropbox(accessTokenKey);

                logger.info(fileId);
                file = drop.getFileLink(fileId);
            } else if (DRIVE_CLOUD_ID.equals(cloudId)) {
                GDrive gDrive = new GDrive(user.getDriveAccessToken(), user.getDriveRefreshToken());
                file = gDrive.getFileLink(fileId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        manager.finalize();

        return file;

    }

    @Override
    public PlayList getPlayList(Long userId) {

        List<Song> data = getFiles("/", userId);
        PlayList playList = SongMetadataPopulation.populate(data, userId);

        return playList;
    }

    @Override
    public boolean saveSongMetadata(Song song, Long userId) {

        boolean result = false;

        UserManager userManager = new UserManager();
        UserEntity user = userManager.getEntityById(UserEntity.class, userId);

        SongManager songManager = new SongManager();
        logger.info("pass1" + song + " " + song.getCloudId() + " " + song.getFileName());
        SongEntity songEntity = songManager.getSongByHash(user.getId(), song.getCloudId(), song.getFileId());
        userManager.finalize();

        if (songEntity == null) {
            // songEntity is empty, create new with metadata
            songEntity = new SongEntity();
            songEntity.setCloudId(song.getCloudId());
            songEntity.setFileId(song.getFileId());
            songEntity.setFileName(song.getFileName());
            songEntity.setUser(user);
            songEntity = setMetadata(songEntity, song);
            result = songManager.addSong(songEntity);
        } else {
            // update metadata
            songEntity = setMetadata(songEntity, song);
            result = songManager.updateSong(songEntity);
        }

        songManager.finalize();

        return result;
    }

    @Override
    public long addPlayList(Long userId, PlayList playList) {
        long playListId = -1;
        UserManager userManager = new UserManager();

        UserEntity user = userManager.getUserById(userId);
        userManager.finalize();

        SongManager songManager = new SongManager();
        Set<SongEntity> songs = new HashSet<SongEntity>();
        if(playList.getSongs() != null){
            List<Object> fileIds = new ArrayList<Object>();
            List<Object> cloudIds = new ArrayList<Object>();

            for(int i = 0; i < playList.getSongs().size(); i++){
                fileIds.add(i, playList.getSongs().get(i).getFileId());
                cloudIds.add(i, playList.getSongs().get(i).getCloudId());
            }

            songs.addAll(addExistingSongs(fileIds, cloudIds, songManager));
            songs.addAll(addNewSongs(fileIds, cloudIds, songManager, user));
        }

        songManager.finalize();

        PlayListManager playListManager = new PlayListManager();
        PlayListEntity playListEntity = new PlayListEntity();

        playListEntity.setName(playList.getName());
        playListEntity.setUser(user);
        playListEntity.setCreated(new Timestamp(System.currentTimeMillis()));
        playListEntity.setUpdated(new Timestamp(System.currentTimeMillis()));
        playListEntity.setSongs(songs);

        playListId = playListManager.addPlayList(playListEntity);
        playListManager.finalize();

        return playListId;
    }

    @Override
    public List<PlayList> getPlayLists(Long userId) {
        PlayListManager playListManager = new PlayListManager();
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("user_id", userId);
        List<PlayListEntity> entities = playListManager.getEntitiesByFields(fields);
        List<PlayList> playLists = new ArrayList<PlayList>();
        if(entities != null){
            for(PlayListEntity entity : entities){
                playLists.add(new PlayList(entity));
            }
        }
        return playLists;
    }

    @Override
    public PlayList getPlayList(Long userId, Long playListId) {

        PlayListManager playListManager = new PlayListManager();
        PlayListEntity playListEntity = playListManager.getEntityById(PlayListEntity.class, playListId);

        PlayList playList = new PlayList(playListId, playListEntity.getName());
        for(SongEntity songEntity : playListEntity.getSongs()){
            playList.add(new Song(songEntity));
        }
        playListManager.finalize();

        return playList;
    }

    @Override
    public boolean deletePlayList(Long playListId) {
        boolean result = false;
        PlayListManager playListManager = new PlayListManager();
        PlayListEntity entity = playListManager.getEntityById(PlayListEntity.class, playListId);
        result = playListManager.deleteEntity(entity);
        playListManager.finalize();

        return result;
    }

    private SongEntity setMetadata(SongEntity songEntity, Song song) {
        SongMetadata metadata = song.getMetadata();
        if (metadata != null) {
            songEntity.setMetadataTitle(metadata.getTitle());
            songEntity.setMetadataAlbum(metadata.getAlbum());
            songEntity.setMetadataArtist(metadata.getArtist());
            songEntity.setMetadataGenre(metadata.getGenre());
            songEntity.setMetadataYear(metadata.getYear());
        }
        return songEntity;
    }

    private List<Song> getFiles(String folderPath, Long userId) {

        FileFetcher dropboxFetcher = new DropboxFileFetcher(folderPath, userId);
        FileFetcher driveFetcher = new DriveFileFetcher(folderPath, userId);
        Thread dropboxThread = new Thread(dropboxFetcher);
        Thread driveThread = new Thread(driveFetcher);
        dropboxThread.start();
        driveThread.start();
        try {
            dropboxThread.join();
            driveThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Song> files = new ArrayList<Song>();
        if (dropboxFetcher.getFiles() != null) {
            files.addAll(dropboxFetcher.getFiles());
        }
        if (driveFetcher.getFiles() != null) {
            files.addAll(driveFetcher.getFiles());
        }

        return files;
    }

    private Set<SongEntity> addExistingSongs(List<Object> fileIds, List<Object> cloudIds, SongManager songManager){
        Set<SongEntity> songs = new HashSet<SongEntity>();
        Map<String, List<Object>> fields = new HashMap<String, List<Object>>();
        fields.put("file_id", fileIds);
        fields.put("cloud_id", cloudIds);
        List<SongEntity> songList = songManager.getEntitiesWithInClause(fields);

        for(SongEntity songEntity : songList){
            fileIds.remove(songEntity.getFileId());
            cloudIds.remove(songEntity.getCloudId());
            songs.add(songEntity);
        }

        return songs;
    }

    private Set<SongEntity> addNewSongs(List<Object> fileIds, List<Object> cloudIds,
                                        SongManager songManager, UserEntity user){
        Set<SongEntity> songs = new HashSet<SongEntity>();

        for(int i = 0; i < fileIds.size(); i++){
            SongEntity songEntity = new SongEntity();
            songEntity.setUser(user);
            songEntity.setFileId((String) fileIds.get(i));
            songEntity.setCloudId((Long) cloudIds.get(i));
            songManager.addEntity(songEntity);
            songs.add(songEntity);
        }

        return songs;
    }
}
