package org.ofdrw.core.text;

import org.junit.jupiter.api.Test;
import org.ofdrw.TestTool;
import org.ofdrw.core.basicType.ST_Array;

import static org.junit.jupiter.api.Assertions.*;

public class TextCodeTest {
    public static TextCode textCodeCase() {
        String str = "G;:5=<>D&#";
        return new TextCode()
                .setX(0.6747)
                .setY(3.5101)
                .setDeltaX(new ST_Array(1.9472, 1.9472, 1.3, 1.3, 1.4, 1.9472, 1.9472, 1.5))
                .setContent(str);
    }

    @Test
    public void gen() throws Exception {
        TestTool.genXml("TextCode", textCodeCase());
    }
}