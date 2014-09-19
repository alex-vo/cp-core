package persistenceTest;

import cloud.Dropbox;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.SongEntity;
import persistence.UserEntity;
import persistence.utility.SongManager;
import persistence.utility.UserManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: vanstr
 * Date: 14.19.2
 * Time: 20:12
 * To change this template use File | Settings | File Templates.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SongTest {

    final static Logger logger = LoggerFactory.getLogger(SongTest.class);


    private static Dropbox dropUnAuth = null; // un authorized dropboz session
    private static Dropbox dropAuth = null; // authorized dropboz session
    private static UserManager userManager = null;
    private static UserEntity user  = null;
    private static SongManager songManger = null;

    private static SongEntity song1 = null;

    @BeforeClass
    public static void method() {
        try {
            dropUnAuth = new Dropbox();

            // ATTENTION user with 1 id should be JUnit user and with access keys
            userManager = new UserManager();
            songManger = new SongManager();

            user = userManager.getUserById(1);



            dropAuth = new Dropbox(user.getDropboxAccessKey());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail("error in preparing");
        }

        logger.info("BeforeClass done");

    }

    @Test
    public void test1SaveSongs(){
        song1 = new SongEntity();
        song1.setUser(user);
        song1.setCloudId(1);
        song1.setFileId("Saga.mp3");
        song1.setFileName("Saga.mp3");
        song1.setFileSize(6666666);
        song1.setMetadataTitle("Basldlsa dasdas");
        song1.setHasMetadata(true);
        assertTrue(songManger.addSong(song1));

        logger.info("test1SaveSongs done");

    }

    @Test
    public void test2GetSongs(){

        Map<String, Object> whereClause = new HashMap<String, Object>();
        whereClause.put("id", song1.getId());
        //whereClause.put("user", user);
        List<SongEntity> list = songManger.getSongsByFields(whereClause);
        logger.debug("list: " + list);
        assertNotNull(list);

        logger.info("test2GetSongs done");
    }

    @Test
    public void test3RemoveSongsById(){

        List<Long> ids = new ArrayList<Long>();
        ids.add(song1.getId());
        boolean res = songManger.deleteSongsByID(ids);
        assertTrue(res);


        Map<String, Object> whereClause = new HashMap<String, Object>();
        whereClause.put("id", song1.getId());
        List<SongEntity> list = songManger.getSongsByFields(whereClause);
        assertNull("Created song not removed",list);

        logger.info("test3RemoveSongsById done");
    }


    @AfterClass
    public static void end() {
        userManager.finalize();
        songManger.finalize();
        logger.info("AfterClass done");
    }
}
