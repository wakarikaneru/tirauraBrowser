package studio.wakaru.test2.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MyTubuyakiLog {
    //つぶやき番号||データ||日付||レス数||未読フラグ||画像フラグ||音声フラグ（||←小区切り　|-|←大区切り　適宜splitなどで処理を）フラグがたてば1　なしは0
    private int tno;
    private String tdata;
    private String tdate;
    private String tres;
    private boolean unreadFlag;
    private boolean imageFlag;
    private boolean soundFlag;

    public MyTubuyakiLog() {
    }

    public void setTno(int tno) {
        this.tno = tno;
    }

    public void setTdata(String tdata) {
        this.tdata = tdata;
    }

    public void setTdate(String tdate) {
        this.tdate = tdate;
    }

    public void setTres(String tres) {
        this.tres = tres;
    }

    public void setUnreadFlag(boolean unreadFlag) {
        this.unreadFlag = unreadFlag;
    }

    public void setImageFlag(boolean imageFlag) {
        this.imageFlag = imageFlag;
    }

    public void setSoundFlag(boolean soundFlag) {
        this.soundFlag = soundFlag;
    }

    public int getTno() {
        return tno;
    }

    public String getTdata() {
        return tdata;
    }

    public String getTdate() {
        return tdate;
    }

    public String getTres() {
        return tres;
    }

    public boolean isUnreadFlag() {
        return unreadFlag;
    }

    public boolean isImageFlag() {
        return imageFlag;
    }

    public boolean isSoundFlag() {
        return soundFlag;
    }

    public static MyTubuyakiLog getMyTubuyakiLog(String str) {
        MyTubuyakiLog mtl = new MyTubuyakiLog();

        String[] params = str.split("\\|\\|");

        try {
            mtl.setTno(Integer.parseInt(params[0]));
            mtl.setTdata(params[1]);
            mtl.setTdate(params[2]);
            mtl.setTres(params[3]);
            mtl.setUnreadFlag("1".equals(params[4]));
            mtl.setImageFlag("1".equals(params[5]));
            mtl.setSoundFlag("1".equals(params[6]));

        } catch (NumberFormatException e) {
            e.printStackTrace();
            mtl = null;
        }

        return mtl;
    }

    public static List<MyTubuyakiLog> getMyTubuyakiLogList(String str) {
        List<MyTubuyakiLog> list = new ArrayList<>();
        Log.d("MyTubuyakiLog", str);

        String[] mtllStr = str.split("\\|-\\|");

        for (String mtlStr : mtllStr) {
            if (!mtlStr.isEmpty()) {
                list.add(getMyTubuyakiLog(mtlStr));
            }
        }

        return list;
    }
}
