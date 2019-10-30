package studio.wakaru.test2.util;

import android.util.Log;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class Detector {
    public static Charset getCharsetName(InputStream is) throws IOException {
        //4kbのメモリバッファを確保する
        byte[] buf = new byte[4096];
        UniversalDetector detector = new UniversalDetector(null);

        //文字コードの推測結果が得られるまでInputStreamを読み進める
        int nread;
        while ((nread = is.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }

        //推測結果を取得する
        detector.dataEnd();
        final String detectedCharset = detector.getDetectedCharset();

        detector.reset();

        if (detectedCharset != null) {
            Log.d("Detector", detectedCharset);
            return Charset.forName(detectedCharset);
        }
        //文字コードを取得できなかった場合、環境のデフォルトを使用する
        return Charset.forName(System.getProperty("file.encoding"));
    }
}
