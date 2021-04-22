package idea.template;

/**
 * 路径类型 枚举
 * @author zheng.li
 */

public enum PathTypeEnum {

    /**
     * service
     */
    SERVICE("service"),
    REQUEST("request"),
    MODEL("model"),
    CONTROLLER("controller"),
    INTERFACE("interface");

    private String name;

    PathTypeEnum(String name) {
        this.name = name;
    }

    public static PathTypeEnum getByName(String name) {
        if (name == null) {
            return null;
        }
        for (PathTypeEnum e : values()) {
            if (e.name.equals(name)) {
                return e;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
