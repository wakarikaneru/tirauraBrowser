package studio.wakaru.test2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.TiraXMLMain;
import studio.wakaru.test2.util.Tiraura;

public class PostActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 0;

    private String tiraURL;
    private String imgURL;
    private String cookie;

    private String upFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //設定を読み込む
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        tiraURL = pref.getString("tiraura_resource", "");
        imgURL = pref.getString("img_resource", "");
        cookie = pref.getString("COOKIE", "");

        upFile = "";

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

        if (tno == 0) {
            getSupportActionBar().setTitle("新規つぶやき");
        } else {
            getSupportActionBar().setTitle("ID:" + tno);
        }

        final TextView text = findViewById(R.id.text_tdata);

        final Switch sage = findViewById(R.id.switch_sage);

        //送信ボタンを押したらデータを返す
        final Button submitButton = findViewById(R.id.button_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PostActivity", "submitButton onClick start");
                String submitText = text.getText().toString();

                String sageStr = "";
                if (sage.isChecked()) {
                    sageStr = "on";
                }
                new PostTask().execute(tiraURL, cookie, String.valueOf(tno), String.valueOf(myData.getMynum()), myData.getMyname(), submitText, sageStr, upFile);
                Log.d("PostActivity", "submitButton onClick end");

                finish();
            }
        });


        //送信ボタンを押したらデータを返す
        final Button imageButton = findViewById(R.id.button_image);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PostActivity", "imageButton onClick start");

                Intent intentGallery;
                intentGallery = new Intent(Intent.ACTION_GET_CONTENT);
                intentGallery.setType("image/*");
                startActivityForResult(intentGallery, REQUEST_GALLERY);

                Log.d("PostActivity", "imageButton onClick end");
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            try {
                final ImageView image_upfile = findViewById(R.id.image_upfile);

                BufferedInputStream inputStream = new BufferedInputStream(getContentResolver().openInputStream(data.getData()));
                Bitmap image = BitmapFactory.decodeStream(inputStream);

                image_upfile.setImageBitmap(image);
                upFile = data.getData().toString();

                Log.d("PostActivity", "onActivityResult " + data.getData());
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            String myNum = params[3];
            String myName = params[4];
            String tData = params[5];
            String sage = params[6];
            String upFile = params[7];

            //画像を処理
            Bitmap image = null;
            byte[] imageByteArray = null;
            String fileName = "image.jpg";

            try {
                if (!upFile.isEmpty()) {
                    ByteArrayOutputStream baos;

                    BufferedInputStream inputStream = new BufferedInputStream(getContentResolver().openInputStream(Uri.parse(upFile)));
                    image = BitmapFactory.decodeStream(inputStream);

                    //ファイルサイズを取得
                    baos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    imageByteArray = baos.toByteArray();
                    int imageSize = imageByteArray.length;
                    Log.d("PostActivity", "imageSize " + imageSize / 1024 + "KB");

                    //ファイルサイズが大きい場合、圧縮をかける
                    if (1024 * 1024 < imageSize) {
                        double targetBytes = 1024.0 * 1024.0 * 0.9;
                        double ratio = imageSize / targetBytes;
                        Log.d("PostActivity", "imageSize ratio " + ratio);
                        int quality = (int) Math.floor(100.0 / ratio);
                        Log.d("PostActivity", "imageSize setQuality " + quality);

                        baos = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                        imageByteArray = baos.toByteArray();
                        image = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                        int compressedImageSize = imageByteArray.length;

                        Log.d("PostActivity", "imageSize compressed " + compressedImageSize / 1024 + "KB");
                    }

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            String str = "";

            if (Integer.parseInt(tno) == 0) {
                str = Tiraura.postTubuyaki(url, cookie, "チラ裏ブラウザからのつぶやき", myName, myNum, "", tData, "tiraura", sage, fileName, imageByteArray);
            } else {
                str = Tiraura.postRes(url, cookie, tno, "0", myNum, tno, "チラ裏ブラウザからのレス", myNum, "", sage, myName, tData, fileName, imageByteArray);
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
