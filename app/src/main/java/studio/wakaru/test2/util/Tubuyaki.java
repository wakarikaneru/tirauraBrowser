package studio.wakaru.test2.util;


import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

public class Tubuyaki {
    /*
        <tno>チラ裏番号
        <tdate>投稿時間
        <tdate2>更新時間
        <uname>投稿ユーザー名
        <uid>投稿ユーザーID
        <uimg1>投稿ユーザーサムネ（実画像）
        <uimg2>投稿ユーザーサムネ（圧縮jpg画像-極小）
        <uimg3>投稿ユーザーサムネ（圧縮jpg画像-小）
        <tdata>つぶやき本文
        <tres>レス数
        <tview>閲覧数
        <thash>ハッシュ
        <parent>親つぶやき番号（つぶやきの場合は全て1）
        <tupfile1>アップファイル（実画像）
        <tupfile2>アップファイル（圧縮jpg画像-極小）
        <tupfile3>アップファイル（圧縮jpg画像-小）
        <nanashiid>名無しID5831のみID表示
        <tgood>グッド数
        <gooder>グッドしたユーザー一覧　id||名前||画像ファイル名|-|というフォーマット（||←小区切り　|-|←大区切り　適宜splitなどで処理を）
     */

    private int tno;
    private String tdate;
    private String tdate2;
    private String uname;
    private int uid;
    private String uimg1;
    private String uimg2;
    private String uimg3;
    private String tdata;
    private int tres;
    private int tview;
    private String thash;
    private int parent;
    private String tupfile1;
    private String tupfile2;
    private String tupfile3;
    private String nanashiid;
    private int tgood;
    private String gooder;

    public List<Tubuyaki> res;

    public Tubuyaki() {
        res = new ArrayList<>();
    }

    public List<Tubuyaki> getRes() {
        return res;
    }

    public void setRes(List<Tubuyaki> res) {
        this.res = res;
    }


    public void formatTdata() {
        tdata = format(tdata);
    }

    public static String format(String str) {
        String f = str;
        f = f.replaceAll("<br>", System.lineSeparator());
        f = f.replaceAll("\\<[^>]*>", "");

        return f;
    }

    public void setTno(int tno) {
        this.tno = tno;
    }

    public void setTdate(String tdate) {
        this.tdate = tdate;
    }

    public void setTdate2(String tdate2) {
        this.tdate2 = tdate2;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setUimg1(String uimg1) {
        this.uimg1 = uimg1;
    }

    public void setUimg2(String uimg2) {
        this.uimg2 = uimg2;
    }

    public void setUimg3(String uimg3) {
        this.uimg3 = uimg3;
    }

    public void setTdata(String tdata) {
        this.tdata = tdata;
    }

    public void setTres(int tres) {
        this.tres = tres;
    }

    public void setTview(int tview) {
        this.tview = tview;
    }

    public void setThash(String thash) {
        this.thash = thash;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public void setTupfile1(String tupfile1) {
        this.tupfile1 = tupfile1;
    }

    public void setTupfile2(String tupfile2) {
        this.tupfile2 = tupfile2;
    }

    public void setTupfile3(String tupfile3) {
        this.tupfile3 = tupfile3;
    }

    public void setNanashiid(String nanashiid) {
        this.nanashiid = nanashiid;
    }

    public void setTgood(int tgood) {
        this.tgood = tgood;
    }

    public void setGooder(String gooder) {
        this.gooder = gooder;
    }

    public int getTno() {
        return tno;
    }

    public String getTdate() {
        return tdate;
    }

    public String getTdate2() {
        return tdate2;
    }

    public String getUname() {
        return uname;
    }

    public int getUid() {
        return uid;
    }

    public String getUimg1() {
        return uimg1;
    }

    public String getUimg2() {
        return uimg2;
    }

    public String getUimg3() {
        return uimg3;
    }

    public String getTdata() {
        return tdata;
    }

    public int getTres() {
        return tres;
    }

    public int getTview() {
        return tview;
    }

    public String getThash() {
        return thash;
    }

    public int getParent() {
        return parent;
    }

    public String getTupfile1() {
        return tupfile1;
    }

    public String getTupfile2() {
        return tupfile2;
    }

    public String getTupfile3() {
        return tupfile3;
    }

    public String getNanashiid() {
        return nanashiid;
    }

    public int getTgood() {
        return tgood;
    }

    public String getGooder() {
        return gooder;
    }

}
