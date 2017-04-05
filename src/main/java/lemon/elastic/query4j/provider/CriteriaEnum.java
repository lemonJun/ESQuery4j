//package lemon.elastic.query4j.provider;
//
///**
// * 查询参数值的枚举类型  要求必须按指定格式赋值
// *
// * @author WangYazhou
// * @date  2015年12月9日 下午4:46:56
// * @see
// */
//public enum CriteriaEnum {
//
//    EQ("eq"), //默认就是=
//    DATE("date"), //日期类型  Date.getTime_Date.getTime
//    RANGE("range"), //范围类型  value1_value2 
//    CONTAINS("contains"), //包含
//    GEO("geo"), //地理范围
//    KEYWORD("keyword"), //关键词
//    NOTEQ("noteq");//不等于
//
//    private String value;
//
//    private CriteriaEnum(String value) {
//        this.value = value;
//    }
//
//    public String getValue() {
//        return value;
//    }
//
//}
