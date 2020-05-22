package com.wee0.box.plugins.storage.oss;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.wee0.box.log.ILogger;
import com.wee0.box.log.LoggerFactory;
import com.wee0.box.plugins.storage.ICloudStoragePlugin;
import com.wee0.box.util.shortcut.CheckUtils;
import com.wee0.box.util.shortcut.StringUtils;

import java.io.File;
import java.io.ObjectStreamException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="78026399@qq.com">白华伟</a>
 * @CreateDate 2020/5/17 20:32
 * @Description OSS操作助手类
 * <pre>
 * 补充说明
 * </pre>
 **/
final class OssHelper {

    // 日志对象
    private static ILogger log = LoggerFactory.getLogger(OssHelper.class);

    /**
     * 默认编码
     */
    static final String DEF_ENCODING = "UTF-8";

    // 用户未指定时默认的存储桶名称
    static final String DEF_BUCKET_NAME = "upload";

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String domain;
    // 默认操作的存储桶名称
    private String defBucketName;


//    // 默认的上传对象类型
//    static final MediaType DEF_UPLOAD_MEDIA_TYPE = MediaType.parse("application/octet-stream");
//    // http请求客户端
//    private OkHttpClient _httpClient;

    // 客户端初始化逻辑
    synchronized void init(String endpoint, String accessKey, String secretKey, String defBucketName, String domain) {
        if (null != this.endpoint)
            throw new IllegalStateException("already initialized.");

        this.endpoint = CheckUtils.checkNotTrimEmpty(endpoint, "endpoint can not be empty!");
        this.accessKey = CheckUtils.checkNotTrimEmpty(accessKey, "accessKey can not be empty!");
        this.secretKey = CheckUtils.checkNotTrimEmpty(secretKey, "secretKey can not be empty!");
        this.domain = CheckUtils.checkTrimEmpty(domain, null);
        if (null != this.domain) this.domain = StringUtils.endsWithChar(this.domain, '/');
        this.defBucketName = CheckUtils.checkTrimEmpty(defBucketName, DEF_BUCKET_NAME);
        log.debug("init by endpoint: {}, default bucket name: {}.", this.endpoint, this.defBucketName);
    }

    // 获取oss客户端对象
    OSS createOSSClient() {
//        if(null == this.ossClient) this.ossClient = new OSSClientBuilder().build(this.endpoint, this.accessKey, this.secretKey);
        return new OSSClientBuilder().build(this.endpoint, this.accessKey, this.secretKey);
    }

