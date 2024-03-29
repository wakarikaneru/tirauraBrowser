package studio.wakaru.test2.ui.search;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import studio.wakaru.test2.PostActivity;
import studio.wakaru.test2.R;
import studio.wakaru.test2.ui.RefreshableFragment;
import studio.wakaru.test2.ui.home.HomeFragmentDirections;
import studio.wakaru.test2.ui.tubuyaki.TubuyakiFragment;
import studio.wakaru.test2.ui.user.UserFragment;
import studio.wakaru.test2.util.Good;
import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.Tiraura;
import studio.wakaru.test2.util.Tubuyaki;

public class SearchFragment extends RefreshableFragment {

    private SearchViewModel searchViewModel;

    private ScrollView scrollView;
    private SwipeRefreshLayout swipe;

    private String tiraURL;
    private String xmlURL;
    private String imgURL;
    private String cookie;
    private MyData myData;
    private Map<Integer, Boolean> abayoMap;

    private int searchMode;
    private String searchString;

    private int sortMode;
    private boolean sortReverse;

    private int replyCount;

    private int entryLineLimit;
    private int replyLineLimit;

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        searchViewModel =
                ViewModelProviders.of(getActivity()).get(SearchViewModel.class);

        final View root = inflater.inflate(R.layout.fragment_search, container, false);

        //設定を読み込む
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        tiraURL = pref.getString("tiraura_resource", "");
        xmlURL = pref.getString("xml_resource", "");
        imgURL = pref.getString("img_resource", "");
        cookie = pref.getString("COOKIE", "");
        replyCount = Integer.parseInt(pref.getString("reply_count", "0"));
        String abayoMapString = pref.getString("ABAYO_MAP", "{}");
        //Log.d("TubuyakiFragment", abayoMapString);

        Gson gson = new Gson();
        Type type = new TypeToken<Map<Integer, Boolean>>() {
        }.getType();
        abayoMap = gson.fromJson(abayoMapString, type);

        entryLineLimit = Integer.parseInt(pref.getString("entry_line_limit", "0"));
        replyLineLimit = Integer.parseInt(pref.getString("reply_line_limit", "0"));

        //設定の検証
        replyCount = Math.max(0, Math.min(replyCount, 300));

        entryLineLimit = Math.max(0, entryLineLimit);
        replyLineLimit = Math.max(0, replyLineLimit);

        myData = new MyData(cookie);

        //検索条件のリセット
        searchMode = searchViewModel.getSearchMode();
        searchString = searchViewModel.getSearchString();

        sortMode = searchViewModel.getSortMode();
        sortReverse = searchViewModel.isSortReverse();

        //スクロール状態を復元
        scrollView = root.findViewById(R.id.scrollView);

