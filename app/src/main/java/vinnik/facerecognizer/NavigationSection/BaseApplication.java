package vinnik.facerecognizer.NavigationSection;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by vinnik on 03.03.2017.
 */

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
