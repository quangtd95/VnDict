package td.quang.dictionaryapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import td.quang.dictionaryapp.R;
import td.quang.dictionaryapp.Utils.Utilizes;
import td.quang.dictionaryapp.activity.DetailActivity;
import td.quang.dictionaryapp.activity.MainActivity;
import td.quang.dictionaryapp.database.MyDatabase;
import td.quang.dictionaryapp.fragment.YourWordsFragment;
import td.quang.dictionaryapp.interfaces.OnLoadMoreListener;
import td.quang.dictionaryapp.model.Word;

/**
 * Created by Quang_TD on 11/20/2016.
 */
public class ListWordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private String database_name;
    private String table_name;

    private OnLoadMoreListener onLoadMoreListener;

    private MainActivity activity;
    private List<Word> wordList;
    private int totalItemCount;
    private int lastVisibleItem;
    private boolean isLoading;
    private int visibleThreshold = 20;

    public void setLoaded() {
        isLoading = false;
    }

    /*
    if database = !YOurwords.
    table_name  may null;
     */
    public ListWordAdapter(final MainActivity activity, String database_name,String table_name, List<Word> wordList) {

        this.activity = activity;
        this.wordList = wordList;
        this.database_name = database_name;
        this.table_name = table_name;

        RecyclerView recyclerView = activity.getCurrentFragment().getRecyclerView();
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount < visibleThreshold + lastVisibleItem) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.loadMore();
                    }
                    isLoading = true;
                }
            }
        });


    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }
    public void addItem(Word word) {
        wordList.add(word);
        notifyItemInserted(wordList.size());
    }

    public void removeItem(int position) {
        wordList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, wordList.size());
    }

    @Override
    public int getItemViewType(int position) {
        return (wordList.get(position) != null) ? VIEW_TYPE_ITEM : VIEW_TYPE_LOADING;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.word_item_main, parent, false);
            return new ItemViewHolder(view);
        }
        if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(activity).inflate(R.layout.word_loading_main, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            final Word word = wordList.get(position);
            itemViewHolder.txtWord.setText(word.getWord());
            final String mean = Utilizes.getTitleFromMean(word.getMean());
            itemViewHolder.txtMean.setText(mean);
            itemViewHolder.cardViewWord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity, DetailActivity.class);
                    Bundle bundle = new Bundle();
                    if (database_name.equalsIgnoreCase(MyDatabase.DATABASE_NAME_YOURWORDS)){
                        bundle.putString("table_name",table_name);
                    }
                    else {
                        bundle.putString("table_name",Utilizes.getTableName(activity,database_name,word));
                    }
                    bundle.putParcelable("data", word);
                    bundle.putString("database_name", database_name);


                    intent.putExtras(bundle);
                    activity.startActivity(intent);

                    if (MyDatabase.getInstance(activity).checkHasInHistory(word.getWord())== false){
                        MyDatabase.getInstance(activity).saveHistory(word.getWord());
                        activity.getCurrentFragment().getHistoriesInput().add(word.getWord());
                        activity.getCurrentFragment().arrayAdapterInput().notifyDataSetChanged();
                        notifyDataSetChanged();
                    }

                }
            });


            if (database_name.equalsIgnoreCase(MyDatabase.DATABASE_NAME_YOURWORDS)){
                itemViewHolder.cardViewWord.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        MyDatabase myDatabase = MyDatabase.getInstance(activity);
                        myDatabase.delete(table_name,word);
                        removeItem(position);
                        return true;
                    }
                });
            }


        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return (wordList == null) ? 0 : wordList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        CardView cardViewWord;
        TextView txtWord;
        TextView txtMean;

        public ItemViewHolder(View itemView) {
            super(itemView);
            cardViewWord = (CardView) itemView.findViewById(R.id.cardViewWord);
            txtWord = (TextView) itemView.findViewById(R.id.txtWord);
            txtMean = (TextView) itemView.findViewById(R.id.txtMean);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBarLoading);

        }
    }


}
