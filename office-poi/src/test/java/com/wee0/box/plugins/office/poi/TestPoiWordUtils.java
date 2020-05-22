package com.wee0.box.plugins.office.poi;

import com.wee0.box.plugins.office.IWordUtils;
import com.wee0.box.util.IHttpUtils;
import com.wee0.box.util.shortcut.HttpUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author <a href="78026399@qq.com">白华伟</a>
 * @CreateDate 2020/1/5 9:22
 * @Description 功能描述
 * <pre>
 * 补充说明
 * </pre>
 **/
public class TestPoiWordUtils {

    public static void main(String[] args) {
        _t2();
    }

    static void _t2() {
    }

    static void _t1() {
        Map<String, Object> _data = new HashMap<>(16);
        _data.put("title1", "xxx什么的标题一");
        _data.put("title2", "标题二");
        _data.put("p1", "小标题一");
        _data.put("p2", "小标题二");
        _data.put("p3", "随便来些什么吧");
        _data.put("p4", "某某四");
        _data.put("p5", "不知道打些什么了");
        _data.put("date", "2020/1/9 13:32:32");
        _data.put("phone", "13666000000");
        _data.put("1", "张三");
        _data.put("2", "男");
        _data.put("3", 18);
        _data.put("120", 120);
        _data.put("121", 121);

        Pattern _pattern = Pattern.compile(IWordUtils.PLACEHOLDER_PATTERN1);
//        Pattern _pattern = Pattern.compile(IWordUtils.DEF_PLACEHOLDER_PATTERN);
        File _testDir = new File(Thread.currentThread().getContextClassLoader().getResource("").getFile());
        try (FileInputStream _in = new FileInputStream(new File(_testDir, "word1Template.docx"));
             FileOutputStream _out = new FileOutputStream(new File(_testDir, "word1.docx"));) {
            PoiWordUtils.me().templateProcess(_in, _out, _pattern, _data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileInputStream _in = new FileInputStream(new File(_testDir, "word1Template.docx"));
             FileOutputStream _out = new FileOutputStream(new File(_testDir, "word2.docx"));) {
            PoiWordUtils.me().templateProcess(_in, _out, _data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
