package forum;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
public class DemoData {
    @ExcelProperty("渠道")
    private String date1;
    @ExcelProperty("保单推送日")
    private String date2;
    @ExcelProperty("保险公司")
    private String date3;
    @ExcelProperty("保单号")
    private String date4;
    @ExcelProperty("分单号")
    private String date5;
    @ExcelProperty("被保人姓名")
    private String date6;
    @ExcelProperty("被保人证件类型")
    private String date7;
    @ExcelProperty("被保人证件号")
    private String date8;
    @ExcelProperty("被保人手机号码")
    private String date9;
    @ExcelProperty("被保人出生日期")
    private String date10;
    @ExcelProperty("投保人姓名")
    private String date11;
    @ExcelProperty("投保人手机号码")
    private String date12;
    @ExcelProperty("保单状态")
    private String date13;
    @ExcelProperty("保单生效日")
    private String date14;
    @ExcelProperty("保单结束日")
    private String date15;
    @ExcelProperty("险种代码")
    private String date16;
    @ExcelProperty("险种名称")
    private String date17;
    @ExcelProperty("保额")
    private String date18;
    @ExcelProperty("剩余保额")
    private String date19;
    @ExcelProperty("等待期截止日期")
    private String date20;
    @ExcelProperty("险种初始生效日")
    private String date21;
    @ExcelProperty("险种生效日")
    private String date22;
    @ExcelProperty("险种结束日")
    private String date23;
    @ExcelProperty("免赔额")
    private String date24;
    @ExcelProperty("赔付比例")
    private String date25;
    @ExcelProperty("特别约定")
    private String date26;
    @ExcelProperty("被保人社保标记")
    private String date27;
    @ExcelProperty("既往理赔史")
    private String date28;
    @ExcelProperty("业务渠道")
    private String date29;
    @ExcelProperty("分公司编号")
    private String date30;
    @ExcelProperty("缴费方式")
    private String date31;
    @ExcelProperty("保费")
    private String date32;
    @ExcelProperty("状态变更时间")
    private String date33;

}