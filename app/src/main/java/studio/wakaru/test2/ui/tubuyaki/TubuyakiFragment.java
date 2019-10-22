package studio.wakaru.test2.ui.tubuyaki;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.picasso.Picasso;

import java.util.List;

import studio.wakaru.test2.R;
import studio.wakaru.test2.ui.home.HomeViewModel;
import studio.wakaru.test2.util.Tubuyaki;

public class TubuyakiFragment extends Fragment {

    private TubuyakiViewModel tubuyakiViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        tubuyakiViewModel =
                ViewModelProviders.of(this).get(TubuyakiViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

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

        //スワイプで更新の操作説明
        LinearLayout getStart = (LinearLayout) getLayoutInflater().inflate(R.layout.tubuyaki_getstart, null);
        tubuyakiRoot.addView(getStart);

        //つぶやきデータを更新
        tubuyakiViewModel.getTubuyakiList().observe(this, new Observer<List<Tubuyaki>>() {
            @Override
            public void onChanged(@Nullable List<Tubuyaki> list) {

                //つぶやき一覧を消去
                tubuyakiRoot.removeAllViews();

                //つぶやき一覧を表示
                int resCount = 0;
                for (Tubuyaki t : list) {
                    LinearLayout lt = (LinearLayout) getLayoutInflater().inflate(R.layout.res, null);
                    tubuyakiRoot.addView(lt);

                    TextView textResNo = lt.findViewById(R.id.text_resNo);

                    TextView textTdata = lt.findViewById(R.id.text_tdata);
                    TextView textTdate = lt.findViewById(R.id.text_tdate);
                    TextView textUname = lt.findViewById(R.id.text_uname);
                    //TextView textTres = lt.findViewById(R.id.text_tres);
                    //TextView textTview = lt.findViewById(R.id.text_tview);
                    TextView textTgood = lt.findViewById(R.id.text_tgood);

                    ImageView imgUimg1 = lt.findViewById(R.id.img_uimg1);
                    ImageView imgTupfile1 = lt.findViewById(R.id.img_tupfile1);


                    textResNo.setText(String.valueOf(resCount));

                    textTdata.setText(t.getTdata());
                    textTdate.setText(t.getTdate());
                    textUname.setText(t.getUname());
                    //textTres.setText("(" + t.getTres() + "レス)");
                    //textTview.setText("(" + t.getTview() + "チラ見)");
                    textTgood.setText("(" + t.getTgood() + "Good)");

                    Picasso.get().load("http://tiraura.orz.hm/usrimg/" + t.getUimg1()).into(imgUimg1);

                    if (t.getTupfile1().isEmpty()) {
                        imgTupfile1.setVisibility(View.GONE);
                    } else {
                        Picasso.get().load("http://tiraura.orz.hm/usrimg/" + t.getTupfile1()).into(imgTupfile1);
                    }

                    resCount++;
                }
                swipe.setRefreshing(false);
            }
        });

        //更新する
        Bundle bundle = getArguments();
        if (bundle != null) {
            tubuyakiViewModel.setTno(bundle.getInt("tno"));
        }
        tubuyakiViewModel.refresh();

        return root;
    }

}