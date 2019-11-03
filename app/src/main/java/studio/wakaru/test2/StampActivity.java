package studio.wakaru.test2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayout;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.Tiraura;

public class StampActivity extends AppCompatActivity {

    public List<Integer> stamps;

    private String tiraURL;
    private String imgURL;
    private String cookie;
    private MyData myData;

    private Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stamp);

        //設定を読み込む
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        tiraURL = pref.getString("tiraura_resource", "");
        imgURL = pref.getString("img_resource", "");
        cookie = pref.getString("COOKIE", "");

        myData = new MyData(cookie);


        Intent intent = getIntent();
        final int tno = intent.getIntExtra("tno", 0);
        final int tubuid = intent.getIntExtra("tubuid", 0);
        final int scount = intent.getIntExtra("scount", 0);

        stamps = new ArrayList<>();
        stamps.add(R.drawable.stamp_fa027);
        stamps.add(R.drawable.stamp_fa028);
        stamps.add(R.drawable.stamp_fa029_k);
        stamps.add(R.drawable.stamp_fa029_t);
        stamps.add(R.drawable.stamp_fa030);


        FlexboxLayout flexbox = findViewById(R.id.flexbox);
        flexbox.setFlexDirection(FlexDirection.ROW);

        for (final int id : stamps) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(id);

            ViewGroup.LayoutParams lp =
                    new LinearLayout.LayoutParams(convertDpToPx(this, 128), convertDpToPx(this, 128));
            iv.setLayoutParams(lp);
            iv.setClickable(true);

            flexbox.addView(iv);

            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("StampActivity", "submitButton onClick start");


                    ImageView img = new ImageView(StampActivity.this);
                    img.setImageResource(id);

                    AlertDialog ad = new AlertDialog.Builder(StampActivity.this)
                            .setView(img)
                            .setPositiveButton("送信", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // OK button pressed
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inScaled = false;
                                    selectedImage = BitmapFactory.decodeResource(getResources(), id, options);

                                    new PostTask().execute(tiraURL, cookie, String.valueOf(tno), String.valueOf(scount), String.valueOf(tubuid), String.valueOf(myData.getMynum()), myData.getMyname(), Tiraura.blank(), "");

                                    Intent intent = getIntent();
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            })
                            .setCancelable(true)
                            .show();

                    Log.d("StampActivity", "submitButton onClick end");
                }
            });
        }


    }

    //非同期でレスをつける
    private class PostTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //do your request in here so that you don't interrupt the UI thread
            String url = params[0];
            String cookie = params[1];
            String tno = params[2];
            String scount = params[3];
            String tubuid = params[4];
            String myNum = params[5];
            String myName = params[6];
            String tData = params[7];
            String sage = params[8];

            //画像を処理
            byte[] imageByteArray = null;
            String fileName = "image.png";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG, 0, baos);
            imageByteArray = baos.toByteArray();

            String str = "";

            if (Integer.parseInt(tno) == 0) {
                str = Tiraura.postTubuyaki(url, cookie, "チラ裏ブラウザからのつぶやき", myName, myNum, "", tData, "tiraura", sage, fileName, imageByteArray);
            } else {
                str = Tiraura.postRes(url, cookie, tno, scount, tubuid, tno, "チラ裏ブラウザからのレス", myNum, "", sage, myName, tData, fileName, imageByteArray);
            }

            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            //Here you are done with the task
            Toast.makeText(getApplicationContext(), "送信しました", Toast.LENGTH_LONG).show();
        }
    }

    //pxをdpに置換
    private static int convertPxToDp(Context context, int px) {
        float d = context.getResources().getDisplayMetrics().density;
        return (int) ((px / d) + 0.5);
    }

    //dpをpxに置換
    public static int convertDpToPx(Context context, int dp) {
        float d = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * d) + 0.5);
    }
}
