package td.quang.dictionaryapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;

import td.quang.dictionaryapp.R;
import td.quang.dictionaryapp.Utils.Utilizes;
import td.quang.dictionaryapp.model.Word;

/**
 * Created by Quang_TD on 11/19/2016.
 */
public class MyDatabase {
    public static final String DATABASE_NAME_ENGVIE = "anh_viet.db";
    public static final String DATABASE_NAME_VIEENG = "viet_anh.db";
    public static final String DATABASE_NAME_YOURWORDS = "YOUR_WORDS.db";

    public static final String TABLE_YOUR_WORDS = "your_words";
    public static final String TABLE_FAVORITES = "favorites";
    public static final String TABLE_HISTORIES = "histories";
    private static final int VERSION = 1;
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_WORD = "Tu";
    private static final String COLUMN_MEAN = "Nghia";
    private static final String COLUMN_NOTE = "Ghichu";

    private static MyDatabase instance;
    private static SQLiteDatabase dbEng, dbVie, dbYourWords;
    private Context context;
    private OpenHelper openHelperEng, openHelperVie, openHelperYourWords;

    public MyDatabase(Context context) {
        this.context = context;

    }

    public static MyDatabase getInstance(Context context) {
        if (instance == null) instance = new MyDatabase(context);
        return instance;
    }

    public void open() {
        openHelperEng = new OpenHelper(context, DATABASE_NAME_ENGVIE, null, VERSION);
        openHelperVie = new OpenHelper(context, DATABASE_NAME_VIEENG, null, VERSION);
        openHelperYourWords = new OpenHelper(context, DATABASE_NAME_YOURWORDS, null, VERSION);
        dbEng = openHelperEng.getWritableDatabase();
        dbVie = openHelperVie.getWritableDatabase();
        dbYourWords = openHelperYourWords.getWritableDatabase();

    }

    public void close() {
        openHelperEng.close();
        openHelperVie.close();
        openHelperYourWords.close();
    }

    public boolean checkHasInHistory(String s) {
        ArrayList<Word> words = findByWord(DATABASE_NAME_YOURWORDS, TABLE_HISTORIES, s);
        if (words.size() > 0) return true;
        else return false;
    }

