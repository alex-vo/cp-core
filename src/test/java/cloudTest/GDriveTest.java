package cloudTest;

import cloud.GDrive;
import commons.SystemProperty;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.UserEntity;
import persistence.utility.UserManager;
import structure.Song;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: alex
 * Date: 6/22/14
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class GDriveTest {

    static final Logger logger = LoggerFactory.getLogger(GDriveTest.class);
    private static String INCORRECT_FILE = SystemProperty.getLocalProperties().getProperty("test.drive.incorrect_file");
    private static String FILE_ID = SystemProperty.getLocalProperties().getProperty("test.drive.correct_file_id");
    private static GDrive gDrive = null;

    @BeforeClass
    public static void method() {
        try{
            UserManager manager = new UserManager();
            UserEntity user = manager.getUserById(2);
            gDrive = new GDrive(user.getDriveAccessToken(), user.getDriveRefreshToken());
            String newToken = gDrive.refreshToken(user.getDriveRefreshToken());
            gDrive.setAccessToken(newToken);
            user.setDriveAccessToken(newToken);
            manager.updateUser(user);
            manager.finalize();
        }catch (Exception e){
            logger.warn("Initialization failed", e);
            fail("Initialization failed");
        }
        logger.info("BeforeClass done");
    }

    @Test
    public void testGetFileList(){
        List<String> fileTypes = new ArrayList<String>();
        fileTypes.add("mp3");
        try {
            List<Song> songList = gDrive.getFileList("/", fileTypes).getSongs();
            assertNotNull(songList);
            boolean isFilePresent = false;
            for(Song song : songList){
                if(FILE_ID.equals(song.getFileId())){
                    isFilePresent = true;
                }
            }
            assertTrue(isFilePresent);
        }catch (Exception e){
            logger.warn("getFileList error", e);
            fail("Error getting file list");
        }
        logger.info("testGetFileList done");
    }

    @Test
    public void testGetFileLink(){
        try{
            String correctLink = gDrive.getFileLink(FILE_ID);
            assertNotNull("File not found", correctLink);
            assertTrue("File is not correct", correctLink.contains(FILE_ID));
        }catch (Exception e){
            logger.warn("getFileLink error", e);
            fail("Error getting file link");
        }

        String incorrectLink = null;
        try{
            incorrectLink = gDrive.getFileLink(INCORRECT_FILE);
        }catch (Exception e){
            assertNull("Incorrect file link", incorrectLink);
        }
        logger.info("testGetFileLink done");
    }


}
