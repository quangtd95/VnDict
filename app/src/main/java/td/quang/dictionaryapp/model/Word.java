package td.quang.dictionaryapp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Quang_TD on 11/20/2016.
 */
public class Word implements Parcelable {
    public static final Creator<Word> CREATOR = new Creator<Word>() {
        @Override
        public Word createFromParcel(Parcel in) {
            return new Word(in);
        }

        @Override
        public Word[] newArray(int size) {
            return new Word[size];
        }
    };
    private int id;
    private String word;
    private String mean;
    private String note="";

    public Word() {
        note = "";
    }

    public Word(String word, String mean) {
        this.word = word;
        this.mean = mean;
        note="";
    }

    protected Word(Parcel in) {
        id = in.readInt();
        word = in.readString();
        mean = in.readString();
        note = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMean() {
        return mean;
    }

    public void setMean(String mean) {
        this.mean = mean;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(word);
        parcel.writeString(mean);
        parcel.writeString(note);
    }
}
