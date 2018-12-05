package com.sucaiji.cjpan.config;

public enum Type {
    VIDEO(Property.VIDEO),
    IMAGE(Property.IMAGE),
    DOCUMENT(Property.DOCUMENT),
    MUSIC(Property.MUSIC),
    OTHER(Property.OTHER);
    private String name;
    Type(String name) {
        this.name = name;
    }
    public static Type getType(String str){
        Type type;
        switch (str) {
            case "image":
                type = Type.IMAGE;
                break;
            case "document":
                type = Type.DOCUMENT;
                break;
            case "video":
                type = Type.VIDEO;
                break;
            case "music":
                type = Type.MUSIC;
                break;
            default:
                type = Type.OTHER;
        }
        return type;
    }
    @Override
    public String toString() {
        return name;
    }
}
