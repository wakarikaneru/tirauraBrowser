package studio.wakaru.test2.ui.search;

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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.TiraXMLMain;
import studio.wakaru.test2.util.Tubuyaki;

public class SearchViewModel extends ViewModel {

    private boolean lock;

    private MutableLiveData<List<Tubuyaki>> mAllTubuyakiList;
    private MutableLiveData<List<Tubuyaki>> mTubuyakiList;
    private MutableLiveData<MyData> mMyData;
    private MutableLiveData<Integer> scroll;
    private int nowEntry;

    private String xmlURL;
    private String imgURL;
    private int entriesCount;
    private boolean reply;
    private String cookie;


    public SearchViewModel() {
        Log.d("SearchViewModel", "SearchViewModel constructor");
        lock = false;
        mAllTubuyakiList = new MutableLiveData<>();
        mTubuyakiList = new MutableLiveData<>();
        mMyData = new MutableLiveData<>();
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
        Log.d("SearchViewModel", "SearchViewModel refresh");
        loadSetting(c);
        new LoadXML().execute(false);
    }

    public void add(Context c) {
        Log.d("SearchViewModel", "SearchViewModel add");
        loadSetting(c);
        new LoadXML().execute(true);
    }


    //非同期で新着を取得
    private class LoadXML extends AsyncTask<Boolean, Void, String> {

        @Override
        protected String doInBackground(Boolean... params) {
            //do your request in here so that you don't interrupt the UI thread

            boolean addFlag = params[0];

            if (!lock) {
                lock = true;

                try {
                    //tiraXMLを読み込む
                    if (!addFlag) {

                        URL u = new URL(xmlURL + "?tn=1");
                        TiraXMLMain tiraXML = new TiraXMLMain(u.toString(), cookie);

                        mMyData.postValue(tiraXML.getMyData());

                        List<Tubuyaki> allList = tiraXML.getTubuyakiList();

                        List<Tubuyaki> searchList = new ArrayList<>();
                        //フィルタ
                        for (Tubuyaki t : allList) {
                            boolean hit = false;
                            if ("チラ裏HOTWORD".equals(t.getThash())) {
                                hit = true;
                            }

                            if (hit) {
                                searchList.add(t);
                            }
                        }

                        //ソート
                        Collections.sort(searchList, new Tubuyaki.Tdate2Comparator());
                        Collections.reverse(searchList);


                        mAllTubuyakiList.postValue(searchList);

                        List<Tubuyaki> list = searchList.subList(0, Math.min(searchList.size(), entriesCount));

                        //レスを取得
                        if (reply) {
                            ExecutorService executor = Executors.newFixedThreadPool(1);
                            List<GetResCallable> fl = new ArrayList<GetResCallable>();

                            for (Tubuyaki t : list) {
                                if (0 < t.getTres()) {
                                    fl.add(new GetResCallable(t));
                                }
                            }

                            //実行＆終了待ち;
                            try {
                                executor.invokeAll(fl);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            executor.shutdown();
                        }

                        mTubuyakiList.postValue(list);

                        nowEntry = 0 + entriesCount;
                    } else {
                        List<Tubuyaki> allList = mAllTubuyakiList.getValue();
                        List<Tubuyaki> list = allList.subList(Math.min(allList.size(), nowEntry), Math.min(allList.size(), nowEntry + entriesCount));

                        if (reply) {
                            ExecutorService executor = Executors.newFixedThreadPool(1);
                            List<GetResCallable> fl = new ArrayList<GetResCallable>();

                            for (Tubuyaki t : list) {
                                if (0 < t.getTres()) {
                                    fl.add(new GetResCallable(t));
                                }
                            }

                            //実行＆終了待ち;
                            try {
                                executor.invokeAll(fl);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            executor.shutdown();
                        }

                        List<Tubuyaki> nowList = mTubuyakiList.getValue();
                        nowList.addAll(list);
                        mTubuyakiList.postValue(nowList);

                        nowEntry = nowEntry + entriesCount;
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
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

    class GetResCallable implements Callable<Tubuyaki> {
        private Tubuyaki tubuyaki;

        public GetResCallable(Tubuyaki t) {
            tubuyaki = t;
        }

        public Tubuyaki call() {
            URL uRes = null;
            try {
                uRes = new URL(xmlURL + "?tn=" + tubuyaki.getTno());
                TiraXMLMain tx = new TiraXMLMain(uRes.toString(), cookie);
                tubuyaki.setRes(tx.getTubuyakiList());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return tubuyaki;
        }
    }
}
