package com.wee0.box.plugins.storage.oss;

import com.wee0.box.plugins.storage.IMultipartUploadInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="78026399@qq.com">白华伟</a>
 * @CreateDate 2020/1/18 8:16
 * @Description 一个简单的分片上传信息对象实现
 * <pre>
 * 补充说明
 * </pre>
 **/
public class SimpleMultipartUploadInfo implements IMultipartUploadInfo {

    // 默认的分片数据大小
    static final int _DEF_PART_SIZE = 5242880;

    // 对象标识
    private String objectId;

    // 上传标识
    private String uploadId;

    // 对象总大小
    private long objectSize;

    // 每个分片的大小
    private int[] partSizes;

    public SimpleMultipartUploadInfo(String objectId, String uploadId, long objectSize) {
        this.objectId = objectId;
        this.uploadId = uploadId;
        this.objectSize = objectSize;

        if (objectSize < _DEF_PART_SIZE)
            throw new IllegalArgumentException("size " + objectSize + " is less than allowed size 5MB");

        List<Integer> _list = new ArrayList<>(32);
        long _size = objectSize;
        while (_size > _DEF_PART_SIZE) {
            _list.add(_DEF_PART_SIZE);
            _size -= _DEF_PART_SIZE;
        }
        _list.add((int) _size);
        this.partSizes = _list.stream().mapToInt(Integer::valueOf).toArray();
    }

    /**
     * @return 对象标识
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * @return 上传标识
     */
    public String getUploadId() {
        return uploadId;
    }

    /**
     * @return 对象大小
     */
    public long getObjectSize() {
        return objectSize;
    }

    /**
     * @return 所有分片大小
     */
    public int[] getPartSizes() {
        return partSizes;
    }

    @Override
    public String toString() {
        StringBuilder _builder = new StringBuilder(128);
        _builder.append("{'objectId':'").append(this.objectId);
        _builder.append("','uploadId':'").append(this.uploadId);
        _builder.append("','objectSize':").append(this.objectSize);
        _builder.append(",'partSizes':").append(Arrays.toString(this.partSizes));
        _builder.append("}");
        return _builder.toString();
    }

}
