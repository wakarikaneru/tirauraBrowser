package studio.wakaru.test2.util;

import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;

public class Tiraura {

    public static String get(String urlStr, String cookies) {

        HttpURLConnection con = null;
        InputStream is = null;

        String str = "";

        try {
            URL url = new URL(urlStr);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Cookie", cookies);
            con.connect();
            is = con.getInputStream();

            // レスポンスコードの確認します。
            int responseCode = con.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP responseCode: " + responseCode);
            }

            // 文字列化します。
            StringBuilder sb = new StringBuilder();

            is = con.getInputStream();
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                str = sb.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (con != null) {
                con.disconnect();
            }
        }

        return str;
    }


    public static String postTubuyaki(String urlStr, String cookies, String title, String name2, String image, String cookieid, String t_session, String data, String hash) {

        HttpURLConnection con = null;
        OutputStream os;
        InputStream is = null;

        String str = "";

        HashMap kv = new HashMap<String, String>();
        kv.put("mode", "bbs_write");
        kv.put("category", "CT01");
        kv.put("upform", "s");
        kv.put("title", title);
        kv.put("name2", name2);
        kv.put("image", image);
        kv.put("cookieid", cookieid);
        kv.put("t_session", t_session);
        kv.put("data", data);
        kv.put("hash", hash);

        try {
            URL url = new URL(urlStr);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Cookie", cookies);

            con.connect();

            os = con.getOutputStream();

            Uri.Builder builder = new Uri.Builder();

            Set<String> keys = kv.keySet();
            for (String key : keys) {
                //[key=value]形式の文字列に変換する。
                builder.appendQueryParameter(key, (String) kv.get(key));
            }

            String join = builder.build().getEncodedQuery();
            PrintStream ps = new PrintStream(os);
            ps.print(join);
            ps.close();

            is = con.getInputStream();

            // レスポンスコードの確認します。
            int responseCode = con.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP responseCode: " + responseCode);
            }

            // 文字列化します。
            StringBuilder sb = new StringBuilder();

            is = con.getInputStream();
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                str = sb.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (con != null) {
                con.disconnect();
            }
        }

        return str;
    }
}
