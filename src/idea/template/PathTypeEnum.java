package idea.template;

/**
 * 路径类型 枚举
 * @author zheng.li
 */

public enum PathTypeEnum {

    /**
     * service层路径
     */
    SERVICE("service"),
    POJO("pojo"),
    CONTROLLER("controller"),
    ENTITY("entity"),
    DAO("dao")
    ;

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
