package forum;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import org.junit.Test;

/**
 * @author NERV
 * @title: EasyExcelTest
 * @projectName bbs-pro
 * @description:
 * @date 2022/8/14 下午 04:46
 */
public class EasyExcelTest {
    /**
     * 指定列的下标或者列名
     */
    @Test
    public void indexOrNameRead() {
        String fileName = "C:\\Users\\92588\\Desktop\\保单导入模板 .xlsx";
        // 有个很重要的点 DemoDataListener 不能被spring管理，要每次读取excel都要new,然后里面用到spring可以构造方法传进去
        // 写法3：
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        EasyExcel.read(fileName, DemoData.class, new DemoDataListener()).sheet().doRead();

        // 写法4
        // 一个文件一个reader
        try (ExcelReader excelReader = EasyExcel.read(fileName, DemoData.class, new DemoDataListener()).build()) {
            // 构建一个sheet 这里可以指定名字或者no
            ReadSheet readSheet = EasyExcel.readSheet(0).build();
            // 读取一个sheet
            excelReader.read(readSheet);
        }
    }
}