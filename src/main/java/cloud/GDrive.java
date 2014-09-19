package cloud;

import cloud.oauth.OAuth2Communicator;
import commons.CloudFile;
import commons.HttpWorker;
import commons.SystemProperty;
import ejb.ContentBeanRemote;
import org.json.JSONArray;
import org.json.JSONObject;
import structure.PlayList;
import structure.Song;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class GDrive extends OAuth2Communicator {

    private static final String GRANT_TYPE_REFRESH = "refresh_token";
    private static final String GRANT_TYPE_AUTHORIZATION = "authorization_code";

    private String accessToken;
    private String refreshToken;
    private Long tokenExpires;

    public GDrive(String accessToken, String refreshToken){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
    
    public GDrive(String accessToken, String refreshToken, Long tokenExpires){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpires = tokenExpires;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getTokenExpires() {
        return tokenExpires;
    }

    public void setTokenExpires(Long tokenExpires) {
        this.tokenExpires = tokenExpires;
    }

    public PlayList getFileList(String folderPath, List<String> fileTypes){
        PlayList files = null;
        try {
            files = retrieveAllFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Iterator<Song> i = files.getSongs().iterator();
        while (i.hasNext()) {
            Song track = i.next();
            if ( !CloudFile.checkFileType(track.getFileName(), fileTypes) ) {
                i.remove();
            }
        }
        return files;
    }


    public PlayList retrieveAllFiles() throws IOException {
        PlayList playList = new PlayList();
        String url = "https://www.googleapis.com/drive/v2/files?oauth_token=" + accessToken;
        JSONObject object = HttpWorker.sendGetRequest(url);
        if(object == null){
            return playList;
        }
        JSONArray fileArray = object.getJSONArray("items");
        for(int i = 0; i < fileArray.length(); i++){
            if(!fileArray.getJSONObject(i).getJSONObject("labels").getBoolean("trashed")
                    && !"application/vnd.google-apps.folder".equals(fileArray.getJSONObject(i).getString("mimeType"))
                    && fileArray.getJSONObject(i).has("title")
                    && fileArray.getJSONObject(i).has("downloadUrl")){

                Song song = new Song(
                        (long)(ContentBeanRemote.DRIVE_CLOUD_ID),
                        fileArray.getJSONObject(i).getString("id"),
                        fileArray.getJSONObject(i).getString("title"),
                        fileArray.getJSONObject(i).getString("downloadUrl") + "&oauth_token=" + this.accessToken,
                        this.tokenExpires
                );
                playList.add(song);
            }
        }
        return playList;
    }

    public String getFileLink(String fileId){
        JSONObject object = HttpWorker.sendGetRequest(SystemProperty.DRIVE_FILES_URL
                + fileId + "?oauth_token=" + this.accessToken);
        String fileSrc = object.getString("downloadUrl") + "&oauth_token=" + this.accessToken;
        return fileSrc;
    }

    @Override
    public OAuth2UserData retrieveAccessToken(String code){
        JSONObject object = super.retrieveAccessToken(code, SystemProperty.DRIVE_CLIENT_ID,
                SystemProperty.DRIVE_CLIENT_SECRET, GRANT_TYPE_AUTHORIZATION, SystemProperty.DRIVE_REDIRECT_URI,
                SystemProperty.DRIVE_EMAIL_SCOPE + "+" + SystemProperty.DRIVE_SCOPE, SystemProperty.DRIVE_TOKEN_URL);
        OAuth2UserData oAuth2UserData = parseDriveData(object);
        return oAuth2UserData;
    }

    @Override
    public String refreshToken(String refreshToken){
        String accessToken = null;
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("client_id", SystemProperty.DRIVE_CLIENT_ID);
            params.put("client_secret", SystemProperty.DRIVE_CLIENT_SECRET);
            params.put("grant_type", GRANT_TYPE_REFRESH);
            params.put("refresh_token", refreshToken);
            JSONObject object = HttpWorker.sendPostRequest(SystemProperty.DRIVE_TOKEN_URL, params);
            accessToken = object.getString("access_token");
            this.tokenExpires = object.getLong("expires_in")*1000 + System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accessToken;
    }

    public static OAuth2UserData parseDriveData(JSONObject jsonObject){
        OAuth2UserData oAuth2UserData = new OAuth2UserData();
        oAuth2UserData.setAccessToken(jsonObject.getString("access_token"));
        oAuth2UserData.setRefreshToken(jsonObject.getString("refresh_token"));
        oAuth2UserData.setExpiresIn(jsonObject.getInt("expires_in"));
        JSONObject object = HttpWorker.sendGetRequest(SystemProperty.DRIVE_EMAIL_URL + "?alt=json&oauth_token="
                + oAuth2UserData.getAccessToken());
        String email = object.getJSONObject("data").get("email").toString();
        oAuth2UserData.setUniqueCloudId(email);
        return oAuth2UserData;
    }
}
