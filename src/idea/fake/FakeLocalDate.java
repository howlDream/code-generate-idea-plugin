package idea.fake;

import java.time.LocalDate;
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

    @Override
    public Object randomValue(String valueName) {
        return formatter.format(LocalDate.now());
    }

}