    public void saveHistory(String s) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, 0);
        contentValues.put(COLUMN_WORD, s);
        contentValues.put(COLUMN_MEAN,"");
        contentValues.put(COLUMN_NOTE,"");
        dbYourWords.insert(TABLE_HISTORIES, null, contentValues);
    }

    private void moveDataFromCursorToArray(Cursor cursor, ArrayList<Word> words) {
        int iID = cursor.getColumnIndex(COLUMN_ID);
        int iWord = cursor.getColumnIndex(COLUMN_WORD);
        int iMean = cursor.getColumnIndex(COLUMN_MEAN);
        int iNote = cursor.getColumnIndex(COLUMN_NOTE);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Word word = new Word();
            word.setId(Integer.parseInt(cursor.getString(iID)));
            word.setWord(cursor.getString(iWord));
            word.setMean(cursor.getString(iMean));
            word.setNote(cursor.getString(iNote));
            if (word.getNote() == null) word.setNote("");
            words.add(word);
        }
        cursor.close();

    }

    public boolean wordIsFavorited(String database_name, String table, Word word) {
        if (findById(database_name, table, word.getId()).getNote().equalsIgnoreCase("favorite"))
            return true;
        else return false;
    }

    public void setFavorite(String database_name, String table_name, Word word, boolean b) {

        if (b) {
            ContentValues contentValues = new ContentValues();
            String note = "favorite";
            contentValues.put(COLUMN_NOTE, note);
            if (database_name.equalsIgnoreCase(MyDatabase.DATABASE_NAME_ENGVIE)) {
                dbEng.update(table_name, contentValues, COLUMN_WORD + " = '" + word.getWord() + "'", null);

            }
            if (database_name.equalsIgnoreCase(MyDatabase.DATABASE_NAME_VIEENG)) {
                dbVie.update(table_name, contentValues, COLUMN_WORD + " = '" + word.getWord() + "'", null);
            }
            word.setNote(note);
            save(TABLE_FAVORITES, word);
        } else {
            word.setNote("");
            delete(TABLE_FAVORITES, word);
        }

    }

    public void delete(String table_name, Word word) {
        if (table_name.equalsIgnoreCase(MyDatabase.TABLE_YOUR_WORDS)) {
            dbYourWords.delete(table_name, COLUMN_WORD + " = '" + word.getWord() + "'", null);

        } else {
            dbYourWords.delete(table_name, COLUMN_WORD + " = '" + word.getWord() + "' AND " + COLUMN_ID + " = " + word.getId(), null);
            // setfavorite = false in database dict.
            String database_name;
            String table_name_source = Utilizes.getTableName(context, DATABASE_NAME_ENGVIE, word);
            if (findById(DATABASE_NAME_ENGVIE, table_name_source, word.getId()).getWord().equalsIgnoreCase(word.getWord())) {
                database_name = DATABASE_NAME_ENGVIE;
            } else database_name = DATABASE_NAME_VIEENG;

            ContentValues contentValues = new ContentValues();
            String note = "";
            contentValues.put(COLUMN_NOTE, note);

            if (database_name.equalsIgnoreCase(MyDatabase.DATABASE_NAME_ENGVIE)) {
                dbEng.update(table_name_source, contentValues, COLUMN_WORD + " = '" + word.getWord() + "'", null);

            }
            if (database_name.equalsIgnoreCase(MyDatabase.DATABASE_NAME_VIEENG)) {
                dbVie.update(table_name_source, contentValues, COLUMN_WORD + " = '" + word.getWord() + "'", null);
            }
        }
    }

    public void save(String table_name, Word word) {
        ContentValues contentValues = new ContentValues();
        if (table_name.equalsIgnoreCase(MyDatabase.TABLE_FAVORITES)) {
            contentValues.put(COLUMN_ID, word.getId());
        }
        contentValues.put(COLUMN_WORD, word.getWord());
        contentValues.put(COLUMN_MEAN, word.getMean());
        contentValues.put(COLUMN_NOTE, word.getNote());
        dbYourWords.insert(table_name, null, contentValues);

    }

    public ArrayList<String> getNameAllTable(String database_name) {
        ArrayList<String> names = new ArrayList<>();
        Cursor cursor;
        String sqlStatement = "SELECT name FROM sqlite_master WHERE type='table'";
        switch (database_name) {
            case DATABASE_NAME_ENGVIE:
                cursor = dbEng.rawQuery(sqlStatement, null);
                break;
            case DATABASE_NAME_VIEENG:
                cursor = dbVie.rawQuery(sqlStatement, null);
                break;
            default:
                return null;

        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            if (!cursor.getString(0).equalsIgnoreCase("android_metadata"))
                names.add(cursor.getString(0));
        }
        Collections.sort(names);
        cursor.close();
        return names;
    }

    public Word findById(String database_name, String table_name, int id) {
        Cursor cursor;
        String sqlStatement = "SELECT * FROM " + table_name + " WHERE " + COLUMN_ID + " = " + id;
        switch (database_name) {
            case DATABASE_NAME_ENGVIE:
                cursor = dbEng.rawQuery(sqlStatement, null);
                break;
            case DATABASE_NAME_VIEENG:
                cursor = dbVie.rawQuery(sqlStatement, null);
                break;
            case DATABASE_NAME_YOURWORDS:
                cursor = dbYourWords.rawQuery(sqlStatement, null);
                break;
            default:
                return null;

        }
        int iID = cursor.getColumnIndex(COLUMN_ID);
        int iWord = cursor.getColumnIndex(COLUMN_WORD);
        int IMean = cursor.getColumnIndex(COLUMN_MEAN);
        int INote = cursor.getColumnIndex(COLUMN_NOTE);
        cursor.moveToFirst();
        Word word = new Word();
        word.setId(cursor.getInt(iID));
        word.setWord(cursor.getString(iWord));
        word.setMean(cursor.getString(IMean));
        word.setNote(cursor.getString(INote));
        if (word.getNote() == null) word.setNote("");
        cursor.close();
        return word;
    }

    public ArrayList<Word> findByWord(String database_name, String table_name, String word) {
        ArrayList<Word> words = new ArrayList<>();
        Cursor cursor;
        table_name = table_name.trim().toLowerCase();
        word = word.trim().toLowerCase();
        String sqlStatement = "SELECT * FROM " + table_name + " WHERE " + COLUMN_WORD + " LIKE '" + word + "%' LIMIT " + 50;
        switch (database_name) {
            case DATABASE_NAME_ENGVIE:
                cursor = dbEng.rawQuery(sqlStatement, null);
                break;
            case DATABASE_NAME_VIEENG:
                cursor = dbVie.rawQuery(sqlStatement, null);
                break;
            case DATABASE_NAME_YOURWORDS:
                cursor = dbYourWords.rawQuery(sqlStatement, null);
                break;
            default:
                return null;
        }
        moveDataFromCursorToArray(cursor, words);
        return words;
    }

    public ArrayList<Word> getData(String database_name, String table_name, int start, int num) {
        ArrayList<Word> words = new ArrayList<>();
        Cursor cursor;
        String sqlStatement = "SELECT * FROM " + table_name + " WHERE " + COLUMN_ID + " >= " + start + " ORDER BY " + COLUMN_ID + " LIMIT " + num;
        switch (database_name) {
            case DATABASE_NAME_ENGVIE:
                cursor = dbEng.rawQuery(sqlStatement, null);
                break;
            case DATABASE_NAME_VIEENG:
                cursor = dbVie.rawQuery(sqlStatement, null);
                break;
            case DATABASE_NAME_YOURWORDS:
                cursor = dbYourWords.rawQuery(sqlStatement, null);
                break;
            default:
                return null;

        }
        moveDataFromCursorToArray(cursor, words);
        return words;
    }

    public ArrayList<String> getHistories() {
        ArrayList<String> words = new ArrayList<>();
        Cursor cursor;
        String sqlStatement = "SELECT * FROM " + TABLE_HISTORIES + " ORDER BY " + COLUMN_WORD + " LIMIT 50";
        cursor = dbYourWords.rawQuery(sqlStatement, null);
        int iWord = cursor.getColumnIndex(COLUMN_WORD);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            words.add(cursor.getString(iWord));
        }
        return words;
    }


    /*****************************************************/
    static class OpenHelper extends SQLiteOpenHelper {


        private String database_name;

        public OpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            this.database_name = name;
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            if (database_name.equalsIgnoreCase(DATABASE_NAME_YOURWORDS)) {
                sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS "
                        + TABLE_YOUR_WORDS + " ("
                        + COLUMN_ID + " INTEGER PRIMARY KEY, "
                        + COLUMN_WORD + " TEXT NOT NULL,"
                        + COLUMN_MEAN + " TEXT NOT NULL,"
                        + COLUMN_NOTE + " TEXT);");
                sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS "
                        + TABLE_FAVORITES + " ("
                        + COLUMN_ID + " INTEGER, "
                        + COLUMN_WORD + " TEXT NOT NULL,"
                        + COLUMN_MEAN + " TEXT NOT NULL,"
                        + COLUMN_NOTE + " TEXT);");
                sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS "
                        + TABLE_HISTORIES + " ("
                        + COLUMN_ID + " INTEGER, "
                        + COLUMN_WORD + " TEXT NOT NULL,"
                        + COLUMN_MEAN + " TEXT,"
                        + COLUMN_NOTE + " TEXT);");


            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }
}
