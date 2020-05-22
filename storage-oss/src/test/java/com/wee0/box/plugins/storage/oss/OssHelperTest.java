package com.wee0.box.plugins.storage.oss;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

/**
 * @author <a href="78026399@qq.com">白华伟</a>
 * @CreateDate 2020/5/17 21:02
 * @Description 功能描述
 * <pre>
 * 补充说明
 * </pre>
 **/
public class OssHelperTest {

    @BeforeClass
    public static void beforeClass() {
        String _endPoint = "oss-cn-shanghai.aliyuncs.com";
        String _accessKey = "xx";
        String _secretKey = "xx";
        String _bucketName = "test1";
        String _domain = "http://static.xxx.com/";

        OssHelper.me().init(_endPoint, _accessKey, _secretKey, _bucketName, _domain);
    }

    @Test
    public void test1() {
        System.out.println("exists: " + OssHelper.me().exists("test/start.jpg"));
        System.out.println("url: " + OssHelper.me().getObjectUrl("test/start.jpg"));

        Map<String, String> _params = OssHelper.me().getSignedUploadForm("test/start.jpg", 300, "", 0);
        System.out.println("params:" + _params);
    }

}
