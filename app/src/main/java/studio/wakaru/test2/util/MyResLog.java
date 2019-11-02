package studio.wakaru.test2.util;

import java.util.ArrayList;
import java.util.List;

public class MyResLog {
    //つぶやき番号||レス数||レス||未読フラグ|-|というフォーマット（||←小区切り　|-|←大区切り　適宜splitなどで処理を）フラグがたてば1　なしは0
    private int tno;
    private String tdata;
    private String tres;
    private boolean unreadFlag;

    public MyResLog() {
    }

    public void setTno(int tno) {
        this.tno = tno;
    }

    public void setTdata(String tdata) {
        this.tdata = tdata;
    }

    public void setTres(String tres) {
        this.tres = tres;
    }

    public void setUnreadFlag(boolean unreadFlag) {
        this.unreadFlag = unreadFlag;
    }

    public int getTno() {
        return tno;
    }

    public String getTdata() {
        return tdata;
    }

    public String getTres() {
        return tres;
    }

    public boolean isUnreadFlag() {
        return unreadFlag;
    }

    public static MyResLog getMyResLog(String str) {
        MyResLog mrl = new MyResLog();

        String[] params = str.split("||");

        try {
            mrl.setTno(Integer.parseInt(params[0]));
            mrl.setTres(params[1]);
            mrl.setTdata(params[2]);
            mrl.setUnreadFlag("1".equals(params[3]));

        } catch (NumberFormatException e) {
            e.printStackTrace();
            mrl = null;
        }

        return mrl;
    }

    public static List<MyResLog> getMyResList(String str) {
        List<MyResLog> list = new ArrayList<>();

        String[] mrllStr = str.split("|-|");

        for (String mrlStr : mrllStr) {
            if (!mrlStr.isEmpty()) {
                list.add(getMyResLog(mrlStr));
            }
        }

        return list;
    }
}
