package studio.wakaru.test2.ui.home;

import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import studio.wakaru.test2.util.TiraXMLMain;
import studio.wakaru.test2.util.Tubuyaki;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List<Tubuyaki>> mTubuyakiList;
    private MutableLiveData<Integer> scroll;

    private String url;
    private int entriesCount;
    private boolean reply;

    public HomeViewModel() {
        Log.d("HomeViewModel", "HomeViewModel constructor");
        mTubuyakiList = new MutableLiveData<>();
        scroll = new MutableLiveData<>();

        url = "http://tiraura.orz.hm/tiraXML3.cgi";
        entriesCount = 10;
        reply = false;

        //SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences();
        //String url = pref.getString("xml_resource", "");

        refresh();
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

    class LoadXML extends Thread {

        public void run() {

            //tiraXMLを読み込む
            TiraXMLMain tiraXML = new TiraXMLMain(url + "?hs=tiraura&st=0&li=" + entriesCount);

            List<Tubuyaki> list = tiraXML.getTubuyakiList();

            if (reply) {
                for (Tubuyaki t : list) {
                    TiraXMLMain tx = new TiraXMLMain(url + "?tn=" + t.tno);
                    t.setRes(tx.getTubuyakiList());
                }
            }

            mTubuyakiList.postValue(list);

        }
    }
}
