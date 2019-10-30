package studio.wakaru.test2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import studio.wakaru.test2.util.Detector;
import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.TiraXMLMain;
import studio.wakaru.test2.util.Tiraura;

public class PostActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 0;

    private String tiraURL;
    private String imgURL;
    private String cookie;
    private MyData myData;

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

        myData = new MyData(cookie);

        upFile = "";

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

                Charset charset = Charset.forName("UTF-8");

                String testText = text.getText().toString();
                try {
                    InputStream is = new ByteArrayInputStream(testText.getBytes());
                    charset = Detector.getCharsetName(is);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                new AlertDialog.Builder(PostActivity.this)
                        .setTitle(charset.toString())
                        .setMessage(testText)
                        .setPositiveButton("OK", null)
                        .show();

                String submitText = text.getText().toString();

                String sageStr = "";
                if (sage.isChecked()) {
                    sageStr = "on";
                }
                new PostTask().execute(tiraURL, cookie, String.valueOf(tno), String.valueOf(myData.getMynum()), myData.getMyname(), submitText, sageStr, upFile);
                Log.d("PostActivity", "submitButton onClick end");

                //finish();
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
            String fileName = "image.png";

            try {
                if (!upFile.isEmpty()) {

                    //画像を取得
                    ByteArrayOutputStream baos;

                    BufferedInputStream inputStream = new BufferedInputStream(getContentResolver().openInputStream(Uri.parse(upFile)));
                    image = BitmapFactory.decodeStream(inputStream);

                    baos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.PNG, 0, baos);
                    imageByteArray = baos.toByteArray();

                    //ファイルサイズを取得
                    int imageSize = imageByteArray.length;
                    Log.d("PostActivity", "imageSize " + imageSize / 1024 + "KB");

                    //ファイルサイズが大きい場合、圧縮をかける
                    int targetFileSize = (int) Math.floor(1024.0 * 1024.0 * 0.9);
                    imageByteArray = imageCompress(targetFileSize, imageByteArray);

                    //圧縮後のファイルサイズを取得
                    int compressedImageSize = imageByteArray.length;
                    Log.d("PostActivity", "imageSize compressed " + compressedImageSize / 1024 + "KB");

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

    public static byte[] imageCompress(int targetBytes, byte[] imageByteArray) {

        byte[] compressedImageByteArray = null;
        Bitmap image = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);

        float scale = 1.0f;
        for (int i = 0; i < 10; i++) {
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);

            Bitmap compressedImage = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedImage.compress(Bitmap.CompressFormat.PNG, 1, baos);
            compressedImageByteArray = baos.toByteArray();

            int compressedImageSize = compressedImageByteArray.length;

            Log.d("PostActivity", "compress " + compressedImageSize / 1024 + "KB");
            if (compressedImageSize <= targetBytes) {
                break;
            } else {
                scale = scale / 2;
            }
        }

        return compressedImageByteArray;
    }
}
