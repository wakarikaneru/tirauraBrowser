package studio.wakaru.test2.ui.tubuyaki;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.text.HtmlCompat;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import studio.wakaru.test2.PostActivity;
import studio.wakaru.test2.R;
import studio.wakaru.test2.ui.RefreshableFragment;
import studio.wakaru.test2.ui.home.HomeFragmentDirections;
import studio.wakaru.test2.ui.search.SearchFragment;
import studio.wakaru.test2.ui.search.SearchViewModel;
import studio.wakaru.test2.ui.user.UserFragment;
import studio.wakaru.test2.util.Good;
import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.Tiraura;
import studio.wakaru.test2.util.Tubuyaki;

public class TubuyakiFragment extends RefreshableFragment {

    private TubuyakiViewModel tubuyakiViewModel;

    private ScrollView scrollView;
    private SwipeRefreshLayout swipe;

    private String tiraURL;
    private String imgURL;
    private boolean richText;
    private String cookie;
    private Map<Integer, Boolean> abayoMap;
    private boolean antiAbayoTubuyaki;
    private boolean antiAbayoRes;

    private MyData myData;
    private Tubuyaki tubuyaki;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        tubuyakiViewModel =
                ViewModelProviders.of(getActivity()).get(TubuyakiViewModel.class);

        View root = inflater.inflate(R.layout.fragment_tubuyaki, container, false);

        //設定を読み込む
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        tiraURL = pref.getString("tiraura_resource", "");
        imgURL = pref.getString("img_resource", "");
        richText = pref.getBoolean("rich_text", false);
        cookie = pref.getString("COOKIE", "");
        String abayoMapString = pref.getString("ABAYO_MAP", "{}");
        //Log.d("TubuyakiFragment", abayoMapString);

        Gson gson = new Gson();
        Type type = new TypeToken<Map<Integer, Boolean>>() {
        }.getType();
        abayoMap = gson.fromJson(abayoMapString, type);
        antiAbayoTubuyaki = pref.getBoolean("anti_abayo_tubuyaki", false);
        antiAbayoRes = pref.getBoolean("anti_abayo_res", false);

        myData = new MyData(cookie);
        tubuyaki = new Tubuyaki();

        //スクロール状態を復元
        scrollView = root.findViewById(R.id.scrollView);

