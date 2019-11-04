package studio.wakaru.test2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
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
import java.io.File;
import java.io.FileInputStream;
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

import static androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL;
import static androidx.exifinterface.media.ExifInterface.ORIENTATION_UNDEFINED;
import static androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION;

public class PostActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_STAMP = 1;

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
        final int tubuid = intent.getIntExtra("tubuid", 0);
        final int scount = intent.getIntExtra("scount", 0);

        if (tno == 0) {
            getSupportActionBar().setTitle("新規つぶやき");
        } else {
            getSupportActionBar().setTitle("ID:" + tno);
        }

        final TextView text = findViewById(R.id.text_tdata);

        final Switch sage = findViewById(R.id.switch_sage);

        //送信ボタンを押したら送信
        final Button submitButton = findViewById(R.id.button_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PostActivity", "submitButton onClick start");

                String submitText = text.getText().toString();

                if (submitText.isEmpty() && !upFile.isEmpty()) {
                    submitText = Tiraura.blank();
                }

                String sageStr = "";
                if (sage.isChecked()) {
                    sageStr = "on";
                }
                new PostTask().execute(tiraURL, cookie, String.valueOf(tno), String.valueOf(scount), String.valueOf(tubuid), String.valueOf(myData.getMynum()), myData.getMyname(), submitText, sageStr, upFile);
                Log.d("PostActivity", "submitButton onClick end");

                finish();
            }
        });


        //画像ボタンを押したら画像選択
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

        //スタンプボタンを押したらスタンプ選択
        final View stamp = findViewById(R.id.image_stamp);
        stamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PostActivity", "stamp onClick start");

                Intent intent = new Intent(getApplicationContext(), StampActivity.class);
                intent.putExtra("tno", tno);
                intent.putExtra("tubuid", tubuid);
                intent.putExtra("scount", scount);
                startActivityForResult(intent, REQUEST_STAMP);

                Log.d("PostActivity", "stamp onClick end");
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            try {
                final ImageView image_upfile = findViewById(R.id.image_upfile);

                //ファイルサイズ取得

                InputStream file = getContentResolver().openInputStream(data.getData());
                long size = file.available();

                Log.d("PostActivity", "data.getData().toString() " + data.getData().toString());
                Log.d("PostActivity", "size " + size);

                //画像取得設定
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                if (1 * 1024 * 1024 < size) {
                    Log.d("PostActivity", "1 * 1024 * 1024 " + size);
                    options.inSampleSize = 8;
                }

                //画像の向きを取得
                ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(data.getData()));
                int orientation = exif.getAttributeInt(TAG_ORIENTATION, ORIENTATION_NORMAL);

                BufferedInputStream inputStream = new BufferedInputStream(getContentResolver().openInputStream(data.getData()));
                Bitmap image = BitmapFactory.decodeStream(inputStream, null, options);

                Bitmap rotatedImage = rotateBitmap(image, orientation);

                image_upfile.setImageBitmap(rotatedImage);
                upFile = data.getData().toString();

                Log.d("PostActivity", "onActivityResult " + data.getData());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == REQUEST_STAMP && resultCode == RESULT_OK) {
            try {

                finish();

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
            String scount = params[3];
            String tubuid = params[4];
            String myNum = params[5];
            String myName = params[6];
            String tData = params[7];
            String sage = params[8];
            String upFile = params[9];

            //画像を処理
            Bitmap image = null;
            Bitmap rotatedImage = null;
            byte[] imageByteArray = null;
            String fileName = "";

            try {
                if (!upFile.isEmpty()) {
                    fileName = "image.png";

                    //画像取得設定
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inScaled = false;

                    //画像の向きを取得
                    ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(Uri.parse(upFile)));
                    int orientation = exif.getAttributeInt(TAG_ORIENTATION, ORIENTATION_NORMAL);

                    //画像を取得
                    ByteArrayOutputStream baos;

                    BufferedInputStream inputStream = new BufferedInputStream(getContentResolver().openInputStream(Uri.parse(upFile)));
                    image = BitmapFactory.decodeStream(inputStream, null, options);

                    rotatedImage = rotateBitmap(image, orientation);

                    baos = new ByteArrayOutputStream();
                    rotatedImage.compress(Bitmap.CompressFormat.PNG, 0, baos);
                    imageByteArray = baos.toByteArray();

                    //ファイルサイズを取得
                    int imageSize = imageByteArray.length;
                    Log.d("PostActivity", "imageSize " + imageSize / 1024 + "KB");

                    //ファイルサイズが大きい場合、圧縮をかける
                    int targetFileSize = (int) Math.floor(0.9 * 1024.0 * 1024.0);
                    imageByteArray = imageCompress(targetFileSize, imageByteArray);

                    //圧縮後のファイルサイズを取得
                    int compressedImageSize = imageByteArray.length;
                    Log.d("PostActivity", "imageSize compressed " + compressedImageSize / 1024 + "KB");

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

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

    public Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        int degree;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
            default:
                degree = 0;
                break;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        Bitmap rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotateBitmap;
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
                scale = (float) (scale / Math.sqrt((double) compressedImageSize / ((double) targetBytes * 0.9)));
            }
        }

        return compressedImageByteArray;
    }
}
