package com.wee0.box.plugins.storage.oss;

import com.wee0.box.log.ILogger;
import com.wee0.box.log.LoggerFactory;
import com.wee0.box.plugins.storage.ICloudStoragePlugin;
import com.wee0.box.plugins.storage.IMultipartUploadInfo;

import java.io.File;
import java.util.Map;

/**
 * @author <a href="78026399@qq.com">白华伟</a>
 * @CreateDate 2020/5/17 19:51
 * @Description 基于OSS的存储插件实现
 * <pre>
 * 补充说明
 * </pre>
 **/
public class OssStorage implements ICloudStoragePlugin {

    // 日志对象
    private static ILogger log = LoggerFactory.getLogger(OssStorage.class);

    @Override
    public Map<String, String> getUploadForm(String id, int expireTime, String contentType, long fileMaxSize) {
        return OssHelper.me().getSignedUploadForm(id, expireTime, contentType, fileMaxSize);
    }

    @Override
    public String getUploadUrl(String id, int expireTime, String contentType) {
        return OssHelper.me().getSignedUploadUrl(id, expireTime, contentType);
    }

    @Override
    public String getDownloadUrl(String id, int expireTime) {
        return OssHelper.me().getSignedDownloadUrl(id, expireTime, null);
    }

    @Override
    public String getPublicUrl(String id) {
        return OssHelper.me().getObjectUrl(id);
    }

    @Override
    public boolean exists(String id) {
        return OssHelper.me().exists(id);
    }

    @Override
    public boolean remove(String id) {
        return OssHelper.me().remove(id);
    }

    @Override
    public boolean copy(String from, String to) {
        return OssHelper.me().copy(from, to);
    }

    /****************************************************************
     * 分片操作支持
     ****************************************************************/

    @Override
    public IMultipartUploadInfo multipartUploadInfoGet(String id, long size) {
        throw new UnsupportedOperationException("This method is not yet implemented.");
    }

    @Override
    public String multipartUploadUrlGet(String objectId, String uploadId, int partIndex, int partSize, int expires) {
        throw new UnsupportedOperationException("This method is not yet implemented.");
    }

    @Override
    public boolean multipartUploadDo(String uploadUrl, File file, int offset, int length) {
        throw new UnsupportedOperationException("This method is not yet implemented.");
    }

    @Override
    public boolean multipartUploadComplete(String objectId, String uploadId) {
        throw new UnsupportedOperationException("This method is not yet implemented.");
    }

    @Override
    public void init(Map<String, String> params) {
        log.debug("init... params: {}", params);
        String _endpoint = params.get("endpoint");
        String _accessKey = params.get("accessKey");
        String _secretKey = params.get("secretKey");
        String _defBucketName = params.get("bucket");
        String _domain = params.get("domain");
        OssHelper.me().init(_endpoint, _accessKey, _secretKey, _defBucketName, _domain);
    }

    @Override
    public void destroy() {
        log.debug("destroy...");
    }

}
