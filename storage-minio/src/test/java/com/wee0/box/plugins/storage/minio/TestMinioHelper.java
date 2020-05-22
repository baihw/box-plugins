package com.wee0.box.plugins.storage.minio;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

/**
 * @author <a href="78026399@qq.com">白华伟</a>
 * @CreateDate 2020/1/18 8:19
 * @Description 功能描述
 * <pre>
 * 补充说明
 * </pre>
 **/
public class TestMinioHelper {

    // 测试文件
    static final File _F = new File("E:\\Downloads\\AxureRP.zip");

    @BeforeClass
    public static void init() {
        String _endPoint = "http://192.168.1.217:10090";
        String _accessKey = "VQ7D5KI82K8C6O1PA1CN";
        String _secretKey = "4FBczAGosjQukVdughZhQVOCS+9iVY8vdqqNCneS";
        String _bucketName = "test1";

        MinioHelper.me().init(_endPoint, _accessKey, _secretKey, _bucketName);
    }

    @Test
    public void testMultipartUpload() {
        SimpleMultipartUploadInfo _info = MinioHelper.me().multipartUploadInfoGet(_F.getName(), _F.length());
        System.out.println("info:" + _info);
        String _uploadId = _info.getUploadId();
        int[] _parts = _info.getPartSizes();
        int _uploadLength = 0;
        for (int _i = 1, _iLen = _parts.length; _i <= _iLen; _i++) {
            int _contentLength = _parts[_i - 1];
            String _uploadUrl = MinioHelper.me().multipartUploadUrlGet(_F.getName(), _uploadId, _i, _contentLength, 0);
            MinioHelper.me().multipartUploadDo(_uploadUrl, _F, _uploadLength, _contentLength);
            System.out.println("upload..., i: " + _i + ", content: " + _contentLength + ", uploaded: " + _uploadLength);
            _uploadLength += _contentLength;
        }
//        boolean _completed = MinioHelper.me().multipartUploadComplete(_F.getName(), _uploadId);
//        System.out.println("_completed: " + _completed);
    }

//    @Test
//    public void multipartUploadInfoGet() {
//        MultipartUploadInfo _info = MinioHelper.me().multipartUploadInfoGet(_F.getName(), _F.length());
//        System.out.println("uploadInfo:" + _info);
//    }

//    @Test
//    public void testUpload() {
//        String _uploadId = "2a56708f-eec4-44c3-89da-d13a3ed4950b";
//        int[] _parts = new int[]{5242880, 5242880, 5242880, 5242880, 5242880, 5242880, 5242880, 5242880, 5242880, 5242880, 5242880, 5242880, 5242880, 5242880, 5242880, 5242880, 3070120};
//        int _uploadLength = 0;
//        for (int _i = 1, _iLen = _parts.length; _i <= _iLen; _i++) {
//            int _contentLength = _parts[_i - 1];
//            String _uploadUrl = MinioHelper.me().multipartUploadUrlGet(_F.getName(), _uploadId, _i, _contentLength, 0);
//            MinioHelper.me().multipartUploadDo(_uploadUrl, _F, _uploadLength, _contentLength);
//            System.out.println("upload..., i: " + _i + ", content: " + _contentLength + ", uploaded: " + _uploadLength);
//            _uploadLength += _contentLength;
//        }
//        boolean _completed = MinioHelper.me().multipartUploadComplete(_F.getName(), _uploadId);
//        System.out.println("_completed: " + _completed);
//    }

//    @Test
//    public void testUploadComplete() {
//        String _uploadId = "61335a38-c1d1-4ee3-b9d5-7074f71c7e11";
//
//        // 上传最后一个分片
//        int _contentLength = (int) (_F.length() - 83886080); // 3070120
//        String _uploadUrl = MinioHelper.me().multipartUploadUrlGet(_F.getName(), _uploadId, 17, _contentLength, 0);
//        MinioHelper.me().multipartUploadDo(_uploadUrl, _F, 83886080, _contentLength);
//
//        boolean _completed = MinioHelper.me().multipartUploadComplete(_F.getName(), _uploadId);
//        System.out.println("_completed: " + _completed);
//    }

}
