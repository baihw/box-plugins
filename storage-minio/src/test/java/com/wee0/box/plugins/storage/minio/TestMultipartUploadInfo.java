package com.wee0.box.plugins.storage.minio;

import java.io.File;

/**
 * @author <a href="78026399@qq.com">白华伟</a>
 * @CreateDate 2020/1/18 8:18
 * @Description 功能描述
 * <pre>
 * 补充说明
 * </pre>
 **/
public class TestMultipartUploadInfo {

    public static void main(String[] args) {

        String _objectId = "o1";
        String _uploadId = "u1";
        long _size = new File("E:\\Downloads\\sysdiag-all-5.0.25.5.exe").length();
        SimpleMultipartUploadInfo _multipartUploadInfo = new SimpleMultipartUploadInfo(_objectId, _uploadId, _size);
        System.out.println("_multipartUploadInfo:" + _multipartUploadInfo);

    }

}
