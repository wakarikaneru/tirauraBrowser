package studio.wakaru.test2.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.picasso.Picasso;

import java.util.List;

import studio.wakaru.test2.R;
import studio.wakaru.test2.util.Tubuyaki;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        final LinearLayout tubuyakiRoot = root.findViewById(R.id.tubuyaki_root);

        //スワイプで更新
        final SwipeRefreshLayout swipe = root.findViewById(R.id.swipe_refresh_layout);
        swipe.setColorSchemeResources(R.color.colorPrimaryDark);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                homeViewModel.refresh();
            }
        });

        //スワイプで更新の操作説明
        LinearLayout getStart = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki_getstart, null);
        tubuyakiRoot.addView(getStart);

        //つぶやきデータを更新
        homeViewModel.getTubuyakiList().observe(this, new Observer<List<Tubuyaki>>() {
            @Override
            public void onChanged(@Nullable List<Tubuyaki> list) {

                //つぶやき一覧を消去
                tubuyakiRoot.removeAllViews();

                //つぶやき一覧を表示
                for (Tubuyaki t : list) {
                    LinearLayout lt = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki, null);
                    tubuyakiRoot.addView(lt);

                    TextView textTdata = lt.findViewById(R.id.text_tdata);
                    TextView textTdate = lt.findViewById(R.id.text_tdate);
                    TextView textUname = lt.findViewById(R.id.text_uname);
                    TextView textTres = lt.findViewById(R.id.text_tres);
                    TextView textTview = lt.findViewById(R.id.text_tview);
                    TextView textTgood = lt.findViewById(R.id.text_tgood);

                    ImageView imgUimg1 = lt.findViewById(R.id.img_uimg1);
                    ImageView imgTupfile1 = lt.findViewById(R.id.img_tupfile1);

                    // つぶやきを簡易表示
                    String tdata = t.getTdata();
                    String[] sa = tdata.split("\r\n|[\n\r\u2028\u2029\u0085]");
                    String tubuyaki = "";
                    for (int i = 0; i < Math.min(3, sa.length); i++) {
                        if (i != 0) {
                            tubuyaki += System.lineSeparator();
                        }
                        tubuyaki += sa[i];
                    }

                    if (100 < tubuyaki.length()) {
                        textTdata.setText(tubuyaki.substring(0, 100) + "…");
                    } else {
                        textTdata.setText(tubuyaki);
                    }

                    textTdate.setText(t.getTdate());
                    textUname.setText(t.getUname());
                    textTres.setText("(" + t.getTres() + "レス)");
                    textTview.setText("(" + t.getTview() + "チラ見)");
                    textTgood.setText("(" + t.getTgood() + "Good)");

                    Picasso.get().load("http://tiraura.orz.hm/usrimg/" + t.getUimg1()).into(imgUimg1);

                    if (t.getTupfile1().isEmpty()) {
                        Picasso.get().load("http://tiraura.orz.hm/usrimg/U201610050290969.png").into(imgTupfile1);
                        imgTupfile1.setVisibility(View.GONE);
                    } else {
                        Picasso.get().load("http://tiraura.orz.hm/usrimg/" + t.getTupfile1()).into(imgTupfile1);
                    }

                    // レス一覧を表示
                    LinearLayout resRoot = lt.findViewById(R.id.res_root);
                    if (t.getRes().size() <= 1) {
                        resRoot.setVisibility(View.GONE);
                    } else {
                        int count = 0;
                        for (Tubuyaki r : t.getRes()) {
                            if (t.getTno() != r.getTno()) {
                                count++;
                                if (t.getRes().size() - 3 <= count) {

                                    LinearLayout lr = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki_res, null);
                                    resRoot.addView(lr);

                                    TextView textRes = lr.findViewById(R.id.text_tdata);
                                    ImageView imgURes = lr.findViewById(R.id.img_uimg1);

                                    // レスを簡易表示
                                    String tdataRes = r.getTdata();
                                    String[] saRes = tdataRes.split("\r\n|[\n\r\u2028\u2029\u0085]");
                                    String tubuyakiRes = "";
                                    for (int i = 0; i < Math.min(2, saRes.length); i++) {
                                        if (i != 0) {
                                            tubuyakiRes += System.lineSeparator();
                                        }
                                        tubuyakiRes += saRes[i];
                                    }

                                    if (100 < tubuyakiRes.length()) {
                                        textRes.setText(tubuyakiRes.substring(0, 100) + "…");
                                    } else {
                                        textRes.setText(tubuyakiRes);
                                    }

                                    Picasso.get().load("http://tiraura.orz.hm/usrimg/" + r.getUimg1()).into(imgURes);
                                }
                            }
                        }
                    }
                }
                swipe.setRefreshing(false);
            }
        });


        return root;
    }

}