package cloudTest;

import cloud.Dropbox;
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

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: vanstr
 * Date: 13.17.7
 * Time: 21:58
 * To change this template use File | Settings | File Templates.
 */
public class DropboxTest {

    final static Logger logger = LoggerFactory.getLogger(DropboxTest.class);

    private static Dropbox dropUnAuth = null; // un authorized dropboz session
    private static Dropbox dropAuth = null; // authorized dropboz session

    private static final String CORRECT_FILE_DROPBOX = SystemProperty.getLocalProperties().getProperty("test.dropbox.correct_file");;
    private static final String INCORRECT_FILE_DROPBOX = SystemProperty.getLocalProperties().getProperty("test.dropbox.incorrect_file");;

    @BeforeClass
    public static void method() {
        try {
            dropUnAuth = new Dropbox();

            // ATTENTION user with 1 id should be JUnit user and with access keys
            UserManager manager = new UserManager();
            UserEntity user = manager.getUserById(1);

            dropAuth = new Dropbox(user.getDropboxAccessKey());

            manager.finalize();
        } catch (Exception e) {
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail("error in preparing");
        }
        logger.info("BeforeClass done");

    }

    @Test
    public void testDropboxFail(){

        try{
            new Dropbox(null);
        }catch( Exception e){
            logger.info("testDropboxFail done");
            return;
        }

        fail("testDropbox wrong result with NULL key pair value");
    }




    // We assume that user has requestTokens in DB
    @Test
    public void testGetUserAccessTokens(){

        // 1. User not provided access to his account, -> get exception in Dropbox class
        /*
        Tokens res = dropUnAuth.getRequestTokens();

        Tokens accessTokens = null;
        try {
            accessTokens = dropUnAuth.getUserAccessTokens(res);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        // accessTokens must be null because not provided access via link to account
        assertNull("AccessTokens should be NULL!", accessTokens);
        */
        // 2.User has provided access to his account
        // assume that user has requestTokens in DB
        // TODO: get user requestTokens, provide access to acc via link, retrieve accessTokens, save them to DB and update dropAuth
        // Problem: user should refer to URL and provide access
        /*
        UserManager manager = new UserManager();
        UserEntity user = manager.getUserById(1);

        Tokens res1 = new Tokens(user.getDropboxRequestKey(), user.getDropboxRequestSecret());
        Tokens accessTokens1 = dropAuth.getUserAccessTokens(res1);

        if( accessTokens1 == null ) fail("AccessTokens1 should not be NULL!");
        if( accessTokens1.secret.isEmpty() || accessTokens1.key.isEmpty() ) fail("key or secret is empty");

        user.setDropboxAccessKey(accessTokens1.key);
        user.setDropboxAccessSecret(accessTokens1.secret);
        manager.updateUser(user);
        manager.finalize();
        */
        logger.info("testGetUserAccessTokens done");
    }

    @Test
    public void testGetFileLink(){

        // 1. file exists
        String res = null; // http://dl.dropboxusercontent.com/1/view/n8cbbuw08p669ku/JUnit/music.mp3
        try {
            res = dropAuth.getFileLink(CORRECT_FILE_DROPBOX);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Fail not found");
        }
        if (!res.endsWith(CORRECT_FILE_DROPBOX)) fail("Bad file link:" + res);

        // 2. file doesnt exist
        String res2 = null;
        try {
            res2 = dropAuth.getFileLink(INCORRECT_FILE_DROPBOX);
        } catch (Exception e) {
        }
        assertNull("Bad file link", res2);

        logger.info("testGetFileLink done");
    }

    @Test
    public void testGetFileList(){

        ArrayList<String> fileTypes = new ArrayList<String>();
        fileTypes.add("mp3");

        // 1. should be exception in method because dropbox session not established
        // unworked
        List<Song> res5 = null;
        try {
            res5 = dropUnAuth.getFileList("/", fileTypes);
        } catch (Exception e) {
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        assertNull("Exception", res5);

        // 2.
        List<Song> res = null;
        try {
            res = dropAuth.getFileList("/", fileTypes);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail("Exception");
        }

        // check does searched file presents in music list
        boolean filePresents = false;

        int resSize = res.size();
        for (int i = 0; i < resSize; i++) {
            logger.debug(res.get(i).getFileId());
            if (CORRECT_FILE_DROPBOX.equals(res.get(i).getFileId())) filePresents = true;
        }
        assertTrue("Music file not found", filePresents);

        logger.info("testGetFileList done");
    }


}
