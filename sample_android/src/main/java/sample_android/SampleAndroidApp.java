package sample_android;

import android.app.Application;

import sample_data.Repository;

/**
 * Created by victor on 09/01/16.
 */
public class SampleAndroidApp extends Application {

    private Repository repository;

    @Override public void onCreate() {
        super.onCreate();
        repository = Repository.init(getFilesDir());
    }

    public Repository getRepository() {
        return repository;
    }
}
