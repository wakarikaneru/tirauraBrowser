package studio.wakaru.test2.ui.tubuyaki;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import studio.wakaru.test2.ui.home.HomeViewModel;
import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.TiraXMLMain;
import studio.wakaru.test2.util.Tiraura;
import studio.wakaru.test2.util.Tubuyaki;

public class TubuyakiViewModel extends ViewModel {

    private boolean lock;

    private MutableLiveData<List<Tubuyaki>> mTubuyakiList;
    private MutableLiveData<MyData> mMyData;
    private MutableLiveData<Integer> scroll;

    private MutableLiveData<Boolean> abayo;

    private String xmlURL;
    private String imgURL;
    private String tiraURL;
    private String cookie;

    private int tno;
    private int uid;
    private int tres;

    public TubuyakiViewModel() {
        Log.d("TubuyakiViewModel", "TubuyakiViewModel constructor");
        lock = false;
        mTubuyakiList = new MutableLiveData<>();
        mMyData = new MutableLiveData<>();
        scroll = new MutableLiveData<>();
        abayo = new MutableLiveData<>();

        tno = 0;
        uid = 0;
        tres = 0;

        xmlURL = "";
        imgURL = "";
        tiraURL = "";
        cookie = "";

    }

    public void loadSetting(Context c) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
        xmlURL = pref.getString("xml_resource", "");
        imgURL = pref.getString("img_resource", "");
        tiraURL = pref.getString("tiraura_resource", "");
        cookie = pref.getString("COOKIE", "");
    }


    public LiveData<List<Tubuyaki>> getTubuyakiList() {
        return mTubuyakiList;
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

    public void setTno(int i) {
        tno = i;
    }

    public int getTno() {
        return tno;
    }

    public void setUid(int i) {
        uid = i;
    }

    public int getUid() {
        return uid;
    }

    public void setTres(int i) {
        tres = i;
    }

    public int getTres() {
        return tres;
    }

    public MutableLiveData<Boolean> getAbayo() {
        return abayo;
    }

    public void setAbayo(MutableLiveData<Boolean> abayo) {
        this.abayo = abayo;
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

                if (tno != 0) {

                    try {
                        //tiraXMLを読み込む
                        URL u = new URL(xmlURL + "?tn=" + tno);
                        TiraXMLMain tiraXML = new TiraXMLMain(u.toString(), cookie);

                        mMyData.postValue(tiraXML.getMyData());

                        List<Tubuyaki> list = tiraXML.getTubuyakiList();

                        mTubuyakiList.postValue(list);

                        //既読をつける
                        URL read = new URL(tiraURL + "?mode=bbsdata_view&Category=CT01&newdata=1&id=" + tno);
                        Tiraura.getHTML(read.toString(), cookie);

                        //あばよチェック
                        URL abayoCheck = new URL(tiraURL + "?mode=mail_form&mid=" + uid);
                        String abayoCheckResult = Tiraura.getHTML(abayoCheck.toString(), cookie);

                        if (abayoCheckResult.contains("残念ながらメッセージが送信できません")) {
                            abayo.postValue(true);
                        }else{
                            abayo.postValue(false);
                        }

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        mTubuyakiList.postValue(new ArrayList<Tubuyaki>());
                    }
                } else {

                    mTubuyakiList.postValue(new ArrayList<Tubuyaki>());

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
