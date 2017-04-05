package td.quang.dictionaryapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import td.quang.dictionaryapp.R;
import td.quang.dictionaryapp.database.MyDatabase;

/**
 * Created by Quang_TD on 11/20/2016.
 */
public class SplashActivity extends AppCompatActivity {
    private static final String DATABASE_NAME_ENGVIE = "anh_viet.db";
    private static final String DATABASE_NAME_VIEENG = "viet_anh.db";
    private static final int VERSION = 1;
    private static final String DB_PATH_SUFFIX = "/databases/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Thread(new Runnable() {
            @Override
            public void run() {
                processCopy();
            }
        }).start();

    }

    private void processCopy() {
        File dbFileEng = new File(getDatabasesPath(DATABASE_NAME_ENGVIE));
        File dbFileVie = new File(getDatabasesPath(DATABASE_NAME_VIEENG));

        if (!dbFileEng.exists() || !dbFileVie.exists()) {
            try {
                InputStream inputStreamEng, inputStreamVie;

                inputStreamEng = getAssets().open(DATABASE_NAME_ENGVIE);
                inputStreamVie = getAssets().open(DATABASE_NAME_VIEENG);

                String outFileNameEng = getDatabasesPath(DATABASE_NAME_ENGVIE);
                String outFileNameVie = getDatabasesPath(DATABASE_NAME_VIEENG);

                File f = new File(this.getApplicationInfo().dataDir + DB_PATH_SUFFIX);

                if (!f.exists()) {
                    f.mkdir();
                }
                OutputStream outputStreamEng = new FileOutputStream(outFileNameEng);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStreamEng.read(buffer)) > 0) {
                    outputStreamEng.write(buffer, 0, length);
                }
                outputStreamEng.flush();
                outputStreamEng.close();
                inputStreamEng.close();


                OutputStream outputStreamVie = new FileOutputStream(outFileNameVie);
                while ((length = inputStreamVie.read(buffer)) > 0) {
                    outputStreamVie.write(buffer, 0, length);
                }
                outputStreamVie.flush();
                outputStreamVie.close();
                inputStreamVie.close();
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();

            } catch (Exception ex) {
                Log.e("TAGG", ex.getMessage());
            }

        } else {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();

        }
    }

    private String getDatabasesPath(String database_name) {
        return this.getApplicationInfo().dataDir + DB_PATH_SUFFIX + database_name;
    }


}

