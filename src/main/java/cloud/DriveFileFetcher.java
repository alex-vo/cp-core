package cloud;

import com.sun.servicetag.UnauthorizedAccessException;
import commons.FileFetcher;
import persistence.UserEntity;
import persistence.utility.UserManager;
import structure.Song;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vanstr
 * Date: 14.25.2
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */
public class DriveFileFetcher extends FileFetcher{

    public DriveFileFetcher(String folderPath, Long userId) {
        super(folderPath, userId);
    }

    public List<Song> getCloudFiles(String folderPath, Long userId){
        List<Song> files = null;
        GDrive gDrive = null;

        UserManager manager = new UserManager();
        UserEntity user = manager.getUserById(userId);
        try{
            String driveAccessToken = user.getDriveAccessToken();
            String driveRefreshToken = user.getDriveRefreshToken();
            if(driveAccessToken != null && driveRefreshToken != null){
                gDrive = new GDrive(driveAccessToken, driveRefreshToken);
                files = gDrive.getFileList(folderPath, REQUIRED_FILE_TYPES).getSongs();
            }
        } catch (UnauthorizedAccessException e) {
            if("401".equals(e.getMessage())){
                gDrive.setAccessToken(gDrive.refreshToken(gDrive.getRefreshToken()));
                try {
                    files = gDrive.getFileList(folderPath, REQUIRED_FILE_TYPES).getSongs();
                    user.setDriveAccessToken(gDrive.getAccessToken());
                    manager.updateUser(user);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            manager.finalize();
        }
        return files;
    }

}