    /**
     * 获取签名授权上传地址
     *
     * @param id          对象标识
     * @param expireTime  授权过期时间。单位：秒。
     * @param contentType 内容类型
     * @param fileMaxSize 允许上传的文件大小
     * @return 授权上传地址
     */
    public Map<String, String> getSignedUploadForm(String id, int expireTime, String contentType, long fileMaxSize) {
        id = CheckUtils.checkNotTrimEmpty(id, "id cannot be empty!");
//        contentType = CheckUtils.checkTrimEmpty(contentType, ICloudStoragePlugin.DEF_CONTENT_TYPE);
        if (1 > expireTime) expireTime = ICloudStoragePlugin.DEF_EXPIRE_TIME;
        Date _expireDate = Date.from(Instant.now().plusSeconds(expireTime));
        if (1 > fileMaxSize) fileMaxSize = ICloudStoragePlugin.DEF_FILE_MAX_SIZE;

        OSS _client = createOSSClient();
        try {
            PolicyConditions _policyConditions = new PolicyConditions();
            // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。
            _policyConditions.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, fileMaxSize);
            _policyConditions.addConditionItem(MatchMode.Exact, PolicyConditions.COND_KEY, id);

            String _postPolicy = _client.generatePostPolicy(_expireDate, _policyConditions);
            byte[] _binaryData = _postPolicy.getBytes(DEF_ENCODING);
            String _encodedPolicy = BinaryUtil.toBase64String(_binaryData);
            String _postSignature = _client.calculatePostSignature(_postPolicy);

            Map<String, String> _result = new HashMap<>(8, 1.0f);
            _result.put("url", "http://" + this.defBucketName + '.' + this.endpoint);
            _result.put("fid", id);
            _result.put("ak", this.accessKey);
            _result.put("policy", _encodedPolicy);
            _result.put("signature", _postSignature);
            _result.put("expires", String.valueOf(expireTime));
            return _result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            _client.shutdown();
        }
    }

    /**
     * 获取签名授权上传地址
     *
     * @param id          对象标识
     * @param expireTime  授权过期时间。单位：秒。
     * @param contentType 内容类型
     * @return 授权上传地址
     */
    public String getSignedUploadUrl(String id, int expireTime, String contentType) {
        id = CheckUtils.checkNotTrimEmpty(id, "id cannot be empty!");
        contentType = CheckUtils.checkNotTrimEmpty(contentType, ICloudStoragePlugin.DEF_CONTENT_TYPE);
        if (1 > expireTime) expireTime = ICloudStoragePlugin.DEF_EXPIRE_TIME;
        Date _expireDate = Date.from(Instant.now().plusSeconds(expireTime));

        GeneratePresignedUrlRequest _signedUrlRequest = new GeneratePresignedUrlRequest(this.defBucketName, id);
        _signedUrlRequest.setContentType(contentType);
        _signedUrlRequest.setMethod(HttpMethod.PUT);
        _signedUrlRequest.setExpiration(_expireDate);

        OSS _client = createOSSClient();
        URL _url = _client.generatePresignedUrl(_signedUrlRequest);
        return _url.toString();
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
        id = CheckUtils.checkNotTrimEmpty(id, "id cannot be empty!");
        if (1 > expireTime) expireTime = ICloudStoragePlugin.DEF_EXPIRE_TIME;
        Date _expireDate = Date.from(Instant.now().plusSeconds(expireTime));
        OSS _client = createOSSClient();
        try {
            URL _url = _client.generatePresignedUrl(this.defBucketName, id, _expireDate);
            return _url.toString();
        } finally {
            _client.shutdown();
        }
    }

    /**
     * 获取资源公开请求地址
     *
     * @param id 资源唯一标识
     * @return 资源请求地址
     */
    public String getObjectUrl(String id) {
        id = CheckUtils.checkNotTrimEmpty(id, "id cannot be empty!");
        id = StringUtils.startsWithoutChar(id, '/');
        if (null != this.domain) return this.domain + id;
        StringBuilder _builder = new StringBuilder();
        _builder.append("https://").append(this.defBucketName).append('.');
        _builder.append(id);
        return _builder.toString();
    }

    /**
     * 判断指定资源是否存在
     *
     * @param id 资源唯一标识
     * @return 存在返回True
     */
    public boolean exists(String id) {
        id = CheckUtils.checkNotTrimEmpty(id, "id cannot be empty!");
        OSS _client = createOSSClient();
        try {
            return _client.doesObjectExist(this.defBucketName, id);
        } finally {
            _client.shutdown();
        }
    }

    /**
     * 删除指定标识资源
     *
     * @param id 资源唯一标识
     * @return 成功返回True
     */
    public boolean remove(String id) {
        id = CheckUtils.checkNotTrimEmpty(id, "id cannot be empty!");
        OSS _client = createOSSClient();
        try {
            _client.deleteObject(this.defBucketName, id);
            return true;
        } finally {
            _client.shutdown();
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
        from = CheckUtils.checkNotTrimEmpty(from, "from cannot be empty!");
        to = CheckUtils.checkNotTrimEmpty(to, "to cannot be empty!");
        OSS _client = createOSSClient();
        try {
            _client.copyObject(this.defBucketName, from, this.defBucketName, to);
            return true;
        } finally {
            _client.shutdown();
        }
    }

    /************************************************************
     ************* 分片上传支持。
     ************************************************************/
    public SimpleMultipartUploadInfo multipartUploadInfoGet(String id, long size) {
        throw new UnsupportedOperationException("This method is not yet implemented.");
    }

    public String multipartUploadUrlGet(String objectId, String uploadId, int partIndex, int partSize, int expires) {
        throw new UnsupportedOperationException("This method is not yet implemented.");
    }

    public boolean multipartUploadComplete(String objectId, String uploadId) {
        throw new UnsupportedOperationException("This method is not yet implemented.");
    }

    public boolean multipartUploadDo(String uploadUrl, File file, int offset, int length) {
        throw new UnsupportedOperationException("This method is not yet implemented.");
    }

    /************************************************************
     ************* 单例样板代码。
     ************************************************************/
    private OssHelper() {
        if (null != OssHelperHolder._INSTANCE) {
            // 防止使用反射API创建对象实例。
            throw new IllegalStateException("that's not allowed!");
        }
    }

    // 当前对象唯一实例持有者。
    private static final class OssHelperHolder {
        private static final OssHelper _INSTANCE = new OssHelper();
    }

    // 防止使用反序列化操作获取多个对象实例。
    private Object readResolve() throws ObjectStreamException {
        return OssHelperHolder._INSTANCE;
    }

    /**
     * 获取当前对象唯一实例。
     *
     * @return 当前对象唯一实例
     */
    public static OssHelper me() {
        return OssHelperHolder._INSTANCE;
    }

}
