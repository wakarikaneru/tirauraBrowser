package studio.wakaru.test2.util;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class MyData {
    private int mynum;
    private String myname;
    private int mytubu;
    private int mytira;
    private String myimg1;
    private String myimg2;
    private String myimg3;
    private String mybimg1;
    private String mybimg2;
    private String mybimg3;
    private String myhash;
    private int mypoint;
    private int mymcount;
    private int mymcount2;
    private String mytubulog;
    private String myreslog;

    public MyData() {
    }

    public MyData(String cookie) {
        //cookieからログイン情報を取得
        Log.d("PostActivity", cookie);
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
            try {
                this.mynum = Integer.parseInt(loginCheck);
                this.myname = name;
            } catch (NumberFormatException e) {
                e.printStackTrace();

                this.mynum = 0;
                this.myname = "";
            }
        }
    }

    public void setMynum(int mynum) {
        this.mynum = mynum;
    }

    public void setMyname(String myname) {
        this.myname = myname;
    }

    public void setMytubu(int mytubu) {
        this.mytubu = mytubu;
    }

    public void setMytira(int mytira) {
        this.mytira = mytira;
    }

    public void setMyimg1(String myimg1) {
        this.myimg1 = myimg1;
    }

    public void setMyimg2(String myimg2) {
        this.myimg2 = myimg2;
    }

    public void setMyimg3(String myimg3) {
        this.myimg3 = myimg3;
    }

    public void setMybimg1(String mybimg1) {
        this.mybimg1 = mybimg1;
    }

    public void setMybimg2(String mybimg2) {
        this.mybimg2 = mybimg2;
    }

    public void setMybimg3(String mybimg3) {
        this.mybimg3 = mybimg3;
    }

    public void setMyhash(String myhash) {
        this.myhash = myhash;
    }

    public void setMypoint(int mypoint) {
        this.mypoint = mypoint;
    }

    public void setMymcount(int mymcount) {
        this.mymcount = mymcount;
    }

    public void setMymcount2(int mymcount2) {
        this.mymcount2 = mymcount2;
    }

    public void setMytubulog(String mytubulog) {
        this.mytubulog = mytubulog;
    }

    public void setMyreslog(String myreslog) {
        this.myreslog = myreslog;
    }

    public int getMynum() {
        return mynum;
    }

    public String getMyname() {
        return myname;
    }

    public int getMytubu() {
        return mytubu;
    }

    public int getMytira() {
        return mytira;
    }

    public String getMyimg1() {
        return myimg1;
    }

    public String getMyimg2() {
        return myimg2;
    }

    public String getMyimg3() {
        return myimg3;
    }

    public String getMybimg1() {
        return mybimg1;
    }

    public String getMybimg2() {
        return mybimg2;
    }

    public String getMybimg3() {
        return mybimg3;
    }

    public String getMyhash() {
        return myhash;
    }

    public int getMypoint() {
        return mypoint;
    }

    public int getMymcount() {
        return mymcount;
    }

    public int getMymcount2() {
        return mymcount2;
    }

    public String getMytubulog() {
        return mytubulog;
    }

    public String getMyreslog() {
        return myreslog;
    }
}
