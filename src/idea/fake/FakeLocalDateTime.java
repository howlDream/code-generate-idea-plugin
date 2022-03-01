package idea.fake;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FakeLocalDateTime implements JsonFakeValuesService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

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
        return formatter.format(LocalDateTime.now());
    }
}
