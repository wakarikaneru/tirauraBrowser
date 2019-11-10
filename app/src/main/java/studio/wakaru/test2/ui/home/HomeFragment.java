package studio.wakaru.test2.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.List;

import studio.wakaru.test2.PostActivity;
import studio.wakaru.test2.R;
import studio.wakaru.test2.ui.search.SearchFragment;
import studio.wakaru.test2.ui.search.SearchViewModel;
import studio.wakaru.test2.ui.tubuyaki.TubuyakiFragment;
import studio.wakaru.test2.ui.user.UserFragment;
import studio.wakaru.test2.util.Good;
import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.Tiraura;
import studio.wakaru.test2.util.Tubuyaki;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    private ScrollView scrollView;
    private SwipeRefreshLayout swipe;

    private String tiraURL;
    private String xmlURL;
    private String imgURL;
    private String cookie;
    private MyData myData;

    private int replyCount;

    private int entryLineLimit;
    private int replyLineLimit;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(getActivity()).get(HomeViewModel.class);

        final View root = inflater.inflate(R.layout.fragment_home, container, false);

        //設定を読み込む
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        tiraURL = pref.getString("tiraura_resource", "");
        xmlURL = pref.getString("xml_resource", "");
        imgURL = pref.getString("img_resource", "");
        cookie = pref.getString("COOKIE", "");
        replyCount = Integer.parseInt(pref.getString("reply_count", "0"));

        entryLineLimit = Integer.parseInt(pref.getString("entry_line_limit", "0"));
        replyLineLimit = Integer.parseInt(pref.getString("reply_line_limit", "0"));

        //設定の検証
        replyCount = Math.max(0, Math.min(replyCount, 300));

        entryLineLimit = Math.max(0, entryLineLimit);
        replyLineLimit = Math.max(0, replyLineLimit);

        myData = new MyData(cookie);


        //スクロール状態を復元
        scrollView = root.findViewById(R.id.scrollView);

        homeViewModel.getScroll().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer i) {
                if (i != null) {
                    scrollView.post(new Runnable() {
                        public void run() {
                            scrollView.setScrollY(homeViewModel.getScroll().getValue());
                        }
                    });
                }
            }
        });

        final LinearLayout tubuyakiRoot = root.findViewById(R.id.tubuyaki_root);

        //スワイプで更新
        swipe = root.findViewById(R.id.swipe_refresh_layout);
        swipe.setColorSchemeResources(R.color.colorPrimaryDark);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                homeViewModel.refresh(getContext());
            }
        });

        //スワイプで更新の操作説明
        LinearLayout getStart = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki_getstart, null);
        tubuyakiRoot.addView(getStart);

        //FAB
        FloatingActionButton fab = root.findViewById(R.id.floatingActionButton);
        fab.bringToFront();
        if (tiraURL.isEmpty() || myData.getMynum() == 0) {
            fab.hide();
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPostActivity(0, 0, 0);
            }
        });

        //初回起動時の更新
        if (!homeViewModel.isInitialized()) {
            homeViewModel.refresh(getContext());
            swipe.setRefreshing(true);
        }

        //つぶやきデータを更新
        homeViewModel.getTubuyakiList().observe(this, new Observer<List<Tubuyaki>>() {
            @Override
            public void onChanged(@Nullable List<Tubuyaki> list) {

                //つぶやき一覧を消去
                tubuyakiRoot.removeAllViews();

                //つぶやき一覧を表示
                if (list.size() <= 0) {
                    //操作説明
                    LinearLayout getStart = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki_help, null);
                    tubuyakiRoot.addView(getStart);

                } else {
                    for (final Tubuyaki t : list) {
                        LinearLayout lt = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki, null);
                        tubuyakiRoot.addView(lt);

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
                                openTubuyaki(t.getTno());

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

                            homeViewModel.add(getContext());

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

        //homeViewModel.refresh(getContext());

        return root;
    }

    public void refresh() {

        homeViewModel.refresh(getContext());
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
                        openTubuyaki(t.getTno());
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

    public void openTubuyaki(int tno) {

        //メニューを選択状態に変更
        BottomNavigationView bnv = getActivity().findViewById(R.id.nav_view);
        Menu menu = bnv.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        //画面遷移
        Bundle bundle = new Bundle();
        bundle.putInt("tno", tno);

        TubuyakiFragment tf = new TubuyakiFragment();
        tf.setArguments(bundle);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, tf)
                .commit();
    }

    public void openSearch(int searchMode, String searchString, int sortMode, boolean sortReverse) {

        //メニューを選択状態に変更
        BottomNavigationView bnv = getActivity().findViewById(R.id.nav_view);
        Menu menu = bnv.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        //画面遷移
        Bundle bundle = new Bundle();
        bundle.putInt("searchMode", searchMode);
        bundle.putString("searchString", searchString);
        bundle.putInt("sortMode", sortMode);
        bundle.putBoolean("sortReverse", sortReverse);

        SearchFragment sf = new SearchFragment();
        sf.setArguments(bundle);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, sf)
                .commit();
    }

    public void openUser(int uid) {

        //メニューを選択状態に変更
        BottomNavigationView bnv = getActivity().findViewById(R.id.nav_view);
        Menu menu = bnv.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        //画面遷移
        Bundle bundle = new Bundle();
        bundle.putInt("uid", uid);

        UserFragment uf = new UserFragment();
        uf.setArguments(bundle);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, uf)
                .commit();
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
        homeViewModel.setScroll(scrollView.getScrollY());
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