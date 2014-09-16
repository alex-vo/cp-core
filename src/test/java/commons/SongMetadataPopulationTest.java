package commons;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.SongEntity;
import persistence.UserEntity;
import persistence.utility.SongManager;
import structure.PlayList;
import structure.Song;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: vanstr
 * Date: 14.1.3
 * Time: 22:41
 * To change this template use File | Settings | File Templates.
 */
public class SongMetadataPopulationTest {
    final static Logger logger = LoggerFactory.getLogger(SongMetadataPopulationTest.class);

    static SongEntity song1 = null;
    static Long userId = 1l;
    static Long cloudId = 1l;
    static UserEntity user;
    static SongManager songManger;

    @BeforeClass
    public static void method() {
        user = new UserEntity();
        user.setId(userId);

        songManger = new SongManager();

        // existed song
        song1 = new SongEntity();
        song1.setUser(user);
        song1.setCloudId(cloudId);
        song1.setFileId("MyJunitTest.mp3");
        song1.setFileName("MyJunitTest.mp3");
        song1.setFileSize(1111);
        song1.setMetadataTitle("JUnitTests");
        song1.setHasMetadata(true);
        songManger.addSong(song1);

        logger.info("BeforeClass done");
    }

    @Test
    public void test1PopulatePlaylist() {

        List<Song> data = new ArrayList<Song>();
        // existed song
        Song trackHasMetadata = new Song(song1.getCloudId(), song1.getFileName(), song1.getFileName(), null, null);
        Song trackDoesNotHasMetadata = new Song(cloudId, "NoThatSong", "", null, null);
        data.add(trackHasMetadata);
        data.add(trackDoesNotHasMetadata);

        PlayList playList = SongMetadataPopulation.populate(data, userId);

        logger.debug("Songs in playlist:" + playList.getSongs().size());
        try {

            for (Song song : playList.getSongs()) {
                if (song.getFileName().equals(song1.getFileName())) {
//                    logger.debug("song:" + song.getMetadata().getTitle());
                    assertTrue("Incorrect authors", song.getMetadata().getTitle().equals(song1.getMetadataTitle()));
                } else {
                    logger.debug("line " + song.getMetadata());
                    assertNull("Metadata should not present", song.getMetadata());
                }
                logger.info(song.toString());
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        logger.info("test1PopulatePlaylist done");
    }

    @AfterClass
    public static void testRemoveSong() {

        List<Long> ids = new ArrayList<Long>();
        ids.add(song1.getId());
        songManger.deleteSongsByID(ids);
        songManger.finalize();

        logger.info("AfterClass done");
    }

}
