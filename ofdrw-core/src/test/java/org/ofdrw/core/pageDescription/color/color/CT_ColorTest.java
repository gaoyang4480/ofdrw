package org.ofdrw.core.pageDescription.color.color;

import org.junit.jupiter.api.Test;
import org.ofdrw.TestTool;
import org.ofdrw.core.basicType.ST_Array;
import org.ofdrw.core.pageDescription.color.pattern.CT_Pattern;
import org.ofdrw.core.pageDescription.color.pattern.CT_PatternTest;

import static org.junit.jupiter.api.Assertions.*;

public class CT_ColorTest {

    public static CT_Color colorCase() {
        return new CT_Color()
                .setValue(new ST_Array(new String[]{"255", "255", "255"}))
                .setAlpha(255)
                .setColor(CT_PatternTest.patternCase());
    }

    @Test
    public void gen() throws Exception {
        TestTool.genXml("color", colorCase());
    }

}