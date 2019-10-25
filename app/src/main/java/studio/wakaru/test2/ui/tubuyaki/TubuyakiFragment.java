package studio.wakaru.test2.ui.tubuyaki;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.text.util.Linkify;
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
import androidx.core.text.HtmlCompat;
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

    private String tiraURL;
    private String imgURL;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        tubuyakiViewModel =
                ViewModelProviders.of(getActivity()).get(TubuyakiViewModel.class);

        //設定を読み込む
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        tiraURL = pref.getString("tiraura_resource", "");
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
                tubuyakiViewModel.refresh(getContext());
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

                            textTdata.setAutoLinkMask(Linkify.WEB_URLS);
                            //textTdata.setText(HtmlCompat.fromHtml(t.getTdata(),HtmlCompat.FROM_HTML_MODE_COMPACT));
                            textTdata.setText(Tubuyaki.format(t.getTdata()));

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

                                    if (!tiraURL.isEmpty()) {
                                        //ブラウザ起動
                                        Uri uri = Uri.parse(tiraURL + "?mode=bbsdata_view&Category=CT01&newdata=1&id=" + tno);
                                        Intent i = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(i);
                                    }

                                    return true;
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

                            textTdata.setAutoLinkMask(Linkify.WEB_URLS);
                            //textTdata.setText(HtmlCompat.fromHtml(t.getTdata(),HtmlCompat.FROM_HTML_MODE_COMPACT));
                            textTdata.setText(Tubuyaki.format(t.getTdata()));

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
                                            .setPositiveButton(android.R.string.ok, null)
                                            .setCancelable(true)
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

        //tubuyakiViewModel.refresh(getContext());

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        tubuyakiViewModel.setScroll(mScrollView.getScrollY());
    }
}