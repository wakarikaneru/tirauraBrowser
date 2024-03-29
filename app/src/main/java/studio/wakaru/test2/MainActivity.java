package studio.wakaru.test2;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;


import java.util.List;

import studio.wakaru.test2.ui.RefreshableFragment;
import studio.wakaru.test2.ui.home.HomeFragment;
import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.TiraXMLMain;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class MainActivity extends AppCompatActivity {

    static final int SETTING = 1;  // The request code
    static final int LOGIN = 2;  // The request code

    private String tiraURL;
    private String xmlURL;
    private String imgURL;
    private String cookie;
    private int bootCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //設定読み込み
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        tiraURL = pref.getString("tiraura_resource", "");
        xmlURL = pref.getString("xml_resource", "");
        imgURL = pref.getString("img_resource", "");
        cookie = pref.getString("COOKIE", "");
        bootCount = pref.getInt("BOOT_COUNT", 0);

        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_new, R.id.navigation_search, R.id.navigation_tubuyaki, R.id.navigation_log_tubuyaki, R.id.navigation_log_res)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        //ナビゲーションを2度押しで更新
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Log.d("TEST", "item.isChecked() " + String.valueOf(item.isChecked()));
                NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);

                if (item.isChecked()) {
                    List<Fragment> fs = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment).getChildFragmentManager().getFragments();
                    for (Fragment f : fs) {
                        if (f != null && f instanceof RefreshableFragment) {
                            ((RefreshableFragment) f).refresh();
                        }
                    }
                    return true;
                } else {
                    switch (item.getItemId()) {
                        case R.id.navigation_new:
                            navController.navigate(R.id.action_global_navigation_new);
                            return true;
                        case R.id.navigation_search:
                            navController.navigate(R.id.action_global_navigation_search);
                            return true;
                        case R.id.navigation_tubuyaki:
                            navController.navigate(R.id.action_global_navigation_tubuyaki);
                            return true;
                        case R.id.navigation_log_tubuyaki:
                            navController.navigate(R.id.action_global_navigation_log_tubuyaki);
                            return true;
                        case R.id.navigation_log_res:
                            navController.navigate(R.id.action_global_navigation_log_res);
                            return true;
                    }
                }
                return false;
            }
        });

        if (bootCount == 0) {
            startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), SETTING);
        }


        //ログイン情報を取得
        cookie = pref.getString("COOKIE", "");
        new LoginTask().execute();

        //通知開始
        Intent intent = new Intent(getApplicationContext(), NotificationService.class);
        startService(intent);


        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("BOOT_COUNT", ++bootCount);
        editor.commit();

    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        tiraURL = pref.getString("tiraura_resource", "");
        xmlURL = pref.getString("xml_resource", "");
        imgURL = pref.getString("img_resource", "");
        cookie = pref.getString("COOKIE", "");

        //ダークモード
        if (pref.getBoolean("dark", false)) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //メニューを更新
        invalidateOptionsMenu();

    }

    @Override
    public void onBackPressed() {
        BottomNavigationView bnv = findViewById(R.id.nav_view);

        if (R.id.navigation_new == bnv.getSelectedItemId()) {
            finish();
        } else {
            bnv.setSelectedItemId(R.id.navigation_new);
        }
    }

    @Override
    public void finish() {
        new AlertDialog.Builder(this)
                .setTitle("終了しますか？")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // OK button pressed
                        MainActivity.super.finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            switch (item.getItemId()) {

                case R.id.action_settings:
                    break;

                case R.id.action_login:
                    item.setEnabled(!tiraURL.isEmpty());
                    break;

                case R.id.action_logout:
                    item.setEnabled(!cookie.isEmpty());
                    break;

                case R.id.action_tiraura:
                    item.setEnabled(!tiraURL.isEmpty());
                    break;

                default:
                    break;
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences pref;
        BottomNavigationView bnv;

        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_settings:
                startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), SETTING);

                return true;

            case R.id.action_login:
                if (!tiraURL.isEmpty()) {
                    //ログイン
                    startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), LOGIN);
                }
                return true;

            case R.id.action_logout:
                //ログアウト
                pref = PreferenceManager.getDefaultSharedPreferences(this);

                SharedPreferences.Editor editor = pref.edit();
                editor.putString("COOKIE", "");
                editor.commit();

                cookie = "";

                //メニューを更新
                invalidateOptionsMenu();

                //Fragmentを初期化
                bnv = findViewById(R.id.nav_view);
                bnv.setSelectedItemId(R.id.navigation_new);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        SharedPreferences pref;
        BottomNavigationView bnv;

        switch (requestCode) {
            case SETTING:

                //設定読み込み
                pref = PreferenceManager.getDefaultSharedPreferences(this);
                tiraURL = pref.getString("tiraura_resource", "");
                xmlURL = pref.getString("xml_resource", "");
                imgURL = pref.getString("img_resource", "");
                cookie = pref.getString("COOKIE", "");

                //メニューを更新
                invalidateOptionsMenu();

                //Fragmentを初期化
                bnv = findViewById(R.id.nav_view);
                bnv.setSelectedItemId(R.id.navigation_new);

                break;
            case LOGIN:

                //ログイン情報を取得
                pref = PreferenceManager.getDefaultSharedPreferences(this);
                cookie = pref.getString("COOKIE", "");
                new LoginTask().execute();

                //メニューを更新
                invalidateOptionsMenu();

                //Fragmentを初期化
                bnv = findViewById(R.id.nav_view);
                bnv.setSelectedItemId(R.id.navigation_new);

                break;
            default:
                break;
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
        protected void onPostExecute(MyData myData) {
            //Here you are done with the task

            if (0 < myData.getMynum()) {
                getSupportActionBar().setTitle(myData.getMyname());
                Toast.makeText(MainActivity.this, myData.getMyname() + "でログインしています", Toast.LENGTH_LONG).show();
            } else {
                getSupportActionBar().setTitle("ログインしていません");
                Toast.makeText(MainActivity.this, "ログインしていません", Toast.LENGTH_LONG).show();
            }

        }
    }
}
