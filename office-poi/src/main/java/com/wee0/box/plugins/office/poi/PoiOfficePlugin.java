package com.wee0.box.plugins.office.poi;

import com.wee0.box.plugins.office.IExcelUtils;
import com.wee0.box.plugins.office.IOfficePlugin;
import com.wee0.box.plugins.office.IWordUtils;

/**
 * @author <a href="78026399@qq.com">白华伟</a>
 * @CreateDate 2020/1/5 9:12
 * @Description 基于poi组件实现的office操作插件
 * <pre>
 * 补充说明
 * </pre>
 **/
public class PoiOfficePlugin implements IOfficePlugin {

    @Override
    public IWordUtils getWordUtils() {
        return PoiWordUtils.me();
    }

    @Override
    public IExcelUtils getExcelUtils() {
        throw new UnsupportedOperationException("This method is not yet implemented.");
    }

}
