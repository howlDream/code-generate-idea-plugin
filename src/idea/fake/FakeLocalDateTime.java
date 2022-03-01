package idea.fake;

import java.util.Date;

public class FakeLocalDateTime implements JsonFakeValuesService {
    @Override
    public Object random() {
        return null;
    }

    @Override
    public Object def() {
        return null;
    }

    @Override
    public Object randomValue(String valueName) {
        return new Date();
    }
}
