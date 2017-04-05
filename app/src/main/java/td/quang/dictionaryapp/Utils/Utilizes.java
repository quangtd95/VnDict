package td.quang.dictionaryapp.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import java.util.ArrayList;

import td.quang.dictionaryapp.database.MyDatabase;
import td.quang.dictionaryapp.model.Word;

/**
 * Created by Quang_TD on 11/20/2016.
 */
public class Utilizes {
    public static int currentPositionOfViewPager;

    public static void sendEmail(Context context){
        SharedPreferences preferences = context.getSharedPreferences("COUNT_OPEN",0);
        int count = preferences.getInt("count",0);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:quang.td95@gmail.com"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, "quang.td95@gmail.com");
        emailIntent.putExtra(Intent.EXTRA_CC, "quang.td95@gmail.com");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Number of times user has opened the Application");
        emailIntent.putExtra(Intent.EXTRA_TEXT, count+"");
        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    public static String getTableName(Context context,String database_name,Word word){
        MyDatabase myDatabase = MyDatabase.getInstance(context);
        ArrayList<String> nameTables = myDatabase.getNameAllTable(database_name);
        String name = word.getWord().charAt(0)+"";
        for (int i = 0 ; i < nameTables.size();i++){
            if (nameTables.get(i).startsWith(name)){
                name = nameTables.get(i);
                break;
            }
        }
        return name;
    }

    public static String getTitleFromMean(String mean){
        String s = mean;
        int i1 = s.indexOf("<li>");
        int i2 = s.indexOf("</li>");
        int i3 = s.indexOf("<ul>", i1 + 4);
        if (i2 <= 0) i2 = i3;
        if (i2 > i3 && i3 >= 0) i2 = i3;
        if (i2 != -1)
            s = s.substring(i1 + 4, i2);
        return s;
    }

    public static String FormatHTML(String content) {
        StringBuilder stringBuilder = new StringBuilder(content);
        int index = 0;
        index = stringBuilder.indexOf("class=title", index);
        while (index != -1) {
            stringBuilder.insert(index + 11, " style=\"color: green ; font-size: 20px\"");
            index = stringBuilder.indexOf("class=title", index + 1);

        }

        index = 0;
        index = stringBuilder.indexOf("class=type", index);
        while (index != -1) {
            stringBuilder.insert(index + 10, " style=\"color:red;font-size: 20px\"");

            index = stringBuilder.indexOf("class=type", index + 1);
        }

        index = 0;
        index = stringBuilder.indexOf("<li>", index);
        while (index != -1) {
            stringBuilder.insert(index + 3, " style=\"color: grey; font-size: 18px\"");
            index = stringBuilder.indexOf("<li>", index + 1);
        }

        return stringBuilder.toString();
    }


    public static ArrayList<Word> loadDataToViewPager(Context context, String database_name,String table_name, Word word) {
        ArrayList<Word> wordList;
        MyDatabase myDatabase = MyDatabase.getInstance(context);
        int id = word.getId();
        int num = 50;
        if (id < 50) num = id - 1;
        int start = id - 50;
        if (id < 50) start = 1;

        wordList = myDatabase.getData(database_name, table_name, start, num);
        wordList.add(word);
        wordList.addAll(myDatabase.getData(database_name, table_name, id + 1, 50));
        currentPositionOfViewPager = num;
        return wordList;
    }
}
