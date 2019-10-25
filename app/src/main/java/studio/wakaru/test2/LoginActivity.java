package studio.wakaru.test2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;

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
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Cookieを取得
                String cookie = CookieManager.getInstance().getCookie(url);
                if (cookie != null) {

                    String[] cookies = cookie.split(",");
                    for (String c : cookies) {
                        String[] kv = c.split(":");

                        if ("login_check".equals(kv[0])) {
                            if (1 < kv.length) {
                                int id = Integer.parseInt(kv[1]);
                                Log.d("LoginActivity", "Login!");

                                //クッキーを保存
                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(view.getContext());

                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("COOKIE", cookie);
                                editor.commit();

                                Toast.makeText(view.getContext(), "ユーザID" + id + "でログインしました", Toast.LENGTH_LONG).show();

                                finish();
                            }
                        }
                    }
                }
            }
        });

    }
}
