package studio.wakaru.test2.ui.log_res;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import studio.wakaru.test2.PostActivity;
import studio.wakaru.test2.R;
import studio.wakaru.test2.ui.RefreshableFragment;
import studio.wakaru.test2.ui.home.HomeFragmentDirections;
import studio.wakaru.test2.ui.search.SearchFragment;
import studio.wakaru.test2.ui.search.SearchViewModel;
import studio.wakaru.test2.ui.tubuyaki.TubuyakiFragment;
import studio.wakaru.test2.ui.user.UserFragment;
import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.MyResLog;
import studio.wakaru.test2.util.Tiraura;
import studio.wakaru.test2.util.Tubuyaki;

public class LogResFragment extends RefreshableFragment {

    private LogResModel logResModel;

    private ScrollView scrollView;
    private SwipeRefreshLayout swipe;

    private String tiraURL;
    private String imgURL;
    private String cookie;
    private MyData myData;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        logResModel =
                ViewModelProviders.of(getActivity()).get(LogResModel.class);

        View root = inflater.inflate(R.layout.fragment_log_tubuyaki, container, false);

        //設定を読み込む
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        tiraURL = pref.getString("tiraura_resource", "");
        imgURL = pref.getString("img_resource", "");
        cookie = pref.getString("COOKIE", "");

        myData = new MyData(cookie);


        //スクロール状態を復元
        scrollView = root.findViewById(R.id.scrollView);

        logResModel.getScroll().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer i) {
                if (i != null) {
                    scrollView.post(new Runnable() {
                        public void run() {
                            scrollView.setScrollY(logResModel.getScroll().getValue());
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
                logResModel.refresh(getContext());
            }
        });

        //スワイプで更新の操作説明
        LinearLayout getStart = (LinearLayout) getLayoutInflater().inflate(R.layout.log_res_getstart, null);
        tubuyakiRoot.addView(getStart);

        //つぶやきデータを更新
        logResModel.getMyData().observe(this, new Observer<MyData>() {
            @Override
            public void onChanged(@Nullable final MyData mydata) {

                //つぶやき一覧を消去
                tubuyakiRoot.removeAllViews();

                //つぶやき一覧を表示
                if (mydata.getMynum() == 0) {
                    //操作説明
                    LinearLayout getStart = (LinearLayout) getLayoutInflater().inflate(R.layout.log_res_help, null);
                    tubuyakiRoot.addView(getStart);

                } else {
                    for (final MyResLog r : mydata.getMyreslog()) {

                        LinearLayout lt;

                        lt = (LinearLayout) getLayoutInflater().inflate(R.layout.log_res, null);
                        tubuyakiRoot.addView(lt);

                        ImageView imageUnread = lt.findViewById(R.id.image_unread);
                        TextView textTdata = lt.findViewById(R.id.text_tdata);
                        TextView textTdate = lt.findViewById(R.id.text_tdate);
                        TextView textTres = lt.findViewById(R.id.text_tres);

                        if (!r.isUnreadFlag()) {
                            imageUnread.setVisibility(View.INVISIBLE);
                        }

                        textTdata.setText(Tubuyaki.format(r.getTdata()));

                        textTdata.setSingleLine();
                        textTdata.setEllipsize(TextUtils.TruncateAt.END);

                        textTres.setText("(" + r.getTres() + "レス)");

                        //タップしたらつぶやき画面に遷移\
                        lt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openTubuyaki(r.getTno(), mydata.getMynum(), r.getTres());

                            }
                        });
                    }

                }
                swipe.setRefreshing(false);
            }
        });

        //logResModel.refresh(getContext());

        return root;
    }

    @Override
    public void refresh() {
        logResModel.setScroll(0);
        logResModel.refresh(getContext());
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
    public void onPause() {
        super.onPause();
        logResModel.setScroll(scrollView.getScrollY());
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