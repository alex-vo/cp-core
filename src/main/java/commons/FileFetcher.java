package commons;

import structure.Song;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vanstr
 * Date: 14.25.2
 * Time: 19:22
 * To change this template use File | Settings | File Templates.
 */
public abstract class FileFetcher implements Runnable{

    public final static List<String> REQUIRED_FILE_TYPES = Arrays.asList("mp3", "wav", "ogg");

    protected String folderPath;
    protected Long userId;
    protected List<Song> files;

    public FileFetcher(String folderPath, Long userId){
        this.folderPath = folderPath;
        this.userId = userId;
    }

    public List<Song> getFiles() {
        return files;
    }

    @Override
    public void run(){
        files = getCloudFiles(folderPath, userId);
    }

    public abstract List<Song> getCloudFiles(String folderPath, Long userId);
}