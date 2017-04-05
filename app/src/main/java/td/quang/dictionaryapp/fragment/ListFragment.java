package td.quang.dictionaryapp.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
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

@SuppressLint("ValidFragment")
public class ListFragment extends MyFragment {
    private static final int SPEECH_REQUEST_CODE = 0;

    private final MainActivity activity;
    private AutoCompleteTextView txtSearch;
    private RecyclerView listWord;
    private ListWordAdapter adapter;
    private List<Word> wordList;

    private MyDatabase myDatabase;
    private ArrayList<String> nameTables;
    private String database_name;
    private ArrayAdapter<String> adapterInput;
    private ArrayList<String> historiesInput;

    private Paint p = new Paint();

    public ListFragment(MainActivity activity, String database_name) {
        this.activity = activity;
        this.database_name = database_name;

    }


    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolBar);
        TextView title = (TextView) toolbar.findViewById(R.id.title);

        if (database_name.equalsIgnoreCase(MyDatabase.DATABASE_NAME_ENGVIE))
            title.setText("ENG-VIE");
        if (database_name.equalsIgnoreCase(MyDatabase.DATABASE_NAME_VIEENG))
            title.setText("VIE-ENG");

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
        nameTables = new ArrayList<>();
        nameTables.addAll(myDatabase.getNameAllTable(database_name));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        listWord.setLayoutManager(linearLayoutManager);

        adapterInput = new ArrayAdapter<>(activity,android.R.layout.simple_spinner_dropdown_item,historiesInput);
        txtSearch.setAdapter(adapterInput);

        wordList = myDatabase.getData(database_name, nameTables.get(0), 1, 50);

        adapter = new ListWordAdapter((MainActivity) getActivity(), database_name,null, wordList);
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
                        char nameTable = wordList.get(wordList.size() - 1).getWord().charAt(0);
                        int indexOfNameTable = 0;
                        for (int i = 0; i < nameTables.size(); i++) {
                            if (nameTables.get(i).startsWith(nameTable + "")) {
                                indexOfNameTable = i;
                                break;
                            }
                        }
                        ArrayList<Word> temp = myDatabase.getData(database_name, nameTables.get(indexOfNameTable), idStart, 50);
                        wordList.addAll(temp);
                        if (temp.size() < 50 && (nameTable != 'z')) {
                            temp = myDatabase.getData(database_name, nameTables.get(indexOfNameTable + 1), 1, 50 - temp.size());
                            wordList.addAll(temp);
                        }
                        adapter.notifyDataSetChanged();
                        adapter.setLoaded();
                    }
                }, 100);
            }
        });

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,  ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT){
                  //  adapter.removeItem(position);
                } else {
                  //  adapter.removeItem(position);
                }

            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if(dX > 0){
                        p.setColor(Color.parseColor("#388E3C"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_add_black_24dp);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                        if (dX > 250) {
                            dX = 250;
                            return;
                        }
                    } else {
                        p.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_book_black_24dp);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);

                        if (dX <- 250) {
                            dX = -250;
                            return;
                        }
                    }


                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(listWord);
        listWord.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        historiesInput = new ArrayList<>();
        historiesInput.addAll(myDatabase.getHistories());
        adapterInput = new ArrayAdapter<>(activity,android.R.layout.simple_spinner_dropdown_item,historiesInput);
        txtSearch.setAdapter(adapterInput);
        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String nameTable;

                if (count == 0) {
                    wordList.clear();
                    wordList.addAll(myDatabase.getData(database_name, nameTables.get(0), 1, 50));
                    adapter.notifyDataSetChanged();
                } else {
                    nameTable = charSequence.charAt(0) + "";
                    nameTable = nameTable.trim().toLowerCase();
                    int indexNameTable = 0;
                    for (int i = 0 ;i< nameTables.size();i++){
                        if(nameTables.get(i).startsWith(nameTable)){
                            indexNameTable = i;
                            nameTable = nameTables.get(i);
                            break;

                        }
                    }


                    if (!nameTables.contains(nameTable)) {
                        wordList.clear();
                        wordList.add(new Word("nothing to show", "nham nhi!!!"));
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    ArrayList<Word> temp = myDatabase.findByWord(database_name, nameTables.get(indexNameTable), charSequence.toString());
                    wordList.clear();
                    wordList.addAll(temp);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
//                MyDatabase.getInstance(activity).saveHistory(editable.toString());
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
        if (database_name.equalsIgnoreCase(MyDatabase.DATABASE_NAME_ENGVIE)) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                    "vi");
        }
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
                bundle.putString("database_name", database_name);
                intent.putExtras(bundle);
                getActivity().startActivity(intent);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
