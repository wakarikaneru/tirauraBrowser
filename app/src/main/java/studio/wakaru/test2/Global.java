package studio.wakaru.test2;

import android.app.Application;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class Global extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //画層の読み込み設定
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, 1024 * 1024 * 1024));
        Picasso built = builder.build();
        Picasso.setSingletonInstance(built);

        //画像キャッシュのデバッグ
        //Picasso.get().setIndicatorsEnabled(true);

    }
}
