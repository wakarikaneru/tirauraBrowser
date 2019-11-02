package studio.wakaru.test2.ui.mydata;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
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
import studio.wakaru.test2.ui.tubuyaki.TubuyakiFragment;
import studio.wakaru.test2.ui.user.UserFragment;
import studio.wakaru.test2.util.Good;
import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.MyTubuyakiLog;
import studio.wakaru.test2.util.Tiraura;
import studio.wakaru.test2.util.Tubuyaki;

public class MyDataFragment extends Fragment {

    private MyDataModel myDataModel;

    private ScrollView scrollView;

    private String tiraURL;
    private String imgURL;
    private String cookie;
    private MyData myData;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        myDataModel =
                ViewModelProviders.of(getActivity()).get(MyDataModel.class);

        View root = inflater.inflate(R.layout.fragment_tubuyaki, container, false);

        //設定を読み込む
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        tiraURL = pref.getString("tiraura_resource", "");
        imgURL = pref.getString("img_resource", "");
        cookie = pref.getString("COOKIE", "");

        myData = new MyData(cookie);


        //スクロール状態を復元
        scrollView = root.findViewById(R.id.scrollView);

        myDataModel.getScroll().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer i) {
                if (i != null) {
                    scrollView.post(new Runnable() {
                        public void run() {
                            scrollView.setScrollY(myDataModel.getScroll().getValue());
                        }
                    });
                }
            }
        });

        final LinearLayout tubuyakiRoot = root.findViewById(R.id.tubuyaki_root);

        //スワイプで更新
        final SwipeRefreshLayout swipe = root.findViewById(R.id.swipe_refresh_layout);
        swipe.setColorSchemeResources(R.color.colorPrimaryDark);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                myDataModel.refresh(getContext());
            }
        });

        //スワイプで更新の操作説明
        LinearLayout getStart = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki_log_getstart, null);
        tubuyakiRoot.addView(getStart);

        //つぶやきデータを更新
        myDataModel.getMyData().observe(this, new Observer<MyData>() {
            @Override
            public void onChanged(@Nullable MyData mydata) {

                //つぶやき一覧を消去
                tubuyakiRoot.removeAllViews();

                //つぶやき一覧を表示
                if (mydata.getMynum() == 0) {
                    //操作説明
                    LinearLayout getStart = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki_log_getstart, null);
                    tubuyakiRoot.addView(getStart);

                } else {
                    for (final MyTubuyakiLog t : mydata.getMytubulog()) {

                        LinearLayout lt;

                        lt = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki_log, null);
                        tubuyakiRoot.addView(lt);

                        ImageView imageUnread = lt.findViewById(R.id.image_unread);
                        TextView textTdata = lt.findViewById(R.id.text_tdata);
                        TextView textTdate = lt.findViewById(R.id.text_tdate);
                        TextView textTres = lt.findViewById(R.id.text_tres);

                        if (!t.isUnreadFlag()) {
                            imageUnread.setVisibility(View.INVISIBLE);
                        }

                        textTdata.setText(Tubuyaki.format(t.getTdata()));

                        textTdata.setSingleLine();
                        textTdata.setEllipsize(TextUtils.TruncateAt.END);

                        textTdate.setText(t.getTdate());
                        textTres.setText("(" + t.getTres() + "レス)");

                        //タップしたらつぶやき画面に遷移
                        final int tno = t.getTno();
                        lt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openTubuyaki(tno);

                            }
                        });
                    }

                }
                swipe.setRefreshing(false);
            }
        });

        //myDataModel.refresh(getContext());

        return root;
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
                        openUser(t.getUid());
                        break;
                    case R.id.item_good:
                        new GoodTask().execute(tiraURL, cookie, String.valueOf(t.getTno()));
                        return true;
                    case R.id.item_res:
                        openPostActivity(t.getTno());
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

    public void openPostActivity(int tno) {

        //画面遷移
        Intent intent = new Intent(getActivity(), PostActivity.class);
        intent.putExtra("tno", tno);
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
        myDataModel.setScroll(scrollView.getScrollY());
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