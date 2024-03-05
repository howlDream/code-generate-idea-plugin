package idea.fake;

public class FakeString implements JsonFakeValuesService {

    @Override
    public Object random() {
        return "fake_data";
    }

    @Override
    public Object def() {
        return "";
    }

    @Override
    public Object randomValue(String valueName) {
        if ("pictureUrl".equals(valueName)) {
            return "https://img0.baidu.com/it/u=1721391133,702358773&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=625";
        }
        else if ("corpid".equals(valueName) || "userId".equals(valueName)) {
            return "1";
        } else if ("platform".equals(valueName)) {
            return "all";
        } else if ("frontDev".equals(valueName)) {
            return "1";
        }


        return "";
    }

}
