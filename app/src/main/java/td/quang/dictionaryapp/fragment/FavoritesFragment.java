package td.quang.dictionaryapp.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import td.quang.dictionaryapp.R;
import td.quang.dictionaryapp.activity.DetailActivity;
import td.quang.dictionaryapp.activity.MainActivity;
import td.quang.dictionaryapp.adapter.ListWordAdapter;
import td.quang.dictionaryapp.database.MyDatabase;
import td.quang.dictionaryapp.interfaces.OnLoadMoreListener;
import td.quang.dictionaryapp.model.Word;

/**
 * Created by Quang_TD on 11/20/2016.
 */
public class FavoritesFragment extends MyFragment {
    private static final int SPEECH_REQUEST_CODE = 0;

    private final Context context;
    private AutoCompleteTextView txtSearch;
    private RecyclerView listWord;
    private ListWordAdapter adapter;
    private List<Word> wordList;

    private MyDatabase myDatabase;
    private ArrayAdapter<String> adapterInput;
    private ArrayList<String> historiesInput;


    @SuppressLint("ValidFragment")
    public FavoritesFragment(Context context) {
        this.context = context;
    }

    public void notifyData(){
        wordList.clear();
        wordList.addAll(myDatabase.getData(MyDatabase.DATABASE_NAME_YOURWORDS,MyDatabase.TABLE_FAVORITES,1,50));
        adapter.notifyDataSetChanged();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolBar);
        TextView title = (TextView) toolbar.findViewById(R.id.title);

        title.setText("FAVORITES");
        View view = inflater.inflate(R.layout.fragment_content, null);
        txtSearch = (AutoCompleteTextView) view.findViewById(R.id.txtSearch);
        listWord = (RecyclerView) view.findViewById(R.id.listWord);

        ImageView btnSearchVoice = (ImageView) view.findViewById(R.id.btnSearchVoice);

        btnSearchVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displaySpeechRecognizer();
            }
        });

        myDatabase = MyDatabase.getInstance(getContext());


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        listWord.setLayoutManager(linearLayoutManager);


        wordList = myDatabase.getData(MyDatabase.DATABASE_NAME_YOURWORDS, MyDatabase.TABLE_FAVORITES, 0, 50);
        adapter = new ListWordAdapter((MainActivity) getActivity(), MyDatabase.DATABASE_NAME_YOURWORDS,MyDatabase.TABLE_FAVORITES, wordList);
        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void loadMore() {
                wordList.add(null);
                adapter.notifyItemInserted(wordList.size() - 1);
                //Load more data for reyclerview
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (wordList.size() < 2) return;
                        wordList.remove(wordList.size() - 1);
                        adapter.notifyItemRemoved(wordList.size());

                        //load more

                        int idStart = wordList.get(wordList.size() - 1).getId();

                        ArrayList<Word> temp = myDatabase.getData(MyDatabase.DATABASE_NAME_YOURWORDS, MyDatabase.TABLE_FAVORITES, idStart+1, 50);
                        wordList.addAll(temp);
                        adapter.notifyDataSetChanged();
                        adapter.setLoaded();
                    }
                }, 100);
            }
        });
        listWord.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        historiesInput = new ArrayList<>();
        historiesInput.addAll(myDatabase.getHistories());
        adapterInput = new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item,historiesInput);
        txtSearch.setAdapter(adapterInput);
        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {


                if (count == 0) {
                    wordList.clear();
                    wordList.addAll(myDatabase.getData(MyDatabase.DATABASE_NAME_YOURWORDS, MyDatabase.TABLE_FAVORITES, 1, 50));
                    adapter.notifyDataSetChanged();
                } else {
                    if (wordList.size() == 0) {
                        wordList.clear();
                        wordList.add(new Word("nothing to show", "nham nhi!!!"));
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    ArrayList<Word> temp = myDatabase.findByWord(MyDatabase.DATABASE_NAME_YOURWORDS, MyDatabase.TABLE_FAVORITES, charSequence.toString());
                    wordList.clear();
                    wordList.addAll(temp);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        return view;
    }

    @Override
    public RecyclerView getRecyclerView() {
        return listWord;
    }

    @Override
    public ArrayList<String> getHistoriesInput() {
        return historiesInput;
    }

    @Override
    public ArrayAdapter<String> arrayAdapterInput() {
        return adapterInput;
    }


    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            // Do something with spokenText
            txtSearch.setText(spokenText);
            if (!wordList.isEmpty()) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("data", wordList.get(0));
                bundle.putString("database_name", MyDatabase.DATABASE_NAME_YOURWORDS);
                bundle.putString("table_name",MyDatabase.TABLE_FAVORITES);
                intent.putExtras(bundle);
                getActivity().startActivity(intent);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
