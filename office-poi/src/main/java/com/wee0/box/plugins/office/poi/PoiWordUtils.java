package com.wee0.box.plugins.office.poi;

import com.wee0.box.plugins.office.IWordUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="78026399@qq.com">白华伟</a>
 * @CreateDate 2020/1/5 9:12
 * @Description 基于poi组件实现的word操作工具类
 * <pre>
 * 补充说明
 * </pre>
 **/
public class PoiWordUtils implements IWordUtils {

    // 默认的占位符匹配模式
    private static final Pattern DEF_PATTERN = Pattern.compile(DEF_PLACEHOLDER_PATTERN);

    @Override
    public void templateProcess(InputStream in, OutputStream out, Pattern placeholderPattern, Function<String, Object> placeholderHandler) throws IOException {
        if (null == in)
            throw new IllegalArgumentException("InputStream can not be null!");
        if (null == out)
            throw new IllegalArgumentException("OutputStream can not be null!");
        if (null == placeholderPattern)
            placeholderPattern = DEF_PATTERN;

        XWPFDocument _document = new XWPFDocument(in);
        if (null == placeholderHandler) {
            // 如果没有任何占位符处理逻辑，则直接输出原模板文档内容。
            _document.write(out);
            out.flush();
            return;
        }

        // 处理段落
        processParagraphs(_document.getParagraphs(), placeholderPattern, placeholderHandler);
        // 处理表格
        processTables(_document.getTables(), placeholderPattern, placeholderHandler);

        String _imgFile = "D:\\C\\Desktop\\start.jpg";
        XWPFParagraph _paragraph = _document.createParagraph();
        XWPFRun _run = _paragraph.createRun();
        _run.setText(_imgFile);
        _run.addBreak();
        try (FileInputStream _in = new FileInputStream(_imgFile)) {
            // 200x200 pixels
            _run.addPicture(_in, XWPFDocument.PICTURE_TYPE_JPEG, _imgFile, Units.toEMU(200), Units.toEMU(200));
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        _run.addBreak(BreakType.PAGE);

        // 处理图片
        processPictures(_document.getAllPictures());


        _document.write(out);
        out.flush();
    }

    @Override
    public void templateProcess(InputStream in, OutputStream out, Pattern placeholderPattern, int placeholderLeftLen, int placeholderRightLen, Map<String, Object> placeholderData) throws IOException {
        Function _placeholderHandler = null;
        if (null != placeholderData && !placeholderData.isEmpty()) {
            _placeholderHandler = new DEF_PLACEHOLDER_HANDLER(placeholderData, placeholderLeftLen, placeholderRightLen);
        }

        templateProcess(in, out, placeholderPattern, _placeholderHandler);
    }

    @Override
    public void templateProcess(InputStream in, OutputStream out, Pattern placeholderPattern, Map<String, Object> placeholderData) throws IOException {
        templateProcess(in, out, placeholderPattern, DEF_PLACEHOLDER_LEFT_LEN, DEF_PLACEHOLDER_RIGHT_LEN, placeholderData);
    }

    @Override
    public void templateProcess(InputStream in, OutputStream out, Map<String, Object> placeholderData) throws IOException {
        templateProcess(in, out, DEF_PATTERN, placeholderData);
    }

    /**
     * 默认的占位符处理逻辑
     */
    private static final class DEF_PLACEHOLDER_HANDLER implements Function<String, Object> {

        private final Map<String, Object> DATA;
        private final int LEFT_LEN;
        private final int RIGHT_LEN;

        DEF_PLACEHOLDER_HANDLER(Map<String, Object> placeholderData, int leftLen, int rightLen) {
            this.DATA = placeholderData;
            this.LEFT_LEN = leftLen;
            this.RIGHT_LEN = rightLen;
        }

        @Override
        public Object apply(String text) {
            final String _key = text.substring(LEFT_LEN, text.length() - RIGHT_LEN).trim();
            if (DATA.containsKey(_key)) {
                return DATA.get(_key);
            }
            return text;
        }
    }

    /**
     * 占位符匹配检查
     *
     * @param value   待检查的值
     * @param pattern 占位符匹配模式
     * @return 是否有可匹配的占位符
     */
    private static boolean _placeholderMatch(String value, Pattern pattern) {
        if (null == value || 0 == value.length())
            return false;
        return pattern.matcher(value).find();
    }

    /**
     * 占位符替换
     *
     * @param value              要处理的值
     * @param pattern            占位符匹配模式
     * @param placeholderHandler 占位符数据处理逻辑
     * @return 处理后的值
     */
    private static String _placeholderReplace(String value, Pattern pattern, Function<String, Object> placeholderHandler) {
        Matcher _matcher = pattern.matcher(value);
        boolean _matched = _matcher.find();
        if (!_matched)
            return value;
        StringBuffer _buffer = new StringBuffer(value.length());
        while (_matched) {
            final String _ITEM = _matcher.group();
            Object _itemObj = placeholderHandler.apply(_ITEM);
            if (null == _itemObj) {
                _itemObj = "";
            }

//            if(_itemObj instanceof Map){
//                // 图片描述信息？
//            }

            // 先统一当做字符串来处理
            String _itemString = String.valueOf(_itemObj);
            // 修正jdk中Matcher的替换实现中遇到"$"符号报错的问题.
            if (-1 != _itemString.indexOf("$".intern()))
                _itemString = _itemString.replaceAll("\\$".intern(), "\\\\\\$".intern());
            _matcher.appendReplacement(_buffer, _itemString);
            _matched = _matcher.find();
        }
        _matcher.appendTail(_buffer);
        return _buffer.toString();
    }

    /**
     * 处理片段中的占位符
     *
     * @param paragraphs         片段集合
     * @param pattern            占位符匹配模式
     * @param placeholderHandler 占位符处理逻辑
     */
    static void processParagraphs(List<XWPFParagraph> paragraphs, Pattern pattern, Function<String, Object> placeholderHandler) {
        if (null == paragraphs || paragraphs.isEmpty())
            return;
        for (XWPFParagraph _paragraph : paragraphs) {
            String _paragraphText = _paragraph.getText();
//            System.out.println("_paragraphText:" + _paragraphText);
            if (!_placeholderMatch(_paragraphText, pattern))
                continue;
            List<XWPFRun> _runs = _paragraph.getRuns();
            for (XWPFRun _run : _runs) {
                String _runText = _run.getText(0);
//                System.out.println("_runText:" + _runText);
//                System.out.println("_runString:" + _run.toString());
                if (!_placeholderMatch(_runText, pattern))
                    continue;
                String _runTextNew = _placeholderReplace(_runText, pattern, placeholderHandler);
                _run.setText(_runTextNew, 0);
            }
        }
    }

    /**
     * 处理表格中的占位符
     *
     * @param tables             表格集合
     * @param pattern            占位符匹配模式
     * @param placeholderHandler 占位符处理逻辑
     */
    static void processTables(List<XWPFTable> tables, Pattern pattern, Function<String, Object> placeholderHandler) {
        if (null == tables || tables.isEmpty())
            return;
        for (XWPFTable _table : tables) {
            for (int _i = 0, _iLen = _table.getNumberOfRows(); _i < _iLen; _i++) {
                XWPFTableRow _row = _table.getRow(_i);
                List<XWPFTableCell> _cells = _row.getTableCells();
                for (XWPFTableCell _cell : _cells) {
                    String _cellText = _cell.getText();
//                    System.out.println("_cellText: " + _cellText);
                    if (!_placeholderMatch(_cellText, pattern))
                        continue;
                    processParagraphs(_cell.getParagraphs(), pattern, placeholderHandler);
                }
            }
        }
    }

    /**
     * 处理图片
     *
     * @param pictures 图片集合
     */
    static void processPictures(List<XWPFPictureData> pictures) {
        if (null == pictures || pictures.isEmpty())
            return;
        for (XWPFPictureData _picture : pictures) {
            System.out.println("_picture:" + _picture);
        }
    }


    /************************************************************
     ************* 单例样板代码。
     ************************************************************/
    private PoiWordUtils() {
        if (null != PoiWordUtilsHolder._INSTANCE) {
            // 防止使用反射API创建对象实例。
            throw new IllegalStateException("that's not allowed!");
        }
    }

    // 当前对象唯一实例持有者。
    private static final class PoiWordUtilsHolder {
        private static final PoiWordUtils _INSTANCE = new PoiWordUtils();
    }

    // 防止使用反序列化操作获取多个对象实例。
    private Object readResolve() throws ObjectStreamException {
        return PoiWordUtilsHolder._INSTANCE;
    }

    /**
     * 获取当前对象唯一实例。
     *
     * @return 当前对象唯一实例
     */
    public static PoiWordUtils me() {
        return PoiWordUtilsHolder._INSTANCE;
    }
}
