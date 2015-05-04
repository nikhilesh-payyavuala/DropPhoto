package com.session.dropphoto;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

import java.util.ArrayList;

/**
 * Created by Nikhilesh on 5/4/15.
 */
public class UserSingleton {
    static DropboxAPI<AndroidAuthSession> mApi;
    static UserSingleton user;
    ArrayList<String> paths;

    public ArrayList<String> getPaths() {
        return paths;
    }

    public void setPaths(ArrayList<String> paths) {
        this.paths = paths;
    }

    public DropboxAPI<AndroidAuthSession> getmApi() {
        return mApi;
    }

    public void setmApi(DropboxAPI<AndroidAuthSession> mApi) {
        this.mApi = mApi;
    }

    public static UserSingleton getInstance(){
        if(user==null){
            user = new UserSingleton();
            return user;
        }
        else{
            return user;
        }

    }
}
