package idea.postman;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * postman collection construct
 * @author zheng.li
 */
public class PostmanCollection {

    private Info info;

    private List<Item0> item;

    private PostmanCollection(Info info, List<Item0> item) {
        this.info = info;
        this.item = item;
    }

    public Info getInfo() {
        return info;
    }

    public List<Item0> getItem() {
        return item;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public void setItem(List<Item0> item) {
        this.item = item;
    }


    /**
     * collection info
     */
    public static class Info {

        public String postmanId = "";

        public String name;

        public String schema = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json";

    }

    /**
     * outer item
     */
    public static class Item0 {

        public String name;

        public List<Item1> item;

    }

    /**
     * inner item
     */
    public static class Item1 {

        public String name;

        public Request request;

        public List<String> response;

        private Item1(String name,Request request,List<String> response) {
            this.name = name;
            this.request = request;
            this.response = response;
        }

    }

    public static class Request {

        public String method = "POST";

        public List<String> header = new ArrayList<>();

        public Body body = new Body();

        public Url url;

    }

    public static class Body {

        public String mode = "raw";

        public String raw = "{}";

        public Options options = new Options();

    }

    public static class Options {

        public RawJson raw = new RawJson();
    }

    public static class RawJson {
        public String language = "json";
    }

    public static class Url {

        public String raw;

        public List<String> host = Collections.singletonList("localhost");

        public String port;

        public List<String> path;

    }

    /**
     * PostmanCollection builder
     */
    public static class PostmanCollectionBuilder {
        private Info info;
        private List<Item0> item;

        private PostmanCollectionBuilder(Info info, List<Item0> item) {
            this.info = info;
            this.item = item;
        }

        public static PostmanCollectionBuilder aPostmanCollection(Info info, List<Item0> item) {
            return new PostmanCollectionBuilder(info, item);
        }

        public PostmanCollection build() {

            return new PostmanCollection(info, item);
        }
    }

    /**
     * Item1 builder
     */
    public static class Item1Builder {

        private String name;

        private Request request;

        private List<String> response;

        private Item1Builder(String name,Request request,List<String> response) {
            this.name = name;
            this.request = request;
            this.response = response;
        }

        public static Item1Builder buildItem1 (String port,String apiPath,String[] pathArray,String raw) {

            PostmanCollection.Url url = new PostmanCollection.Url();
            url.raw = apiPath;
            url.port = port;
            url.path = Arrays.asList(pathArray);

            PostmanCollection.Request request = new PostmanCollection.Request();
            request.url = url;
            PostmanCollection.Body body = new PostmanCollection.Body();
            body.raw = raw;
            request.body = body;

            int lastIndex = apiPath.lastIndexOf("/");
            return new Item1Builder(apiPath.substring(lastIndex),request,new ArrayList<>());
        }

        public Item1 build() {
            return new Item1(name,request,response);
        }

    }

}
