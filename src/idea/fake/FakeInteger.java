package idea.fake;

import java.util.Random;

public class FakeInteger implements JsonFakeValuesService {

    private final Random random = new Random();

    @Override
    public Object random() {
        return random.nextInt(100);
    }

    @Override
    public Object def() {
        return 0;
    }

    @Override
    public Object randomValue(String valueName) {
        if ("pageIndex".equals(valueName)) {
            return 1;
        }
        if ("pageSize".equals(valueName)) {
            return 10;
        }
        return 0;
    }
}
