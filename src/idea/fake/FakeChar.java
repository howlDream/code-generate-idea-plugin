package idea.fake;

public class FakeChar implements JsonFakeValuesService {
    @Override
    public Object random() {
        return 'c';
    }

    @Override
    public Object def() {
        return 'c';
    }

    @Override
    public Object randomValue(String valueName) {
        return 'c';
    }

}
