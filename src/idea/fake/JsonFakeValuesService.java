package idea.fake;

public interface JsonFakeValuesService {

    Object random();

    Object def();

    Object randomValue(String valueName);
}
