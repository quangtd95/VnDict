package td.quang.dictionaryapp.adapter;

import android.graphics.Bitmap;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import td.quang.dictionaryapp.R;
import td.quang.dictionaryapp.Utils.Utilizes;
import td.quang.dictionaryapp.activity.DetailActivity;
import td.quang.dictionaryapp.database.MyDatabase;
import td.quang.dictionaryapp.model.Word;

/**
 * Created by Quang_TD on 11/20/2016.
 */
public class ViewPagerDetailAdapter extends PagerAdapter {
    private List<Word> wordList;
    private DetailActivity activity;
    private String database_name;

    public ViewPagerDetailAdapter(DetailActivity activity, String database_name, List<Word> wordList) {
        this.activity = activity;
        this.database_name = database_name;
        this.wordList = wordList;

    }

    @Override
    public int getCount() {
        return (wordList == null) ? 0 : wordList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        final Word word = wordList.get(position);
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        final View view = layoutInflater.inflate(R.layout.content_viewpager_detail, container, false);
        ImageButton btnTTS = (ImageButton) view.findViewById(R.id.btnTTS);

        btnTTS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.getTextToSpeech().speak(word.getWord(), TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        final WebView webView = (WebView) view.findViewById(R.id.viewContent);
        final String mimeType = "text/html";
        final String encoding = "UTF-8";

        final String html = Utilizes.FormatHTML(word.getMean());


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!url.equalsIgnoreCase("about:blank")) {
                    String link = url.substring(8);
                    Word ww = new Word();
                    ww.setWord(link);
                    String table_name = Utilizes.getTableName(activity, database_name, ww);
                    ArrayList<Word> list = MyDatabase.getInstance(activity).findByWord(database_name, table_name, link);
                    if (list.size() != 0) {
                        wordList.clear();
                        wordList.addAll(Utilizes.loadDataToViewPager(activity, database_name, table_name,list.get(0)));
                        notifyDataSetChanged();
                        activity.getViewPager().setCurrentItem(Utilizes.currentPositionOfViewPager,true);
                        final String html = Utilizes.FormatHTML(wordList.get(Utilizes.currentPositionOfViewPager).getMean());
                        view.loadDataWithBaseURL("", html, mimeType, encoding, "");
                    }

                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {

            }
        });
        webView.loadDataWithBaseURL("", html, mimeType, encoding, "");
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