        tubuyakiViewModel.getScroll().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer i) {
                if (i != null) {
                    scrollView.post(new Runnable() {
                        public void run() {
                            scrollView.setScrollY(tubuyakiViewModel.getScroll().getValue());
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
                tubuyakiViewModel.refresh(getContext());
            }
        });

        //スワイプで更新の操作説明
        LinearLayout getStart = (LinearLayout) getLayoutInflater().inflate(R.layout.res_getstart, null);
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
                openPostActivity(tubuyaki.getTno(), tubuyaki.getUid(), tubuyaki.getTres());
            }
        });

        //つぶやきデータを更新
        tubuyakiViewModel.getTubuyakiList().observe(this, new Observer<List<Tubuyaki>>() {
            @Override
            public void onChanged(@Nullable List<Tubuyaki> list) {

                //つぶやき一覧を消去
                tubuyakiRoot.removeAllViews();

                //つぶやき一覧を表示
                if (list.size() <= 0) {
                    //操作説明
                    LinearLayout getStart = (LinearLayout) getLayoutInflater().inflate(R.layout.res_getstart, null);
                    tubuyakiRoot.addView(getStart);

                } else {
                    int resCount = 0;
                    tubuyaki = list.get(0);

                    //あばよチェック
                    abayoMap.put(tubuyakiViewModel.getUid(), tubuyakiViewModel.isAbayo());
                    SharedPreferences.Editor editor = pref.edit();
                    Gson gson = new Gson();
                    editor.putString("ABAYO_MAP", gson.toJson(abayoMap));
                    editor.commit();

                    for (final Tubuyaki t : list) {

                        LinearLayout lt;
                        if (resCount == 0) {
                            lt = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki, null);
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


                            textResNo.setText(String.valueOf(resCount));

                            if (abayoMap.containsKey(t.getUid())) {
                                if (abayoMap.get(t.getUid())) {
                                    if (antiAbayoTubuyaki) {
                                        //lt.setVisibility(View.GONE);
                                    }
                                    imgAbayo.setVisibility(View.VISIBLE);
                                }
                            }

                            textTdata.setAutoLinkMask(Linkify.WEB_URLS);
                            if (richText) {
                                textTdata.setText(HtmlCompat.fromHtml(t.getTdata(), HtmlCompat.FROM_HTML_MODE_COMPACT));
                            } else {
                                textTdata.setText(Tubuyaki.format(t.getTdata()));
                            }

                            if (t.getTtitle().contains("［スタンプ］")) {
                                textTdata.setVisibility(View.GONE);
                                ViewGroup.LayoutParams lp = imgTupfile1.getLayoutParams();
                                lp.height = lp.height * 3;
                                lp.width = lp.width * 3;
                                imgTupfile1.setLayoutParams(lp);
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

                            imgTupfile1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ImageView img = new ImageView(getActivity());
                                    Picasso.get().load(imgTupfile1Url).into(img);
                                    img.setScaleType(ImageView.ScaleType.FIT_XY);
                                    img.setAdjustViewBounds(true);

                                    new AlertDialog.Builder(getActivity())
                                            .setView(img)
                                            .setPositiveButton(android.R.string.ok, null)
                                            .setCancelable(true)
                                            .show();
                                }
                            });

                            final int tno = t.getTno();

                            //長押しでブラウザで開く
                            lt.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    popup(v, myData, t);

                                    return true;
                                }
                            });


                        } else {
                            lt = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki, null);
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

                            textResNo.setText(String.valueOf(resCount));

                            if (abayoMap.containsKey(t.getUid())) {
                                if (abayoMap.get(t.getUid())) {
                                    if (antiAbayoRes) {
                                        lt.setVisibility(View.GONE);
                                    }
                                    imgAbayo.setVisibility(View.VISIBLE);
                                }
                            }

                            textTdata.setAutoLinkMask(Linkify.WEB_URLS);
                            if (richText) {
                                textTdata.setText(HtmlCompat.fromHtml(t.getTdata(), HtmlCompat.FROM_HTML_MODE_COMPACT));
                            } else {
                                textTdata.setText(Tubuyaki.format(t.getTdata()));
                            }

                            if ("［画像有り］".equals(Tubuyaki.format(t.getTdata()).trim())) {
                                textTdata.setVisibility(View.GONE);
                                ViewGroup.LayoutParams lp = imgTupfile1.getLayoutParams();
                                lp.height = lp.height * 3;
                                lp.width = lp.width * 3;
                                imgTupfile1.setLayoutParams(lp);
                            }

                            textTdate.setText(t.getTdate());
                            textUname.setText(t.getUname());
                            textTres.setVisibility(View.GONE);//textTres.setText("(" + t.getTres() + "レス)");
                            textTview.setVisibility(View.GONE);//textTview.setText("(" + t.getTview() + "チラ見)");
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

                            imgTupfile1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ImageView img = new ImageView(getActivity());
                                    Picasso.get().load(imgTupfile1Url).into(img);
                                    img.setScaleType(ImageView.ScaleType.FIT_XY);
                                    img.setAdjustViewBounds(true);

                                    new AlertDialog.Builder(getActivity())
                                            .setView(img)
                                            .setPositiveButton(android.R.string.ok, null)
                                            .setCancelable(true)
                                            .show();
                                }
                            });

                            final int tno = t.getTno();

                            //長押しでポップアップメニューで開く
                            lt.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    popup(v, myData, t);
                                    return true;
                                }
                            });
                        }
                        resCount++;
                    }

                    //続きを取得する
                    LinearLayout layoutContinue = (LinearLayout) getLayoutInflater().inflate(R.layout.res_continue, null);
                    tubuyakiRoot.addView(layoutContinue);

                    layoutContinue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            tubuyakiViewModel.refresh(getContext());
                            swipe.setRefreshing(true);

                            LinearLayout layoutLoading = (LinearLayout) getLayoutInflater().inflate(R.layout.res_loading, null);
                            tubuyakiRoot.addView(layoutLoading);

                            tubuyakiRoot.removeView(v);
                        }
                    });
                }
                swipe.setRefreshing(false);
            }
        });

        //tnoを設定する
        Bundle bundle = getArguments();
        if (bundle != null) {
            boolean setArgs = bundle.getBoolean("setArgs", false);
            if (setArgs) {
                int prevTno = tubuyakiViewModel.getTno();
                int nowTno = bundle.getInt("tno");
                int uid = bundle.getInt("uid");
                int tres = bundle.getInt("tres");

                Log.d("TubuyakiFragment", "prevTno = " + prevTno + ", nowTno = " + nowTno);
                if (prevTno != nowTno) {
                    tubuyakiViewModel.setTno(nowTno);
                    tubuyakiViewModel.setUid(uid);
                    tubuyakiViewModel.setTres(tres);

                    tubuyakiViewModel.setScroll(0);
                    tubuyakiViewModel.refresh(getContext());
                    swipe.setRefreshing(true);
                }
            }
        } else {
            Log.d("TubuyakiFragment", "bundle == null");
        }

        //tubuyakiViewModel.refresh(getContext());

        return root;
    }

    @Override
    public void refresh() {
        tubuyakiViewModel.setScroll(0);
        tubuyakiViewModel.refresh(getContext());
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
        navController.navigate(HomeFragmentDirections.actionGlobalNavigationTubuyaki(true, tno, uid, tres));
    }

    public void openSearch(int searchMode, String searchString, int sortMode, boolean sortReverse) {
        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navController.navigate(HomeFragmentDirections.actionGlobalNavigationSearch(true, searchMode, searchString, sortMode, sortReverse));
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
    public void onPause() {
        super.onPause();
        tubuyakiViewModel.setScroll(scrollView.getScrollY());
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