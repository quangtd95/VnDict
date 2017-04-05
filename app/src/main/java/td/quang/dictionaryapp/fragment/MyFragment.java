package td.quang.dictionaryapp.fragment;


import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Quang_TD on 11/20/2016.
 */
public abstract class MyFragment extends Fragment {

    public abstract RecyclerView getRecyclerView();
    public abstract ArrayList<String> getHistoriesInput();
    public abstract ArrayAdapter<String> arrayAdapterInput();
}
