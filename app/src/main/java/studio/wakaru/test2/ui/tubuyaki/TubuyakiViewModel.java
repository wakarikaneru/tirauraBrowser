package studio.wakaru.test2.ui.tubuyaki;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import studio.wakaru.test2.ui.home.HomeViewModel;
import studio.wakaru.test2.util.TiraXMLMain;
import studio.wakaru.test2.util.Tubuyaki;

public class TubuyakiViewModel extends ViewModel {

    private boolean lock;

    private MutableLiveData<List<Tubuyaki>> mTubuyakiList;
    private MutableLiveData<Integer> scroll;

    private String xmlURL;
    private String imgURL;
    private String cookie;

    private int tno;

    public TubuyakiViewModel() {
        Log.d("TubuyakiViewModel", "TubuyakiViewModel constructor");
        lock = false;
        mTubuyakiList = new MutableLiveData<>();
        scroll = new MutableLiveData<>();

        xmlURL = "";
        imgURL = "";
        cookie = "";
        tno = 0;

    }

    public void loadSetting(Context c) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
        xmlURL = pref.getString("xml_resource", "");
        imgURL = pref.getString("img_resource", "");
        cookie = pref.getString("COOKIE", "");
    }


    public LiveData<List<Tubuyaki>> getTubuyakiList() {
        return mTubuyakiList;
    }

    public LiveData<Integer> getScroll() {
        return scroll;
    }

    public void setScroll(int scroll) {
        this.scroll.setValue(scroll);
    }

    public void setTno(int i) {
        tno = i;

        refresh();
    }

    public void refresh() {
        LoadXML t = new LoadXML();
        t.start();
    }

    public void refresh(Context c) {
        loadSetting(c);
        LoadXML t = new LoadXML();
        t.start();
    }

    class LoadXML extends Thread {
        public void run() {
            if (!lock) {
                lock = true;

                if (tno != 0) {

                    try {
                        //tiraXMLを読み込む
                        URL u = new URL(xmlURL + "?tn=" + tno);
                        TiraXMLMain tiraXML = new TiraXMLMain(u.toString(), cookie);
                        List<Tubuyaki> list = tiraXML.getTubuyakiList();

                        mTubuyakiList.postValue(list);

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        mTubuyakiList.postValue(new ArrayList<Tubuyaki>());
                    }
                } else {

                    mTubuyakiList.postValue(new ArrayList<Tubuyaki>());

                }

                lock = false;
            }
        }
    }
}
