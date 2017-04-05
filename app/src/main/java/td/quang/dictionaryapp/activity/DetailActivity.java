package td.quang.dictionaryapp.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import td.quang.dictionaryapp.R;
import td.quang.dictionaryapp.Utils.Utilizes;
import td.quang.dictionaryapp.adapter.ViewPagerDetailAdapter;
import td.quang.dictionaryapp.database.MyDatabase;
import td.quang.dictionaryapp.model.Word;

/**
 * Created by Quang_TD on 11/20/2016.
 */
public class DetailActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private static TextToSpeech textToSpeech;
    private FloatingActionButton fab;
    private Word word;

    private int positionOfViewPager = 0;
    private MyDatabase myDatabase;

    public TextToSpeech getTextToSpeech() {
        return textToSpeech;
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        myDatabase = MyDatabase.getInstance(DetailActivity.this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView textView = (TextView) toolbar.findViewById(R.id.title);
        Bundle bundle = getIntent().getExtras();

        word = bundle.getParcelable("data");
        final String database_name = bundle.getString("database_name");
        final String table_name = bundle.getString("table_name");

        textView.setText(word.getWord());

        final ArrayList<Word> wordList = Utilizes.loadDataToViewPager(this, database_name,table_name, word);

        positionOfViewPager = Utilizes.currentPositionOfViewPager;


        fab = (FloatingActionButton) findViewById(R.id.fab);

        if (!database_name.equalsIgnoreCase(MyDatabase.DATABASE_NAME_YOURWORDS)) {
            if (myDatabase.wordIsFavorited(database_name, table_name, word)) {
                fab.setImageDrawable(getDrawable(R.drawable.ic_thumb_down_black_24dp));
            } else {
                fab.setImageDrawable(getDrawable(R.drawable.ic_thumb_up_black_24dp));
            }
        }

        if (textToSpeech == null){
            textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onInit(int status) {
                    if (status != TextToSpeech.ERROR) {
                        if (database_name.equalsIgnoreCase(MyDatabase.DATABASE_NAME_ENGVIE)) {
                            textToSpeech.setLanguage(Locale.US);
                        } else if (database_name.equalsIgnoreCase(MyDatabase.DATABASE_NAME_VIEENG)) {
                            textToSpeech.setLanguage(Locale.forLanguageTag("vi"));
                        }

                    } else {
                        Log.e("TAGG", "init false");
                    }

                }
            });
        }

        viewPager = (ViewPager) findViewById(R.id.viewPagerDetail);

        final ViewPagerDetailAdapter viewPagerDetailAdapter = new ViewPagerDetailAdapter(this, database_name, wordList);

        viewPager.setAdapter(viewPagerDetailAdapter);

        viewPager.setCurrentItem(positionOfViewPager, false);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {


            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }


            @Override
            public void onPageSelected(int position) {
                try {
                    positionOfViewPager = position;
                    textView.setText(wordList.get(position).getWord());
                    if (!database_name.equalsIgnoreCase(MyDatabase.DATABASE_NAME_YOURWORDS)) {
                        if (myDatabase.wordIsFavorited(database_name, table_name, wordList.get(position))) {
                            fab.setImageDrawable(getDrawable(R.drawable.ic_thumb_down_black_24dp));
                        } else {
                            fab.setImageDrawable(getDrawable(R.drawable.ic_thumb_up_black_24dp));
                        }
                    }
                    Log.i("TAGG","onPageSelected"+textView.getText().toString());
                } catch (NullPointerException e) {
                    Log.e("TAGG", e.toString());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        viewPagerDetailAdapter.notifyDataSetChanged();



        if (!database_name.equalsIgnoreCase(MyDatabase.DATABASE_NAME_YOURWORDS)) {


            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (myDatabase.wordIsFavorited(database_name,table_name,wordList.get(positionOfViewPager))) {
                        fab.setImageDrawable(getDrawable(R.drawable.ic_thumb_up_black_24dp));
                        myDatabase.setFavorite(database_name, table_name, wordList.get(positionOfViewPager),false);
                    } else {
                        fab.setImageDrawable(getDrawable(R.drawable.ic_thumb_down_black_24dp));
                        myDatabase.setFavorite(database_name, table_name, wordList.get(positionOfViewPager),true);
                    }

                }
            });

        } else fab.setVisibility(View.GONE);


    }

}
