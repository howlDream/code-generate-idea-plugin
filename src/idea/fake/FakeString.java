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
        return "";
    }

}
