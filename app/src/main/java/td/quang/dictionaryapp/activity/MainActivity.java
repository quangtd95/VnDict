package td.quang.dictionaryapp.activity;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import td.quang.dictionaryapp.R;
import td.quang.dictionaryapp.Utils.Utilizes;
import td.quang.dictionaryapp.database.MyDatabase;
import td.quang.dictionaryapp.fragment.FavoritesFragment;
import td.quang.dictionaryapp.fragment.ListFragment;
import td.quang.dictionaryapp.fragment.MyFragment;
import td.quang.dictionaryapp.fragment.YourWordsFragment;
import td.quang.dictionaryapp.model.Word;

/**
 * Created by Quang_TD on 11/20/2016.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private MyFragment frEngVie;

    private MyDatabase myDatabase;
    private MyFragment frVieEng;
    private MyFragment frYourWords;
    private MyFragment frFavorites;
    private CoordinatorLayout frContainer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        frEngVie = new ListFragment(this, MyDatabase.DATABASE_NAME_ENGVIE);
        frVieEng = new ListFragment(this, MyDatabase.DATABASE_NAME_VIEENG);
        frYourWords = new YourWordsFragment(this);
        frFavorites = new FavoritesFragment(this);
        setSupportActionBar(toolbar);
        myDatabase = MyDatabase.getInstance(this);
        myDatabase.open();


        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        frContainer = (CoordinatorLayout) findViewById(R.id.frContainer);

        switchFragment(R.id.frContainer, frEngVie);




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO:
                showDialogAddYourWord();
            }
        });

    }

    public void showDialogAddYourWord() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton("Reset", null);
        builder.setNeutralButton("Cancel", null);
        builder.setPositiveButton("Save", null);

        final AlertDialog alertDialog = builder.create();
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_word, null);
        final TextView txtWord = (TextView) view.findViewById(R.id.txtWord);
        final TextView txtMean = (TextView) view.findViewById(R.id.txtMean);
        alertDialog.setView(view);

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button save = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!txtWord.getText().toString().isEmpty() && !txtMean.getText().toString().isEmpty()) {
                            Word word = new Word();
                            word.setWord(txtWord.getText().toString());
                            word.setMean(txtMean.getText().toString());
                            myDatabase.save(MyDatabase.TABLE_YOUR_WORDS, word);

                            if (findFragment(R.id.frContainer) instanceof YourWordsFragment) {
                                ((YourWordsFragment) findFragment(R.id.frContainer)).notifyData();
                            }

                            Snackbar.make(frContainer, "Save to Your Words", Snackbar.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        } else {
                            if (txtMean.getText().toString().isEmpty())
                                txtWord.setError("not null");
                            if (txtMean.getText().toString().isEmpty())
                                txtMean.setError("not null");

                        }

                    }
                });
                Button reset = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                reset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        txtWord.setText("");
                        txtMean.setText("");
                    }
                });

                Button cancel = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

            }
        });
        alertDialog.show();
    }

    public MyFragment findFragment(int container) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        return (MyFragment) fragmentManager.findFragmentById(container);
    }

    public void switchFragment(int container, MyFragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(container, fragment);
        transaction.commit();
    }

    public MyFragment getCurrentFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        return (MyFragment) fragmentManager.findFragmentById(R.id.frContainer);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.optionmenu_main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_eng_vie) {
            switchFragment(R.id.frContainer, frEngVie);

        } else if (id == R.id.nav_eng_eng) {

        } else if (id == R.id.nav_vie_eng) {
            switchFragment(R.id.frContainer, frVieEng);

        } else if (id == R.id.nav_favorite) {
            switchFragment(R.id.frContainer,frFavorites);

        } else if (id == R.id.nav_your_words) {
            switchFragment(R.id.frContainer, frYourWords);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_help) {
            Utilizes.sendEmail(this);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        myDatabase.close();
        super.onDestroy();

    }
}

