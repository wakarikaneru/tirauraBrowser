package studio.wakaru.test2.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import studio.wakaru.test2.util.TiraXMLMain;
import studio.wakaru.test2.util.Tubuyaki;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List<Tubuyaki>> mTubuyakiList;
    private MutableLiveData<Integer> scroll;

    private String xmlURL;
    private String imgURL;
    private int entriesCount;
    private boolean reply;

    public HomeViewModel() {
        Log.d("HomeViewModel", "HomeViewModel constructor");
        mTubuyakiList = new MutableLiveData<>();
        scroll = new MutableLiveData<>();

        xmlURL = "";
        imgURL = "";
        entriesCount = 10;
        reply = true;
    }

    public void loadSetting(Context c) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
        xmlURL = pref.getString("xml_resource", "");
        imgURL = pref.getString("img_resource", "");
        entriesCount = Integer.parseInt(pref.getString("entries_count", "10"));
        reply = pref.getBoolean("reply", false);
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

            try {
                //tiraXMLを読み込む
                URL u = new URL(xmlURL + "?hs=tiraura&st=0&li=" + entriesCount);
                TiraXMLMain tiraXML = new TiraXMLMain(u.toString());
                List<Tubuyaki> list = tiraXML.getTubuyakiList();

                if (reply) {
                    for (Tubuyaki t : list) {
                        if (0 < t.tres) {
                            URL uRes = new URL(xmlURL + "?tn=" + t.tno);
                            TiraXMLMain tx = new TiraXMLMain(uRes.toString());
                            t.setRes(tx.getTubuyakiList());
                        }
                    }
                }

                mTubuyakiList.postValue(list);

            } catch (MalformedURLException e) {
                e.printStackTrace();
                mTubuyakiList.postValue(new ArrayList<Tubuyaki>());
            }
        }
    }
}
