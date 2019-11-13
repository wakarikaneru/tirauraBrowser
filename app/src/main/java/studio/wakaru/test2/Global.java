package studio.wakaru.test2;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class Global extends Application {

    private int cacheSize;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        cacheSize = Integer.parseInt(pref.getString("cache_size", "128"));

        //画層の読み込み設定
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, cacheSize * 1024 * 1024));
        Picasso built = builder.build();
        Picasso.setSingletonInstance(built);

        //画像キャッシュのデバッグ
        //Picasso.get().setIndicatorsEnabled(true);

    }
}
