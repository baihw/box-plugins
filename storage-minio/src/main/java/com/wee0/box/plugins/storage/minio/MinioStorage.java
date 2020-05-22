package com.wee0.box.plugins.storage.minio;

import com.wee0.box.log.ILogger;
import com.wee0.box.log.LoggerFactory;
import com.wee0.box.plugins.storage.ICloudStoragePlugin;
import com.wee0.box.plugins.storage.IMultipartUploadInfo;

import java.io.File;
import java.util.Map;

/**
 * @author <a href="78026399@qq.com">白华伟</a>
 * @CreateDate 2020/1/1 7:12
 * @Description 基于minio的存储插件实现
 * <pre>
 * 补充说明
 * </pre>
 **/
public class MinioStorage implements ICloudStoragePlugin {

    // 日志对象
    private static ILogger log = LoggerFactory.getLogger(MinioStorage.class);

    @Override
    public Map<String, String> getUploadForm(String id, int expireTime, String contentType, long fileMaxSize) {
        throw new UnsupportedOperationException("This method is not yet implemented.");
    }

    @Override
    public String getUploadUrl(String id, int expireTime, String contentType) {
        return MinioHelper.me().getSignedUploadUrl(id, expireTime, null);
    }

    @Override
    public String getDownloadUrl(String id, int expireTime) {
        return MinioHelper.me().getSignedDownloadUrl(id, expireTime, null);
    }

    @Override
    public String getPublicUrl(String id) {
        return MinioHelper.me().getObjectUrl(id);
    }

    @Override
    public boolean exists(String id) {
        return MinioHelper.me().exists(id);
    }

    @Override
    public boolean remove(String id) {
        return MinioHelper.me().remove(id);
    }

    @Override
    public boolean copy(String from, String to) {
        return MinioHelper.me().copy(from, to);
    }

    /****************************************************************
     * 分片操作支持
     ****************************************************************/

    @Override
    public IMultipartUploadInfo multipartUploadInfoGet(String id, long size) {
        return MinioHelper.me().multipartUploadInfoGet(id, size);
    }

    @Override
    public String multipartUploadUrlGet(String objectId, String uploadId, int partIndex, int partSize, int expires) {
        return MinioHelper.me().multipartUploadUrlGet(objectId, uploadId, partIndex, partSize, expires);
    }

    @Override
    public boolean multipartUploadDo(String uploadUrl, File file, int offset, int length) {
        return MinioHelper.me().multipartUploadDo(uploadUrl, file, offset, length);
    }

    @Override
    public boolean multipartUploadComplete(String objectId, String uploadId) {
        return MinioHelper.me().multipartUploadComplete(objectId, uploadId);
    }

    @Override
    public void init(Map<String, String> params) {
        log.debug("init... params: {}", params);
        String _endpoint = params.get("endpoint");
        String _accessKey = params.get("accessKey");
        String _secretKey = params.get("secretKey");
        String _defBucketName = params.get("bucket");
        MinioHelper.me().init(_endpoint, _accessKey, _secretKey, _defBucketName);
    }

    @Override
    public void destroy() {
        log.debug("destroy...");
    }

}
