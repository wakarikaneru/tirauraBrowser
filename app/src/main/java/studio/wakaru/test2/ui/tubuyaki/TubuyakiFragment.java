package studio.wakaru.test2.ui.tubuyaki;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.picasso.Picasso;

import java.util.List;

import studio.wakaru.test2.R;
import studio.wakaru.test2.util.Good;
import studio.wakaru.test2.util.Tubuyaki;

public class TubuyakiFragment extends Fragment {

    private TubuyakiViewModel tubuyakiViewModel;

    private ScrollView mScrollView;

    private String imgURL;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        tubuyakiViewModel =
                ViewModelProviders.of(getActivity()).get(TubuyakiViewModel.class);

        //設定を読み込む
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        imgURL = pref.getString("img_resource", "");

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mScrollView = root.findViewById(R.id.scrollView);

        //スクロール状態を復元
        tubuyakiViewModel.getScroll().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer i) {
                if (i != null) {
                    mScrollView.post(new Runnable() {
                        public void run() {
                            mScrollView.setScrollY(tubuyakiViewModel.getScroll().getValue());
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
                tubuyakiViewModel.refresh();
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
                    for (Tubuyaki t : list) {
                        LinearLayout lt;
                        if (resCount <= 0) {
                            lt = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki, null);
                            tubuyakiRoot.addView(lt);

                            TextView textResNo = lt.findViewById(R.id.text_resNo);

                            TextView textTdata = lt.findViewById(R.id.text_tdata);
                            TextView textTdate = lt.findViewById(R.id.text_tdate);
                            TextView textUname = lt.findViewById(R.id.text_uname);
                            TextView textTres = lt.findViewById(R.id.text_tres);
                            TextView textTview = lt.findViewById(R.id.text_tview);
                            TextView textTgood = lt.findViewById(R.id.text_tgood);
                            TextView textTgood2 = lt.findViewById(R.id.text_tgood2);

                            ImageView imgUimg1 = lt.findViewById(R.id.img_uimg1);
                            ImageView imgTupfile1 = lt.findViewById(R.id.img_tupfile1);


                            textResNo.setText(String.valueOf(resCount));

                            textTdata.setText(t.getTdata());
                            textTdate.setText(t.getTdate());
                            textUname.setText(t.getUname());
                            textTres.setText("(" + t.getTres() + "レス)");
                            textTview.setText("(" + t.getTview() + "チラ見)");
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

                                    new AlertDialog.Builder(getActivity())
                                            .setView(img)
                                            .setPositiveButton("OK", null)
                                            .show();
                                }
                            });

                        } else {
                            lt = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki, null);
                            tubuyakiRoot.addView(lt);

                            TextView textResNo = lt.findViewById(R.id.text_resNo);

                            TextView textTdata = lt.findViewById(R.id.text_tdata);
                            TextView textTdate = lt.findViewById(R.id.text_tdate);
                            TextView textUname = lt.findViewById(R.id.text_uname);
                            TextView textTres = lt.findViewById(R.id.text_tres);
                            TextView textTview = lt.findViewById(R.id.text_tview);
                            TextView textTgood = lt.findViewById(R.id.text_tgood);
                            TextView textTgood2 = lt.findViewById(R.id.text_tgood2);

                            ImageView imgUimg1 = lt.findViewById(R.id.img_uimg1);
                            ImageView imgTupfile1 = lt.findViewById(R.id.img_tupfile1);


                            textResNo.setText(String.valueOf(resCount));

                            textTdata.setText(t.getTdata());
                            textTdate.setText(t.getTdate());
                            textUname.setText(t.getUname());
                            textTres.setVisibility(View.GONE);//textTres.setText("(" + t.getTres() + "レス)");
                            textTview.setVisibility(View.GONE);//textTview.setText("(" + t.getTview() + "チラ見)");
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

                                    new AlertDialog.Builder(getActivity())
                                            .setView(img)
                                            .setPositiveButton("OK", null)
                                            .show();
                                }
                            });

                        }
                        resCount++;
                    }
                }
                swipe.setRefreshing(false);
            }
        });

        //tnoを設定する
        Bundle bundle = getArguments();
        if (bundle != null) {
            tubuyakiViewModel.setTno(bundle.getInt("tno"));
        }

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        tubuyakiViewModel.setScroll(mScrollView.getScrollY());
    }
}