package studio.wakaru.test2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private String tiraURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //設定読み込み
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        tiraURL = pref.getString("tiraura_resource", "");

        //ログイン画面を開く
        final WebView webView = findViewById(R.id.web_view);

        Uri uri = Uri.parse(tiraURL + "?mode=logout");

        if (!tiraURL.isEmpty()) {
            webView.loadUrl(uri.toString());
        } else {
            finish();
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Cookieを取得
                String cookie = CookieManager.getInstance().getCookie(url);

                if (cookie != null) {
                    Map<String, String> kv = new HashMap<>();

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
                                Log.d("MyData", k + " = " + v);
                            }
                        }
                    }

                    //ログインチェック
                    String loginCheck = kv.get("login_check");
                    String name = kv.get("name");

                    if (loginCheck != null) {
                        if (!loginCheck.isEmpty()) {

                            //クッキーを保存
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(view.getContext());

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("COOKIE", cookie);
                            editor.commit();

                            Toast.makeText(view.getContext(), "ユーザID" + loginCheck + "でログインしました", Toast.LENGTH_LONG).show();

                            finish();
                        }
                    }
                }
            }
        });

    }
}
