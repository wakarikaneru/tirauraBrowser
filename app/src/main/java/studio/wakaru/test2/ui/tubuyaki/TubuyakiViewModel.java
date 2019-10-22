package studio.wakaru.test2.ui.tubuyaki;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import studio.wakaru.test2.ui.home.HomeViewModel;
import studio.wakaru.test2.util.TiraXMLMain;
import studio.wakaru.test2.util.Tubuyaki;

public class TubuyakiViewModel extends ViewModel {

    private MutableLiveData<List<Tubuyaki>> mTubuyakiList;
    private MutableLiveData<Integer> scroll;

    private String url;
    private int tno;

    public TubuyakiViewModel() {
        Log.d("TubuyakiViewModel", "TubuyakiViewModel constructor");
        mTubuyakiList = new MutableLiveData<>();
        scroll = new MutableLiveData<>();

        url = "http://tiraura.orz.hm/tiraXML3.cgi";
        tno = 0;

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

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTno(int i) {
        tno = i;

        refresh();
    }

    public void refresh() {
        LoadXML t = new LoadXML();
        t.start();
    }

    class LoadXML extends Thread {
        public void run() {
            if (tno != 0) {

                //tiraXMLを読み込む

                TiraXMLMain tiraXML = new TiraXMLMain(url + "?tn=" + tno);

                List<Tubuyaki> list = tiraXML.getTubuyakiList();

                mTubuyakiList.postValue(list);

            } else {

                mTubuyakiList.postValue(new ArrayList<Tubuyaki>());

            }
        }
    }
}