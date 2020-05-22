package com.wee0.box.plugins.storage.minio;

import com.wee0.box.log.ILogger;
import com.wee0.box.log.LoggerFactory;
import com.wee0.box.util.shortcut.CheckUtils;
import io.minio.CopyConditions;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.ServerSideEncryption;
import io.minio.errors.*;
import io.minio.messages.Item;
import io.minio.messages.ListPartsResult;
import io.minio.messages.Part;
import okhttp3.*;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="78026399@qq.com">白华伟</a>
 * @CreateDate 2020/1/18 8:12
 * @Description minio操作助手类
 * <pre>
 * 补充说明
 * </pre>
 **/
final class MinioHelper {

    // 日志对象
    private static ILogger log = LoggerFactory.getLogger(MinioHelper.class);

    // 当前版本需要用到的几个官方客户端未暴露出的私有方法。
    private static Method _initMultipartUpload, _calculateMultipartSize, _listObjectParts, _completeMultipart;

    static {
        Class _clientCla = MinioClient.class;
        try {
            _initMultipartUpload = _clientCla.getDeclaredMethod("initMultipartUpload", new Class[]{String.class, String.class, Map.class});
            _initMultipartUpload.setAccessible(true);
            _calculateMultipartSize = _clientCla.getDeclaredMethod("calculateMultipartSize", new Class[]{long.class});
            _calculateMultipartSize.setAccessible(true);
            _listObjectParts = _clientCla.getDeclaredMethod("listObjectParts", new Class[]{String.class, String.class, String.class, int.class});
            _listObjectParts.setAccessible(true);
            _completeMultipart = _clientCla.getDeclaredMethod("completeMultipart", new Class[]{String.class, String.class, String.class, Part[].class});
            _completeMultipart.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    // 用户未指定时默认的存储桶名称
    static final String DEF_BUCKET_NAME = "data";

    // minio客户端
    private MinioClient client;
    // 默认操作的存储桶名称
    private String defBucketName;


    // 默认的上传对象类型
    static final MediaType DEF_UPLOAD_MEDIA_TYPE = MediaType.parse("application/octet-stream");
    // http请求客户端
    private OkHttpClient _httpClient;

    // 客户端初始化逻辑
    synchronized void init(String endpoint, String accessKey, String secretKey, String defBucketName) {
        if (null != client)
            throw new IllegalStateException("already initialized.");

        String _endpoint = CheckUtils.checkNotTrimEmpty(endpoint, "endpoint can not be empty!");
        String _accessKey = CheckUtils.checkNotTrimEmpty(accessKey, "accessKey can not be empty!");
        String _secretKey = CheckUtils.checkNotTrimEmpty(secretKey, "secretKey can not be empty!");
        this.defBucketName = CheckUtils.checkTrimEmpty(defBucketName, DEF_BUCKET_NAME);
        try {
            client = new MinioClient(_endpoint, _accessKey, _secretKey);
        } catch (InvalidEndpointException | InvalidPortException e) {
            throw new RuntimeException(e);
        }
        log.debug("init by endpoint: {}, default bucket name: {}.", _endpoint, this.defBucketName);
    }

    /**
     * 获取签名授权上传地址
     *
     * @param id         对象标识
     * @param expireTime 授权过期时间。单位：秒。
     * @param params     附加参数
     * @return 授权上传地址
     */
    public String getSignedUploadUrl(String id, int expireTime, Map<String, String> params) {
        try {
            return this.client.getPresignedObjectUrl(io.minio.http.Method.PUT, this.defBucketName, id, expireTime, params);
        } catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | XmlPullParserException | ErrorResponseException | InternalException | InvalidExpiresRangeException | InvalidResponseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取签名授权下载地址
     *
     * @param id         对象标识
     * @param expireTime 授权过期时间。单位：秒。
     * @param params     附加参数
     * @return 授权下载地址
     */
    public String getSignedDownloadUrl(String id, int expireTime, Map<String, String> params) {
        try {
            return this.client.getPresignedObjectUrl(io.minio.http.Method.GET, this.defBucketName, id, expireTime, params);
//            return this.client.presignedGetObject(this.defBucketName, id, expireTime);
        } catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | XmlPullParserException | ErrorResponseException | InternalException | InvalidExpiresRangeException | InvalidResponseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取资源请求地址
     *
     * @param id 资源唯一标识
     * @return 资源请求地址
     */
    public String getObjectUrl(String id) {
        try {
            return this.client.getObjectUrl(this.defBucketName, id);
        } catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | XmlPullParserException | ErrorResponseException | InternalException | InvalidResponseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断指定资源是否存在
     *
     * @param id 资源唯一标识
     * @return 存在返回True
     */
    public boolean exists(String id) {
        try {
            this.client.statObject(this.defBucketName, id);
            return true;
        } catch (ErrorResponseException | InvalidResponseException | XmlPullParserException | InvalidBucketNameException | InvalidKeyException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | InternalException | InvalidArgumentException | IOException e) {
            log.debug("exists error.", e);
            return false;
        }
    }

    /**
     * 删除指定标识资源
     *
     * @param id 资源唯一标识
     * @return 成功返回True
     */
    public boolean remove(String id) {
        try {
            this.client.removeObject(this.defBucketName, id);
            return true;
        } catch (ErrorResponseException | InvalidResponseException | XmlPullParserException | InvalidBucketNameException | InvalidKeyException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | InternalException | InvalidArgumentException | IOException e) {
            log.debug("remove error.", e);
            return false;
        }
    }

    /**
     * 拷贝指定标识资源
     *
     * @param from 资源唯一标识
     * @param to   新的资源唯一标识
     * @return 成功返回True
     */
    public boolean copy(String from, String to) {
        try {
            this.client.copyObject(this.defBucketName, to, (Map) null, (ServerSideEncryption) null, this.defBucketName, from, (ServerSideEncryption) null, (CopyConditions) null);
            return true;
        } catch (ErrorResponseException | InvalidResponseException | XmlPullParserException | InvalidBucketNameException | InvalidKeyException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | InternalException | InvalidArgumentException | IOException e) {
            log.debug("copy error.", e);
            return false;
        }
    }

    /************************************************************
     ************* 分片上传支持。
     ************************************************************/
    public SimpleMultipartUploadInfo multipartUploadInfoGet(String id, long size) {
        try {
            String _uploadId = (String) _initMultipartUpload.invoke(this.client, this.defBucketName, id, new HashMap<String, String>());
            SimpleMultipartUploadInfo _multipartUploadInfo = new SimpleMultipartUploadInfo(id, _uploadId, size);
            return _multipartUploadInfo;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public String multipartUploadUrlGet(String objectId, String uploadId, int partIndex, int partSize, int expires) {
        Map<String, String> _params = new HashMap<>();
        //分片编号
        _params.put("partNumber", String.valueOf(partIndex));
        //分片的大小
        _params.put("Content-Length", String.valueOf(partSize));
        //文件名
        _params.put("key", objectId);
        //uploadId
        _params.put("uploadId", uploadId);
        //过期时间
        if (1 > expires)
            expires = 60 * 60 * 24;
        return getSignedUploadUrl(objectId, expires, _params);
//        return this.client.getPresignedObjectUrl(io.minio.http.Method.PUT, this.defBucketName, objectId, expires, _param);;
    }

    public boolean multipartUploadComplete(String objectId, String uploadId) {
        try {
            ListPartsResult _partsResult = (ListPartsResult) _listObjectParts.invoke(this.client, this.defBucketName, objectId, uploadId, 0);
            Part[] _uploadParts = _partsResult.partList().toArray(new Part[]{});
            _completeMultipart.invoke(this.client, this.defBucketName, objectId, uploadId, _uploadParts);
            return true;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean multipartUploadDo(String uploadUrl, File file, int offset, int length) {
        try (RandomAccessFile _raFile = new RandomAccessFile(file, "r");) {
            _raFile.seek(offset);
            byte[] _content = new byte[length];
            _raFile.readFully(_content);

            RequestBody _body = RequestBody.create(DEF_UPLOAD_MEDIA_TYPE, _content);
            Request _request = new Request.Builder().url(uploadUrl)
                    .header("Content-Type", "application/octet-stream")
                    .header("Connection", "keep-alive")
                    .header("Content-Length", String.valueOf(length))
                    .put(_body).build();
//            System.out.println("_request:" + _request);
            _httpClient.newCall(_request).execute();
//            Response _response = _httpClient.newCall(_request).execute();
//            System.out.println("response:" + _response);
//            String _eTag = _response.header("etag");
//            System.out.println("response.etag:" + _eTag);
            return true;
        } catch (IOException e) {
            log.warn("multipartUploadDo error!", e);
            return false;
        }
    }

    /************************************************************
     ************* 单例样板代码。
     ************************************************************/
    private MinioHelper() {
        if (null != MinioHelperHolder._INSTANCE) {
            // 防止使用反射API创建对象实例。
            throw new IllegalStateException("that's not allowed!");
        }

        List<Protocol> _protocol = new LinkedList();
        _protocol.add(Protocol.HTTP_1_1);
        _httpClient = new OkHttpClient().newBuilder()
                .connectTimeout(900L, TimeUnit.SECONDS)
                .writeTimeout(900L, TimeUnit.SECONDS)
                .readTimeout(900L, TimeUnit.SECONDS)
                .protocols(_protocol).build();

    }

    // 当前对象唯一实例持有者。
    private static final class MinioHelperHolder {
        private static final MinioHelper _INSTANCE = new MinioHelper();
    }

    // 防止使用反序列化操作获取多个对象实例。
    private Object readResolve() throws ObjectStreamException {
        return MinioHelperHolder._INSTANCE;
    }

    /**
     * 获取当前对象唯一实例。
     *
     * @return 当前对象唯一实例
     */
    public static MinioHelper me() {
        return MinioHelperHolder._INSTANCE;
    }

    // ---------------- 测试方法

    void test_ls(String path) {
        if ("/".equals(path)) {
            // minio不能识别'/'，列出根目录需要传null。
            path = null;
        }
        Iterable<Result<Item>> _objects = this.client.listObjects(this.defBucketName, path, false);
        _objects.forEach((_res) -> {
            try {
                Item _item = _res.get();
                String _fid = (String) _item.get("Key");
                if ('/' != _fid.charAt(0)) {
                    // 路径统一以'/'开头。
                    _fid = "/".concat(_fid);
                }
                String _lastModified = (String) _item.getOrDefault("LastModified", null);
                long _fTime = 0;
                if (null != _lastModified) {
                    _fTime = Instant.parse(_lastModified).toEpochMilli();
                }
                long _fSize = (long) _item.getOrDefault("Size", 0);
                log.debug("fid: {}", _fid);
                log.debug("fSize: {}", _fSize);
                log.debug("fTime: {}", _fTime);
                log.debug("isDir: {}", 0 == _fTime);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    String test_read(String fileId, String charsetName) {
        if (null == charsetName)
            charsetName = "UTF-8";
        try (InputStream inStream = this.client.getObject(this.defBucketName, fileId);) {
            byte[] buf = new byte[16384];
            int bytesRead;
            StringBuilder sb = new StringBuilder();
            while (0 <= (bytesRead = inStream.read(buf, 0, buf.length))) {
                sb.append(new String(buf, 0, bytesRead, charsetName));
            }
            return sb.toString();
        } catch (ErrorResponseException e) {
            throw new RuntimeException("response error:" + e.getMessage(), e);
        } catch (XmlPullParserException | InvalidBucketNameException | InvalidKeyException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | InvalidResponseException | InternalException | InvalidArgumentException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
