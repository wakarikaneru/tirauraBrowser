package studio.wakaru.test2.ui.log_tubuyaki;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

import java.net.MalformedURLException;
import java.net.URL;

import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.TiraXMLMain;

public class LogTubuyakiModel extends ViewModel {

    private boolean lock;

    private MutableLiveData<MyData> mMyData;
    private MutableLiveData<Integer> scroll;

    private String xmlURL;
    private String imgURL;
    private String cookie;

    private int tno;

    public LogTubuyakiModel() {
        Log.d("LogResModel", "LogResModel constructor");
        lock = false;
        mMyData = new MutableLiveData<>();
        scroll = new MutableLiveData<>();

        xmlURL = "";
        imgURL = "";
        cookie = "";

    }

    public void loadSetting(Context c) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
        xmlURL = pref.getString("xml_resource", "");
        imgURL = pref.getString("img_resource", "");
        cookie = pref.getString("COOKIE", "");
    }

    public LiveData<MyData> getMyData() {
        return mMyData;
    }

    public LiveData<Integer> getScroll() {
        return scroll;
    }

    public void setScroll(int scroll) {
        this.scroll.setValue(scroll);
    }

    public void refresh(Context c) {
        loadSetting(c);
        new LoadXML().execute();
    }

    //非同期で新着を取得
    private class LoadXML extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //do your request in here so that you don't interrupt the UI thread

            if (!lock) {
                lock = true;

                try {
                    //tiraXMLを読み込む
                    URL u = new URL(xmlURL);
                    TiraXMLMain tiraXML = new TiraXMLMain(u.toString(), cookie);

                    mMyData.postValue(tiraXML.getMyData());

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    mMyData.postValue(new MyData());
                }

                lock = false;
            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            //Here you are done with the task
        }
    }


}
