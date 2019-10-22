package studio.wakaru.test2.ui.tubuyaki;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import studio.wakaru.test2.ui.home.HomeViewModel;
import studio.wakaru.test2.util.TiraXMLMain;
import studio.wakaru.test2.util.Tubuyaki;

public class TubuyakiViewModel extends ViewModel {

    private MutableLiveData<List<Tubuyaki>> mTubuyakiList;
    private MutableLiveData<Integer> scroll;
    private int tno;

    public TubuyakiViewModel() {
        Log.d("TubuyakiViewModel", "TubuyakiViewModel constructor");
        mTubuyakiList = new MutableLiveData<>();
        scroll = new MutableLiveData<>();
        tno = 0;

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
                TiraXMLMain tiraXML = new TiraXMLMain("http://tiraura.orz.hm/tiraXML3.cgi?tn=" + tno);

                List<Tubuyaki> list = tiraXML.getTubuyakiList();

                mTubuyakiList.postValue(list);

            } else {

                mTubuyakiList.postValue(new ArrayList<Tubuyaki>());

            }
        }
    }
}