package studio.wakaru.test2.util;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class Tiraura {

    public static final String TWO_HYPHENS = "--";
    public static final String BOUNDARY = "----" + UUID.randomUUID().toString();
    public static final String CRLF = "\r\n";

    public static String getXML(String urlStr, String cookies) {

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

    public static String getHTML(String urlStr, String cookies) {

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
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "EUC_JP"));
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


    public static String postTubuyaki(String urlStr, String cookies, String title, String name2, String cookieid, String t_session, String data, String hash, String hashcheck, String image, byte[] upfile) {

        String str = "";

        HashMap<String, String> kv = new HashMap<String, String>();
        kv.put("mode", "bbs_write");
        kv.put("Category", "CT01");
        kv.put("upform", "s");
        kv.put("Title", title);
        kv.put("Name2", name2);
        kv.put("cookieid", cookieid);
        kv.put("t_session", t_session);
        kv.put("Data", data);
        kv.put("hash", hash);
        kv.put("hashcheck", hashcheck);

        kv.put("image", image);
        //kv.put("upfile", upfile);

        return post(urlStr, cookies, kv, upfile);
    }

    public static String postRes(String urlStr, String cookies, String upform, String scount, String tubuid, String no, String title, String cookieid, String t_session, String nomove, String name, String data, String image, byte[] upfile) {

        String str = "";

        HashMap<String, String> kv = new HashMap<String, String>();
        kv.put("mode", "bbs_write");
        kv.put("Category", "CT01");
        kv.put("upform", upform);
        kv.put("scount", scount);
        kv.put("Title", title);
        kv.put("tubuid", tubuid);
        kv.put("f", "u");
        kv.put("no", no);
        kv.put("cookieid", cookieid);
        kv.put("t_session", t_session);
        kv.put("Nomove", nomove);

        kv.put("Name", name);
        kv.put("Data", data);

        kv.put("image", image);
        //kv.put("upfile", upfile);

        return post(urlStr, cookies, kv, upfile);

    }

    private static String post(String urlStr, String cookies, HashMap<String, String> kv, byte[] upfile) {

        HttpURLConnection con = null;
        OutputStream os;
        InputStream is = null;

        String str = "";

        try {
            URL url = new URL(urlStr);
            //URL url = new URL("https://en9dcm5enx2il.x.pipedream.net/");
            con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestMethod("POST");
            con.setRequestProperty("Host", url.getHost());
            con.setRequestProperty("Origin", url.getHost());
            con.setRequestProperty("Referer", url.getHost());
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            con.setRequestProperty("Cookie", cookies);


            os = con.getOutputStream();

            PrintStream ps = new PrintStream(os, true, "EUC-JP");

            Uri.Builder builder = new Uri.Builder();

            Set<String> keys = kv.keySet();
            for (String key : keys) {
                ps.print(TWO_HYPHENS + BOUNDARY);
                ps.print(CRLF);
                ps.print("Content-Disposition: form-data; name=\"" + key + "\"");
                ps.print(CRLF);

                ps.print(CRLF);
                ps.write(escape(kv.get(key), Charset.forName("EUC-JP")).getBytes(Charset.forName("EUC-JP")));
                ps.print(CRLF);
            }

            if (upfile != null) {
                ps.print(TWO_HYPHENS + BOUNDARY);
                ps.print(CRLF);
                ps.print("Content-Disposition: form-data; name=\"upfile\"; filename=\"" + kv.get("image") + "\"");
                ps.print(CRLF);
                ps.print("Content-Type: image/png");
                ps.print(CRLF);

                ps.print(CRLF);
                ps.write(upfile);
                ps.print(CRLF);

                Log.d("Tiraura", "upfile Upload");
            } else {
                ps.print(TWO_HYPHENS + BOUNDARY);
                ps.print("Content-Disposition: form-data; name=\"upfile\"; filename=\"\"");
                ps.print(CRLF);
                ps.print("Content-Type: application/octet-stream");
                ps.print(CRLF);

                ps.print(CRLF);
                ps.print("");
                ps.print(CRLF);

                Log.d("Tiraura", "upfile null");
            }

            ps.print(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS);

            ps.close();

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
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "EUC_JP"));
                String line;
                while ((line = br.readLine()) != null) {
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

    private static String escape(String str, Charset charset) {
        StringBuilder sb = new StringBuilder();
        CharsetEncoder encorder = charset.newEncoder();

        for (String s : str.split("")) {
            Log.d("Tiraura", s);

            if (encorder.canEncode(s)) {
                sb.append(s);
            } else {
                Log.d("Tiraura", "Can't Encode");
                sb.append("&#x" + Integer.toHexString(s.codePointAt(0)) + ";");
                Log.d("Tiraura", "Escape To " + "&#x" + Integer.toHexString(s.codePointAt(0)) + ";");
            }

        }

        return sb.toString();
    }

    public static String blank() {
        StringBuilder sb = new StringBuilder();
        int count = new Random().nextInt(1024);

        for (int i = 0; i < count; i++) {
            sb.append(" ");
        }

        return sb.toString();
    }
}
