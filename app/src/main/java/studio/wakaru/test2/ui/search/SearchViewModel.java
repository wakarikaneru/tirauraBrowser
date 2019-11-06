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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.TiraXMLMain;
import studio.wakaru.test2.util.Tubuyaki;

public class SearchViewModel extends ViewModel {

    public static final int SEARCH_MODE_NONE = 0;
    public static final int SEARCH_MODE_UNAME = 1;
    public static final int SEARCH_MODE_UID = 2;
    public static final int SEARCH_MODE_TDATA = 4;
    public static final int SEARCH_MODE_THASH = 8;

    public static final int SORT_MODE_NONE = 0;
    public static final int SORT_MODE_TNO = 1;
    public static final int SORT_MODE_TDATE = 2;
    public static final int SORT_MODE_TDATE2 = 4;
    public static final int SORT_MODE_TVIEW = 8;
    public static final int SORT_MODE_TGOOD = 16;

    private boolean lock;

    private MutableLiveData<List<Tubuyaki>> mAllTubuyakiList;
    private MutableLiveData<List<Tubuyaki>> mTubuyakiList;
    private MutableLiveData<MyData> mMyData;
    private MutableLiveData<Integer> scroll;

    private int nowEntry;

    Map<Integer, Comparator<Tubuyaki>> sortObj;

    private int searchMode;
    private String searchString;

    private int sortMode;
    private boolean sortReverse;

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

        sortObj = new HashMap<>();
        sortObj.put(SORT_MODE_NONE, new Tubuyaki.Tdate2Comparator());
        sortObj.put(SORT_MODE_TNO, new Tubuyaki.TnoComparator());
        sortObj.put(SORT_MODE_TDATE, new Tubuyaki.TdateComparator());
        sortObj.put(SORT_MODE_TDATE2, new Tubuyaki.Tdate2Comparator());
        sortObj.put(SORT_MODE_TVIEW, new Tubuyaki.TviewComparator());
        sortObj.put(SORT_MODE_TGOOD, new Tubuyaki.TgoodComparator());


        searchMode = SEARCH_MODE_NONE;
        searchString = "";

        sortMode = SORT_MODE_NONE;
        sortReverse = true;

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

    public int getSearchMode() {
        return searchMode;
    }

    public String getSearchString() {
        return searchString;
    }

    public int getSortMode() {
        return sortMode;
    }

    public boolean isSortReverse() {
        return sortReverse;
    }

    public void setScroll(int scroll) {
        this.scroll.setValue(scroll);
    }

    public void refresh(Context c, int searchMode, String searchString, int sortMode, boolean sortReverse) {
        Log.d("SearchViewModel", "SearchViewModel refresh");

        this.searchMode = searchMode;
        this.searchString = searchString;

        this.sortMode = sortMode;
        this.sortReverse = sortReverse;

        Log.d("SearchViewModel", this.searchMode + " " + this.searchString + " " + this.sortMode + " " + this.sortReverse);

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

                            switch (searchMode) {
                                case SEARCH_MODE_NONE:
                                    hit = true;
                                    break;
                                case SEARCH_MODE_UNAME:
                                    if (t.getUname().contains(searchString)) {
                                        hit = true;
                                    }
                                    break;
                                case SEARCH_MODE_UID:
                                    if (searchString.equals(String.valueOf(t.getUid()))) {
                                        hit = true;
                                    }
                                    break;
                                case SEARCH_MODE_TDATA:
                                    if (t.getTdata().contains(searchString)) {
                                        hit = true;
                                    }
                                    break;
                                case SEARCH_MODE_THASH:
                                    if (searchString.equals(t.getThash())) {
                                        hit = true;
                                    }
                                    break;
                                default:
                                    hit = false;
                                    break;
                            }

                            if (hit) {
                                searchList.add(t);
                            }
                        }

                        //ソート
                        Comparator<Tubuyaki> c = sortObj.get(sortMode);
                        if (c != null) {
                            Collections.sort(searchList, c);
                        }
                        if (sortReverse) {
                            Collections.reverse(searchList);
                        }


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

    public static String getSearchModeString(int id) {
        switch (id) {
            case SEARCH_MODE_NONE:
                return "すべて";
            case SEARCH_MODE_UNAME:
                return "ユーザー名";
            case SEARCH_MODE_UID:
                return "ユーザーID";
            case SEARCH_MODE_TDATA:
                return "つぶやき";
            case SEARCH_MODE_THASH:
                return "ハッシュ";
            default:
                return "不明";
        }
    }

    public static String getSortModeString(int id) {
        switch (id) {
            case SORT_MODE_NONE:
                return "デフォルト";
            case SORT_MODE_TNO:
                return "つぶやきID";
            case SORT_MODE_TDATE:
                return "つぶやき作成日時";
            case SORT_MODE_TDATE2:
                return "つぶやき更新日時";
            case SORT_MODE_TVIEW:
                return "チラ見";
            case SORT_MODE_TGOOD:
                return "Good";
            default:
                return "不明";
        }
    }
}
