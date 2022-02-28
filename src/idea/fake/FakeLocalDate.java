package idea.fake;

import java.time.format.DateTimeFormatter;
import java.util.Date;

public class FakeLocalDate implements JsonFakeValuesService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Object random() {
        return new Date();
    }

    @Override
    public Object def() {
        return new Date();
    }

}