        searchViewModel.getScroll().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer i) {
                if (i != null) {
                    scrollView.post(new Runnable() {
                        public void run() {
                            scrollView.setScrollY(searchViewModel.getScroll().getValue());
                        }
                    });
                }
            }
        });

        final LinearLayout tubuyakiRoot = root.findViewById(R.id.tubuyaki_root);
        final LinearLayout searchInfoRoot = root.findViewById(R.id.search_info);

        //スワイプで更新
        swipe = root.findViewById(R.id.swipe_refresh_layout);
        swipe.setColorSchemeResources(R.color.colorPrimaryDark);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                searchViewModel.refresh(getContext(), searchMode, searchString, sortMode, sortReverse);
            }
        });

        //スワイプで更新の操作説明
        LinearLayout getStart = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki_getstart, null);
        tubuyakiRoot.addView(getStart);

        //FAB
        FloatingActionButton fab = root.findViewById(R.id.floatingActionButton);
        fab.bringToFront();
        if (false) {
            fab.hide();
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View searchOption = inflater.inflate(R.layout.dialog_search, null);

                final Spinner searchModeSpinner = searchOption.findViewById(R.id.spinner_search_mode);
                final TextView seachStringTextView = searchOption.findViewById(R.id.text_search_string);
                final Spinner sortModeSpinner = searchOption.findViewById(R.id.spinner_sort_mode);
                final CheckBox sortReverseCheck = searchOption.findViewById(R.id.check_sort_reverse);

                String[] searchModeList = {"すべて", "ユーザー名", "ユーザーID", "つぶやき", "ハッシュ"};
                SpinnerAdapter searchModeSpinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.row_text, searchModeList);
                searchModeSpinner.setAdapter(searchModeSpinnerAdapter);

                String[] sortModeList = {"デフォルト", "つぶやきID", "つぶやき作成日時", "つぶやき更新日時", "チラ見", "Good", "レス"};
                SpinnerAdapter sortModeSpinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.row_text, sortModeList);
                sortModeSpinner.setAdapter(sortModeSpinnerAdapter);

                AlertDialog ad = new AlertDialog.Builder(getActivity())
                        .setView(searchOption)
                        .setPositiveButton("検索", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // OK button pressed

                                int seachModeInt = SearchViewModel.SEARCH_MODE_NONE;
                                switch (searchModeSpinner.getSelectedItemPosition()) {
                                    case 0:
                                        seachModeInt = SearchViewModel.SEARCH_MODE_NONE;
                                        break;
                                    case 1:
                                        seachModeInt = SearchViewModel.SEARCH_MODE_UNAME;
                                        break;
                                    case 2:
                                        seachModeInt = SearchViewModel.SEARCH_MODE_UID;
                                        break;
                                    case 3:
                                        seachModeInt = SearchViewModel.SEARCH_MODE_TDATA;
                                        break;
                                    case 4:
                                        seachModeInt = SearchViewModel.SEARCH_MODE_THASH;
                                        break;
                                    default:
                                        break;
                                }

                                int sortModeInt = SearchViewModel.SORT_MODE_NONE;
                                switch (sortModeSpinner.getSelectedItemPosition()) {
                                    case 0:
                                        sortModeInt = SearchViewModel.SORT_MODE_NONE;
                                        break;
                                    case 1:
                                        sortModeInt = SearchViewModel.SORT_MODE_TNO;
                                        break;
                                    case 2:
                                        sortModeInt = SearchViewModel.SORT_MODE_TDATE;
                                        break;
                                    case 3:
                                        sortModeInt = SearchViewModel.SORT_MODE_TDATE2;
                                        break;
                                    case 4:
                                        sortModeInt = SearchViewModel.SORT_MODE_TVIEW;
                                        break;
                                    case 5:
                                        sortModeInt = SearchViewModel.SORT_MODE_TGOOD;
                                        break;
                                    case 6:
                                        sortModeInt = SearchViewModel.SORT_MODE_TRES;
                                        break;
                                    default:
                                        break;
                                }

                                searchMode = seachModeInt;
                                searchString = seachStringTextView.getText().toString().trim();
                                sortMode = sortModeInt;
                                sortReverse = sortReverseCheck.isChecked();

                                searchViewModel.setScroll(0);
                                searchViewModel.refresh(getContext(), searchMode, searchString, sortMode, sortReverse);
                                swipe.setRefreshing(true);
                            }
                        })
                        .setCancelable(true)
                        .show();

            }
        });

        //つぶやきデータを更新
        searchViewModel.getTubuyakiList().observe(this, new Observer<List<Tubuyaki>>() {
            @Override
            public void onChanged(@Nullable List<Tubuyaki> list) {

                //検索条件を表示
                searchInfoRoot.removeAllViews();
                LinearLayout searchInfo = (LinearLayout) getLayoutInflater().inflate(R.layout.search_info, null);
                ((TextView) searchInfo.findViewById(R.id.search_mode)).setText(SearchViewModel.getSearchModeString(searchMode));
                ((TextView) searchInfo.findViewById(R.id.search_text)).setText(searchString);

                ((TextView) searchInfo.findViewById(R.id.sort_mode)).setText(SearchViewModel.getSortModeString(sortMode));
                if (sortReverse) {
                    ((ImageView) searchInfo.findViewById(R.id.sort_reverse)).setImageResource(R.drawable.ic_arrow_downward_black_24dp);
                } else {
                    ((ImageView) searchInfo.findViewById(R.id.sort_reverse)).setImageResource(R.drawable.ic_arrow_upward_black_24dp);
                }

                searchInfoRoot.addView(searchInfo);

                //つぶやき一覧を消去
                tubuyakiRoot.removeAllViews();

                //つぶやき一覧を表示
                if (list.size() <= 0) {
                    //操作説明
                    LinearLayout getStart = (LinearLayout) getLayoutInflater().inflate(R.layout.search_help, null);
                    tubuyakiRoot.addView(getStart);

                } else {
                    for (final Tubuyaki t : list) {
                        LinearLayout lt = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki, null);
                        tubuyakiRoot.addView(lt);

                        ImageView imgAbayo = lt.findViewById(R.id.img_abayo);

                        TextView textResNo = lt.findViewById(R.id.text_resNo);

                        TextView textTdata = lt.findViewById(R.id.text_tdata);
                        TextView textTdate = lt.findViewById(R.id.text_tdate);
                        TextView textUname = lt.findViewById(R.id.text_uname);
                        TextView textTres = lt.findViewById(R.id.text_tres);
                        TextView textTview = lt.findViewById(R.id.text_tview);
                        TextView textTsage = lt.findViewById(R.id.text_tsage);
                        TextView textTgood = lt.findViewById(R.id.text_tgood);
                        TextView textTgood2 = lt.findViewById(R.id.text_tgood2);

                        ImageView imgUimg1 = lt.findViewById(R.id.img_uimg1);
                        ImageView imgTupfile1 = lt.findViewById(R.id.img_tupfile1);

                        textResNo.setVisibility(View.GONE);

                        if (abayoMap.containsKey(t.getUid())) {
                            if (abayoMap.get(t.getUid())) {
                                imgAbayo.setVisibility(View.VISIBLE);
                            }
                        }

                        // つぶやきを簡易表示

                        textTdata.setText(Tubuyaki.format(t.getTdata()));

                        if (entryLineLimit != 0) {
                            if (entryLineLimit == 1) {
                                textTdata.setSingleLine();
                            } else {
                                textTdata.setMaxLines(entryLineLimit);
                            }
                            textTdata.setEllipsize(TextUtils.TruncateAt.END);
                        }

                        textTdate.setText(t.getTdate());
                        textUname.setText(t.getUname());
                        textTres.setText("(" + t.getTres() + "レス)");
                        textTview.setText("(" + t.getTview() + "チラ見)");
                        if (1 == t.getTsage() || 1 == t.getTstealth()) {
                            textTsage.setVisibility(View.VISIBLE);
                        } else {
                            textTsage.setVisibility(View.GONE);
                        }
                        textTgood.setText("(" + t.getTgood() + "Good)");
                        textTgood2.setText(Good.good("♡", t.getTgood()));

                        Picasso.get().load(imgURL + t.getUimg1()).into(imgUimg1);

                        final String imgTupfile1Url = imgURL + t.getTupfile1();

                        if (t.getTupfile1().isEmpty()) {
                            imgTupfile1.setVisibility(View.GONE);
                        } else {
                            Picasso.get().load(imgTupfile1Url).into(imgTupfile1);
                        }

                        // レス一覧を表示
                        LinearLayout resRoot = lt.findViewById(R.id.res_root);

                        if (t.getRes().size() <= 1) {
                            resRoot.setVisibility(View.GONE);
                        } else {
                            int count = 0;
                            int resCount = 0;
                            for (Tubuyaki r : t.getRes()) {
                                if (t.getTno() != r.getTno()) {
                                    count++;
                                    if (t.getRes().size() - replyCount <= count) {

                                        LinearLayout lr = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki_res, null);
                                        resRoot.addView(lr);

                                        TextView textResNoRes = lr.findViewById(R.id.text_resNo);

                                        TextView textTdataRes = lr.findViewById(R.id.text_tdata);
                                        TextView textUnameRes = lr.findViewById(R.id.text_uname);
                                        ImageView imgURes = lr.findViewById(R.id.img_uimg1);

                                        textResNoRes.setText(String.valueOf(resCount));

                                        // レスを簡易表示
                                        textTdataRes.setText(Tubuyaki.format(r.getTdata()));

                                        if (replyLineLimit != 0) {
                                            if (replyLineLimit == 1) {
                                                textTdataRes.setSingleLine();
                                            } else {
                                                textTdataRes.setMaxLines(replyLineLimit);
                                            }
                                            textTdataRes.setEllipsize(TextUtils.TruncateAt.END);
                                        }

                                        textUnameRes.setText(r.getUname());

                                        Picasso.get().load(imgURL + r.getUimg1()).into(imgURes);
                                    }
                                }

                                resCount++;
                            }
                        }


                        imgTupfile1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImageView img = new ImageView(getActivity());
                                Picasso.get().load(imgTupfile1Url).into(img);
                                img.setScaleType(ImageView.ScaleType.FIT_XY);
                                img.setAdjustViewBounds(true);

                                AlertDialog ad = new AlertDialog.Builder(getActivity())
                                        .setView(img)
                                        .setPositiveButton("OK", null)
                                        .setCancelable(true)
                                        .show();

                            }
                        });

                        //タップしたらつぶやき画面に遷移
                        final int tno = t.getTno();
                        lt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openTubuyaki(t.getTno(), t.getUid(), t.getTres());

                            }
                        });

                        //長押しでポップアップメニューを表示
                        lt.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                popup(v, myData, t);
                                return true;
                            }
                        });

                        //registerForContextMenu(lt);

                    }

                    //続きを取得する
                    LinearLayout layoutContinue = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki_continue, null);
                    tubuyakiRoot.addView(layoutContinue);

                    layoutContinue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            searchViewModel.add(getContext());
                            swipe.setRefreshing(true);

                            LinearLayout layoutLoading = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki_loading, null);
                            tubuyakiRoot.addView(layoutLoading);

                            tubuyakiRoot.removeView(v);
                        }
                    });

                }

                swipe.setRefreshing(false);
            }
        });

        //検索条件を設定する
        Bundle bundle = getArguments();
        if (bundle != null) {
            searchMode = bundle.getInt("searchMode", SearchViewModel.SEARCH_MODE_NONE);
            searchString = bundle.getString("searchString", "");

            sortMode = bundle.getInt("sortMode", SearchViewModel.SORT_MODE_NONE);
            sortReverse = bundle.getBoolean("sortReverse", true);

            searchViewModel.refresh(getContext(), searchMode, searchString, sortMode, sortReverse);
            swipe.setRefreshing(true);
        }

        //searchViewModel.refresh(getContext());

        return root;
    }

    @Override
    public void refresh() {
        searchViewModel.setScroll(0);
        searchViewModel.refresh(getContext(), searchMode, searchString, sortMode, sortReverse);
        swipe.setRefreshing(true);
    }

    public void popup(View v, final MyData m, final Tubuyaki t) {

        PopupMenu popup = new PopupMenu(v.getContext(), v, Gravity.END, R.attr.actionOverflowMenuStyle, 0);
        popup.getMenuInflater().inflate(R.menu.menu_tubuyaki_context, popup.getMenu());

        popup.show();

        Menu menu = popup.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            switch (item.getItemId()) {
                case R.id.item_open:
                    item.setEnabled(t.getTno() != 0 && t.getParent() == 1);
                    break;
                case R.id.item_useropen:
                    item.setEnabled(t.getTno() != 0);
                    break;
                case R.id.item_good:
                    item.setEnabled(!tiraURL.isEmpty() && m.getMynum() != 0 && t.getTno() != 0);
                    break;
                case R.id.item_res:
                    item.setEnabled(!tiraURL.isEmpty() && m.getMynum() != 0 && t.getTno() != 0 && t.getParent() == 1);
                    break;
                case R.id.item_browser:
                    item.setEnabled(!tiraURL.isEmpty() && m.getMynum() != 0 && t.getTno() != 0 && t.getParent() == 1);
                    break;
            }
        }

        // ポップアップメニューのメニュー項目のクリック処理
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                // 押されたメニュー項目名をToastで表示
                switch (item.getItemId()) {
                    case R.id.item_open:
                        openTubuyaki(t.getTno(), t.getUid(), t.getTres());
                        break;
                    case R.id.item_useropen:
                        //openUser(t.getUid());
                        openSearch(SearchViewModel.SEARCH_MODE_UID, String.valueOf(t.getUid()), SearchViewModel.SORT_MODE_TDATE2, true);
                        break;
                    case R.id.item_good:
                        new GoodTask().execute(tiraURL, cookie, String.valueOf(t.getTno()));
                        return true;
                    case R.id.item_res:
                        openPostActivity(t.getTno(), t.getUid(), t.getTres());
                        return true;
                    case R.id.item_browser:
                        //ブラウザ起動
                        openBrowser(t.getTno());
                        return true;
                    default:
                        return false;
                }
                return false;
            }
        });
    }

    public void openTubuyaki(int tno, int uid, int tres) {
        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navController.navigate(HomeFragmentDirections.actionGlobalNavigationTubuyaki(tno, uid, tres));
    }

    public void openSearch(int searchMode, String searchString, int sortMode, boolean sortReverse) {
        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navController.navigate(HomeFragmentDirections.actionGlobalNavigationSearch(searchMode, searchString, sortMode, sortReverse));
    }

    public void openPostActivity(int tno, int tubuid, int tres) {

        //画面遷移
        Intent intent = new Intent(getActivity(), PostActivity.class);
        intent.putExtra("tno", tno);
        intent.putExtra("tubuid", tubuid);
        intent.putExtra("scount", tres);
        startActivity(intent);

    }

    public void openBrowser(int tno) {
        Uri uri = Uri.parse(tiraURL + "?mode=bbsdata_view&Category=CT01&newdata=1&id=" + tno);
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(i);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_tubuyaki_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        Toast.makeText(getContext(), "更新したとき反映されます", Toast.LENGTH_LONG).show();
        switch (item.getItemId()) {
            case R.id.item_open:
                // your first action code
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());

        //ログイン情報を取得
        cookie = pref.getString("COOKIE", "");
    }

    @Override
    public void onPause() {
        super.onPause();
        searchViewModel.setScroll(scrollView.getScrollY());
    }

    //非同期でGoodをつける
    private class GoodTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //do your request in here so that you don't interrupt the UI thread
            String url = params[0];
            String cookie = params[1];
            String tno = params[2];
            Tiraura.getXML(url + "?mode=fb_submit&f=u&Category=CT01&no=" + tno, cookie);
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            //Here you are done with the task
            Toast.makeText(getContext(), "更新したとき反映されます", Toast.LENGTH_LONG).show();
        }
    }

}