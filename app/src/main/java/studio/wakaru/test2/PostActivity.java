package studio.wakaru.test2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.TiraXMLMain;
import studio.wakaru.test2.util.Tiraura;

public class PostActivity extends AppCompatActivity {

    private String tiraURL;
    private String imgURL;
    private String cookie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //設定を読み込む
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        tiraURL = pref.getString("tiraura_resource", "");
        imgURL = pref.getString("img_resource", "");
        cookie = pref.getString("COOKIE", "");

        //cookieからログイン情報を取得
        Log.d("PostActivity", cookie);
        Map<String, String> kv = new HashMap<>();
        final MyData myData = new MyData();
        if (!cookie.isEmpty()) {
            String[] cookies = cookie.split("=");
            String[] cookieDataList = cookies[1].split(",");

            for (String s : cookieDataList) {
                String[] cookieKV = s.split(":");
                if (1 <= cookieKV.length) {
                    String k = cookieKV[0];
                    String v = "";
                    if (2 <= cookieKV.length) {
                        v = cookieKV[1];
                    }
                    kv.put(k, v);
                    Log.d("PostActivity", k + " = " + v);
                }
            }
        }
        myData.setMynum(Integer.parseInt(kv.get("login_check")));
        myData.setMyname(kv.get("name"));

        Intent intent = getIntent();
        final int tno = intent.getIntExtra("tno", 0);

        getSupportActionBar().setTitle("ID:" + tno);

        final TextView text = findViewById(R.id.text_tdata);

        //送信ボタンを押したらデータを返す
        final Button submit = findViewById(R.id.button_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PostActivity", "onClick start");
                String submitText = text.getText().toString();

                new PostTask().execute(tiraURL, cookie, String.valueOf(tno), String.valueOf(myData.getMynum()), myData.getMyname(), submitText);
                Log.d("PostActivity", "onClick end");

                finish();
            }
        });


    }

    //非同期でレスをつける
    private class PostTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //do your request in here so that you don't interrupt the UI thread
            String url = params[0];
            String cookie = params[1];
            String tno = params[2];
            String myNum = params[3];
            String myName = params[4];
            String tData = params[5];

            String str = "";

            if (Integer.parseInt(tno) == 0) {
                str = Tiraura.postTubuyaki(url, cookie, "チラ裏ブラウザからのつぶやき", myName, "", myNum, "", tData, "tiraura", null);
            } else {
                str = Tiraura.postRes(url, cookie, tno, "0", myNum, tno, "チラ裏ブラウザからのレス", myNum, "", myName, tData, null);
            }

            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            //Here you are done with the task
            Toast.makeText(getApplicationContext(), "送信しました", Toast.LENGTH_LONG).show();
        }
    }
}
