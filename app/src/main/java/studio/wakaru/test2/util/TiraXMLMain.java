package studio.wakaru.test2.util;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class TiraXMLMain {

    Element element;

    public TiraXMLMain() {
    }

    public TiraXMLMain(String url) {
        loadTiraXML(url);
    }

    public TiraXMLMain(String url, String cookies) {
        loadTiraXMLWithCookies(url, cookies);
    }

    public TiraXMLMain loadTiraXML(String url) {

        Document document = null;
        try {
            // 1. DocumentBuilderFactoryのインスタンスを取得する
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 2. DocumentBuilderのインスタンスを取得する
            DocumentBuilder builder = factory.newDocumentBuilder();
            // 3. DocumentBuilderにXMLを読み込ませ、Documentを作る
            document = builder.parse(url);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            if (document == null) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                DOMImplementation dom = builder.getDOMImplementation();
                document = dom.createDocument(null, "tiraura", null);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        // 4. Documentから、ルート要素を取得する
        this.element = document.getDocumentElement();

        return this;
    }

    public TiraXMLMain loadTiraXMLWithCookies(String urlStr, String cookies) {

        Document document = null;

        String xmlStr = "";

        xmlStr = Tiraura.getXML(urlStr, cookies);

        Log.d("TiraXMLMain", xmlStr);

        try {
            // 1. DocumentBuilderFactoryのインスタンスを取得する
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 2. DocumentBuilderのインスタンスを取得する
            DocumentBuilder builder = factory.newDocumentBuilder();
            // 3. DocumentBuilderにXMLを読み込ませ、Documentを作る
            document = builder.parse(new InputSource(new StringReader(xmlStr)));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            if (document == null) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                DOMImplementation dom = builder.getDOMImplementation();
                document = dom.createDocument(null, "tiraura", null);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        // 4. Documentから、ルート要素を取得する
        this.element = document.getDocumentElement();

        return this;
    }

    public Element getElement() {
        return element;
    }

    public List<Tubuyaki> getTubuyakiList() {
        ArrayList<Tubuyaki> list = new ArrayList<Tubuyaki>();

        NodeList nodeList = element.getElementsByTagName("tiratweet");
        for (Integer i = 0; i < nodeList.getLength(); ++i) {
            Element e = (Element) nodeList.item(i);

            Tubuyaki t = new Tubuyaki();
            t.setGooder(e.getElementsByTagName("gooder").item(0).getTextContent().trim());
            t.setNanashiid(e.getElementsByTagName("nanashiid").item(0).getTextContent().trim());
            t.setParent(Integer.parseInt(e.getElementsByTagName("parent").item(0).getTextContent().trim()));
            t.setTdata(e.getElementsByTagName("tdata").item(0).getTextContent().trim());
            t.setTdate(e.getElementsByTagName("tdate").item(0).getTextContent().trim());
            t.setTdate2(e.getElementsByTagName("tdate2").item(0).getTextContent().trim());
            t.setTgood(Integer.parseInt(e.getElementsByTagName("tgood").item(0).getTextContent().trim()));
            t.setThash(e.getElementsByTagName("thash").item(0).getTextContent().trim());
            t.setTno(Integer.parseInt(e.getElementsByTagName("tno").item(0).getTextContent().trim()));
            t.setTres(Integer.parseInt(e.getElementsByTagName("tres").item(0).getTextContent().trim()));
            t.setTupfile1(e.getElementsByTagName("tupfile1").item(0).getTextContent().trim());
            t.setTupfile2(e.getElementsByTagName("tupfile2").item(0).getTextContent().trim());
            t.setTupfile3(e.getElementsByTagName("tupfile3").item(0).getTextContent().trim());
            t.setTview(Integer.parseInt(e.getElementsByTagName("tview").item(0).getTextContent().trim()));
            t.setUid(Integer.parseInt(e.getElementsByTagName("uid").item(0).getTextContent().trim()));
            t.setUimg1(e.getElementsByTagName("uimg1").item(0).getTextContent().trim());
            t.setUimg2(e.getElementsByTagName("uimg2").item(0).getTextContent().trim());
            t.setUimg3(e.getElementsByTagName("uimg3").item(0).getTextContent().trim());
            t.setUname(e.getElementsByTagName("uname").item(0).getTextContent().trim());

            t.setTdata(StringEscapeUtils.unescapeXml(t.getTdata()));
            t.setThash(StringEscapeUtils.unescapeXml(t.getThash()));
            list.add(t);
        }

        return list;
    }

    public MyData getMyData() {
        MyData myData = new MyData();
        Element e = element;

        if (null != e.getElementsByTagName("mynum").item(0)) {

            if (!e.getElementsByTagName("mynum").item(0).getTextContent().trim().isEmpty()) {
                myData.setMynum(Integer.parseInt(e.getElementsByTagName("mynum").item(0).getTextContent().trim()));
            }
            myData.setMyname(e.getElementsByTagName("myname").item(0).getTextContent().trim());
            if (!e.getElementsByTagName("mytubu").item(0).getTextContent().trim().isEmpty()) {
                myData.setMytubu(Integer.parseInt(e.getElementsByTagName("mytubu").item(0).getTextContent().trim()));
            }
            if (!e.getElementsByTagName("mytira").item(0).getTextContent().trim().isEmpty()) {
                myData.setMytira(Integer.parseInt(e.getElementsByTagName("mytira").item(0).getTextContent().trim()));
            }
            myData.setMyimg1(StringUtils.defaultString(e.getElementsByTagName("myimg1").item(0).getTextContent().trim()));
            myData.setMyimg2(StringUtils.defaultString(e.getElementsByTagName("myimg2").item(0).getTextContent().trim()));
            myData.setMyimg3(StringUtils.defaultString(e.getElementsByTagName("myimg3").item(0).getTextContent().trim()));
            myData.setMybimg1(StringUtils.defaultString(e.getElementsByTagName("mybimg1").item(0).getTextContent().trim()));
            myData.setMybimg2(StringUtils.defaultString(e.getElementsByTagName("mybimg2").item(0).getTextContent().trim()));
            myData.setMybimg3(StringUtils.defaultString(e.getElementsByTagName("mybimg3").item(0).getTextContent().trim()));
            myData.setMyhash(StringUtils.defaultString(e.getElementsByTagName("myhash").item(0).getTextContent().trim()));
            if (!e.getElementsByTagName("mypoint").item(0).getTextContent().trim().isEmpty()) {
                myData.setMypoint(Integer.parseInt(e.getElementsByTagName("mypoint").item(0).getTextContent().trim()));
            }
            if (!e.getElementsByTagName("mymcount").item(0).getTextContent().trim().isEmpty()) {
                myData.setMymcount(Integer.parseInt(e.getElementsByTagName("mymcount").item(0).getTextContent().trim()));
            }
            if (!e.getElementsByTagName("mymcount2").item(0).getTextContent().trim().isEmpty()) {
                myData.setMymcount2(Integer.parseInt(e.getElementsByTagName("mymcount2").item(0).getTextContent().trim()));
            }
            myData.setMytubulog(MyTubuyakiLog.getMyTubuyakiLogList(StringUtils.defaultString(e.getElementsByTagName("mytubulog").item(0).getTextContent().trim())));
            myData.setMyreslog(MyResLog.getMyResList(StringUtils.defaultString(e.getElementsByTagName("myreslog").item(0).getTextContent().trim())));
        }

        return myData;
    }

    public String getString() {
        return element.getTextContent();
    }


    public String convertInputStreamToString(InputStream stream, int length) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[length];
        reader.read(buffer);
        return new String(buffer);
    }
}
