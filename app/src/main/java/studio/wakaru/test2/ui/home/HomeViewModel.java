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

    public HomeViewModel() {
        Log.d("HomeViewModel", "HomeViewModel constructor");
        mTubuyakiList = new MutableLiveData<>();
        scroll = new MutableLiveData<>();

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
            TiraXMLMain tiraXML = new TiraXMLMain("http://tiraura.orz.hm/tiraXML3.cgi?st=0&li=30&hs=tiraura");

            List<Tubuyaki> list = tiraXML.getTubuyakiList();

            for (Tubuyaki t : list) {
                TiraXMLMain tx = new TiraXMLMain("http://tiraura.orz.hm/tiraXML3.cgi?tn=" + t.tno);
                t.setRes(tx.getTubuyakiList());
            }

            mTubuyakiList.postValue(list);

        }
    }
}
