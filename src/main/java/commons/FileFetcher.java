package commons;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vanstr
 * Date: 14.25.2
 * Time: 19:22
 * To change this template use File | Settings | File Templates.
 */
//TODO: refactor
public abstract class FileFetcher implements Runnable{

    public final static List<String> REQUIRED_FILE_TYPES = Arrays.asList("mp3", "wav", "ogg");

    protected String folderPath;
    protected Long userId;
    protected List<String[]> files;

    public FileFetcher(String folderPath, Long userId){
        this.folderPath = folderPath;
        this.userId = userId;
    }

    public List<String[]> getFiles() {
        return files;
    }

    @Override
    public void run(){
        files = getCloudFiles(folderPath, userId);
    }

    public abstract List<String[]> getCloudFiles(String folderPath, Long userId);
}