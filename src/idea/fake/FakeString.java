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

}
