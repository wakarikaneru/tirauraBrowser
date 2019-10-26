package studio.wakaru.test2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;


import java.io.IOException;

import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.TiraXMLMain;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class MainActivity extends AppCompatActivity {

    private String tiraURL;
    private String xmlURL;
    private String imgURL;
    private String cookie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_new, R.id.navigation_tubuyaki, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        //設定読み込み
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        tiraURL = pref.getString("tiraura_resource", "");
        xmlURL = pref.getString("xml_resource", "");
        imgURL = pref.getString("img_resource", "");
        cookie = pref.getString("COOKIE", "");


    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        //ダークモード
        if (pref.getBoolean("dark", false)) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //ログイン情報を取得
        cookie = pref.getString("COOKIE", "");
        new LoginTask().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));

                return true;

            case R.id.action_login:
                if (!tiraURL.isEmpty()) {
                    //ブラウザ起動
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }

                return true;

            case R.id.action_tiraura:
                if (!tiraURL.isEmpty()) {
                    //ブラウザ起動
                    Uri uri = Uri.parse(tiraURL);
                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(i);
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    //ログイン状況を取得
    private class LoginTask extends AsyncTask<String, Void, MyData> {

        @Override
        protected MyData doInBackground(String... params) {
            //do your request in here so that you don't interrupt the UI thread
            MyData m = new TiraXMLMain(xmlURL, cookie).getMyData();
            if (m != null) {
                return m;
            }
            return null;
        }

        @Override
        protected void onPostExecute(MyData user) {
            //Here you are done with the task

            if (0 < user.getMynum()) {
                getSupportActionBar().setTitle(user.getMyname());
                Toast.makeText(MainActivity.this, user.getMyname() + "でログインしています", Toast.LENGTH_LONG).show();
            } else {
                getSupportActionBar().setTitle("ログインしていません");
                Toast.makeText(MainActivity.this, "ログインしていません", Toast.LENGTH_LONG).show();
            }

        }
    }
}
