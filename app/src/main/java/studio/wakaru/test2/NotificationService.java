package studio.wakaru.test2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import java.util.Timer;
import java.util.TimerTask;

import studio.wakaru.test2.util.MyData;
import studio.wakaru.test2.util.MyResLog;
import studio.wakaru.test2.util.MyTubuyakiLog;
import studio.wakaru.test2.util.TiraXMLMain;

public class NotificationService extends Service {

    public static int TERM = 5 * 60 * 1000;

    //
    public static int NOTICE_ID = 0;

    private String tiraURL;
    private String xmlURL;
    private String imgURL;
    private String cookie;

    private boolean checkTubuyaki;
    private boolean checkRes;
    private boolean checkNotice;
    private boolean checkMessage;

    public NotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // to do something
        Log.d("NotificationService", "onStartCommand");

        //設定読み込み
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        tiraURL = pref.getString("tiraura_resource", "");
        xmlURL = pref.getString("xml_resource", "");
        imgURL = pref.getString("img_resource", "");
        cookie = pref.getString("COOKIE", "");

        checkTubuyaki = pref.getBoolean("notice_tubuyaki", false);
        checkTubuyaki = pref.getBoolean("notice_tubuyaki", false);
        checkRes = pref.getBoolean("notice_res", false);
        checkNotice = pref.getBoolean("notice_notice", false);
        checkMessage = pref.getBoolean("notice_message", false);

        new NoticeTask().execute();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    //ログイン状況を取得
    private class NoticeTask extends AsyncTask<String, Void, MyData> {

        @Override
        protected MyData doInBackground(String... params) {
            //do your request in here so that you don't interrupt the UI thread

            //定期的に実行
            TimerTask task = new TimerTask() {
                int count = 0;

                @Override
                public void run() {
                    // Timerのスレッド
                    checkUnread();
                }
            };
            Timer t = new Timer();
            t.scheduleAtFixedRate(task, 0, TERM);

            return null;
        }

        @Override
        protected void onPostExecute(MyData myData) {
            //Here you are done with the task

        }


        public void loadSetting(Context c) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
            xmlURL = pref.getString("xml_resource", "");
            imgURL = pref.getString("img_resource", "");
            cookie = pref.getString("COOKIE", "");

            checkTubuyaki = pref.getBoolean("notice_tubuyaki", false);
            checkRes = pref.getBoolean("notice_res", false);
            checkNotice = pref.getBoolean("notice_notice", false);
            checkMessage = pref.getBoolean("notice_message", false);
        }

        private void checkUnread() {

            Log.d("NotificationService", "checkUnread");

            loadSetting(NotificationService.this);

            boolean notify = false;
            boolean tubu = false;
            boolean res = false;
            boolean notice = false;
            boolean message = false;


            String url = xmlURL + "?tn=2";
            MyData m = new TiraXMLMain(url, cookie).getMyData();
            if (checkTubuyaki) {
                for (MyTubuyakiLog tubuLog : m.getMytubulog()) {
                    if (tubuLog.isUnreadFlag()) {
                        notify = true;
                        tubu = true;
                    }
                }
            }
            if (checkRes) {
                for (MyResLog resLog : m.getMyreslog()) {
                    if (resLog.isUnreadFlag()) {
                        notify = true;
                        res = true;
                    }
                }
            }
            if (checkNotice) {
                if (0 < m.getMymcount2()) {
                    notify = true;
                    notice = true;
                }
            }
            if (checkMessage) {
                if (0 < m.getMymcount()) {
                    notify = true;
                    message = true;
                }
            }

            if (notify) {

                StringBuilder sb = new StringBuilder();
                if (tubu) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(System.lineSeparator());
                    }
                    sb.append("あなたのつぶやきにレスがつきました！！");
                }
                if (res) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(System.lineSeparator());
                    }
                    sb.append("レスしたつぶやきにレスがつきました！！");
                }
                if (notice) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(System.lineSeparator());
                    }
                    sb.append("未読のおしらせがあります！！");
                }
                if (message) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(System.lineSeparator());
                    }
                    sb.append("未読のメッセージがあります！！");
                }

                notice(sb.toString());
            } else {
                removeNotice();
            }
        }

        private void notice(String message) {

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);

            Notification notification = null;

            //システムから通知マネージャー取得
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            //アプリ名をチャンネルIDとして利用
            String appID = getString(R.string.app_name);
            String chID = "未読通知";

            //アンドロイドのバージョンで振り分け
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {     //APIが「26」以上の場合

                //通知チャンネルIDを生成してインスタンス化
                NotificationChannel notificationChannel = new NotificationChannel(appID, chID, NotificationManager.IMPORTANCE_DEFAULT);
                //通知の説明のセット
                notificationChannel.setDescription(chID);
                //通知チャンネルの作成
                notificationManager.createNotificationChannel(notificationChannel);
                //通知の生成と設定とビルド
                notification = new Notification.Builder(getApplicationContext(), appID)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message)
                        .setSmallIcon(R.drawable.ic_notify)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .build();
            } else {
                //APIが「25」以下の場合
                //通知の生成と設定とビルド
                notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message)
                        .setSmallIcon(R.drawable.ic_notify)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .build();
            }
            //通知の発行
            notificationManager.notify(NOTICE_ID, notification);

        }

        private void removeNotice() {
            //通知を消去
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NotificationService.NOTICE_ID);
        }
    }
}
