package studio.wakaru.test2.ui.user;

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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.TiraXMLMain;
import studio.wakaru.test2.util.Tubuyaki;

public class UserViewModel extends ViewModel {

    private boolean lock;

    private MutableLiveData<List<Tubuyaki>> mTubuyakiList;
    private MutableLiveData<MyData> mMyData;
    private MutableLiveData<Integer> scroll;

    private int nowEntry;

    private String xmlURL;
    private String imgURL;
    private int entriesCount;
    private boolean reply;
    private String cookie;

    private int uid;

    public UserViewModel() {
        Log.d("UserViewModel", "UserViewModel constructor");
        lock = false;
        mTubuyakiList = new MutableLiveData<>();
        mMyData = new MutableLiveData<>();
        scroll = new MutableLiveData<>();
        nowEntry = 0;
        uid = 0;

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

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void refresh(Context c) {
        Log.d("UserViewModel", "UserViewModel refresh");
        loadSetting(c);
        new LoadXML().execute(false);
    }

    public void add(Context c) {
        Log.d("UserViewModel", "UserViewModel add");
        loadSetting(c);
        new LoadXML().execute(true);
    }


    //非同期でユーザーつぶやきを取得
    private class LoadXML extends AsyncTask<Boolean, Void, String> {

        @Override
        protected String doInBackground(Boolean... params) {
            //do your request in here so that you don't interrupt the UI thread

            boolean addFlag = params[0];

            if (!lock) {
                lock = true;

                if (uid != 0) {
                    try {
                        //tiraXMLを読み込む
                        if (addFlag) {
                        } else {
                            nowEntry = 0;
                        }

                        URL u = new URL(xmlURL + "?us=" + uid + "&st=" + nowEntry + "&li=" + entriesCount);
                        TiraXMLMain tiraXML = new TiraXMLMain(u.toString(), cookie);

                        mMyData.postValue(tiraXML.getMyData());

                        List<Tubuyaki> list = tiraXML.getTubuyakiList();

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

                        if (addFlag) {
                            List<Tubuyaki> listBase = mTubuyakiList.getValue();
                            listBase.addAll(list);

                            mTubuyakiList.postValue(listBase);
                        } else {
                            mTubuyakiList.postValue(list);
                        }

                        nowEntry += entriesCount;

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
