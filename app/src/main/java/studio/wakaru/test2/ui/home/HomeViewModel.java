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

    private boolean lock;

    private MutableLiveData<List<Tubuyaki>> mTubuyakiList;
    private MutableLiveData<Integer> scroll;
    private int nowEntry;

    private String xmlURL;
    private String imgURL;
    private int entriesCount;
    private boolean reply;
    private String cookie;


    public HomeViewModel() {
        Log.d("HomeViewModel", "HomeViewModel constructor");
        lock = false;
        mTubuyakiList = new MutableLiveData<>();
        scroll = new MutableLiveData<>();
        nowEntry = 0;

        xmlURL = "";
        imgURL = "";
        cookie = "";
        entriesCount = 10;
        reply = true;
    }

    public void loadSetting(Context c) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
        xmlURL = pref.getString("xml_resource", "");
        imgURL = pref.getString("img_resource", "");
        entriesCount = Integer.parseInt(pref.getString("entries_count", "10"));
        reply = pref.getBoolean("reply", false);
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

    public void refresh(Context c) {
        Log.d("HomeViewModel", "HomeViewModel refresh");
        loadSetting(c);
        LoadXML t = new LoadXML();
        t.start();
    }

    public void add(Context c) {
        Log.d("HomeViewModel", "HomeViewModel add");
        loadSetting(c);
        LoadXML2 t = new LoadXML2();
        t.start();
    }

    class LoadXML extends Thread {

        public void run() {
            if (!lock) {
                lock = true;

                try {
                    //tiraXMLを読み込む
                    nowEntry = 0;
                    URL u = new URL(xmlURL + "?hs=tiraura&st=" + nowEntry + "&li=" + entriesCount);
                    TiraXMLMain tiraXML = new TiraXMLMain(u.toString(), cookie);

                    List<Tubuyaki> list = tiraXML.getTubuyakiList();

                    if (reply) {
                        for (Tubuyaki t : list) {
                            if (0 < t.getTres()) {
                                URL uRes = new URL(xmlURL + "?tn=" + t.getTno());
                                TiraXMLMain tx = new TiraXMLMain(uRes.toString(), cookie);
                                t.setRes(tx.getTubuyakiList());
                            }
                        }
                    }

                    nowEntry += entriesCount;
                    mTubuyakiList.postValue(list);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    mTubuyakiList.postValue(new ArrayList<Tubuyaki>());
                }

                lock = false;
            }
        }
    }

    class LoadXML2 extends Thread {

        public void run() {
            if (!lock) {
                lock = true;

                try {
                    //tiraXMLを読み込む
                    URL u = new URL(xmlURL + "?hs=tiraura&st=" + nowEntry + "&li=" + entriesCount);
                    TiraXMLMain tiraXML = new TiraXMLMain(u.toString(), cookie);
                    List<Tubuyaki> list = tiraXML.getTubuyakiList();

                    if (reply) {
                        for (Tubuyaki t : list) {
                            if (0 < t.getTres()) {
                                URL uRes = new URL(xmlURL + "?tn=" + t.getTno());
                                TiraXMLMain tx = new TiraXMLMain(uRes.toString(), cookie);
                                t.setRes(tx.getTubuyakiList());
                            }
                        }
                    }

                    List<Tubuyaki> listBase = mTubuyakiList.getValue();
                    listBase.addAll(list);

                    nowEntry += entriesCount;
                    mTubuyakiList.postValue(listBase);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    mTubuyakiList.postValue(new ArrayList<Tubuyaki>());
                }

                lock = false;
            }
        }
    }
}
