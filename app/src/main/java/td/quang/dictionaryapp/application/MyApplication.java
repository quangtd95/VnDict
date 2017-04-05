package td.quang.dictionaryapp.application;

import android.app.Application;
import android.content.SharedPreferences;

/**
 * Created by Quang_TD on 11/22/2016.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preferences = getSharedPreferences("COUNT_OPEN",0);
        SharedPreferences.Editor editor = preferences.edit();
        int count = preferences.getInt("count",0);
        count++;
        editor.putInt("count",count);
        editor.commit();
    }


}
