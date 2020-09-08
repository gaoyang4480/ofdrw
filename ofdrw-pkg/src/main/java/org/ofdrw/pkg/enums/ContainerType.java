package org.ofdrw.pkg.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 容器类型
 *
 * @author gaoyang
 * @version 1.0
 * @date 2020/09/08 13:52
 * @see org.ofdrw.pkg.enums
 */
public enum ContainerType {

    /**
     * 文件系统
     */
    FILE_SYSTEM(0, "文件系统"),
    /**
     * ZIP内存文件
     */
    ZIP_MEMORY_FILE(1, "ZIP内存文件");

    private static final Map<Integer, ContainerType> CODE_MAP = new HashMap<>();

    static {
        for (ContainerType typeEnum : ContainerType.values()) {
            CODE_MAP.put(typeEnum.getCode(), typeEnum);
        }
    }

    ContainerType(int code, String meaning) {
        this.code = code;
        this.meaning = meaning;
    }

    public static ContainerType getEnum(Integer code) {
        return CODE_MAP.get(code);
    }

    private final int code;
    private final String meaning;

    public int getCode() {
        return code;
    }

    public String getMeaning() {
        return meaning;
    }

}
