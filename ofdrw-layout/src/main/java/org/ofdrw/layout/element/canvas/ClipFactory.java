package org.ofdrw.layout.element.canvas;

import org.ofdrw.core.basicType.ST_Array;
import org.ofdrw.core.basicType.ST_Box;
import org.ofdrw.core.graph.pathObj.AbbreviatedData;
import org.ofdrw.core.graph.pathObj.CT_Path;
import org.ofdrw.core.pageDescription.clips.Area;
import org.ofdrw.core.pageDescription.clips.CT_Clip;
import org.ofdrw.core.pageDescription.clips.Clips;

/**
 * 裁剪区域构造器
 *
 * @author 权观宇
 * @since 2020-05-04 16:11:55
 */
public class ClipFactory implements Cloneable {

    /**
     * 裁剪路径数据
     */
    private AbbreviatedData data;

    /**
     * 裁剪路径的变换矩阵
     */
    private ST_Array ctm;

    /**
     * 是否填充
     */
    private Boolean fill;

    /**
     * 是否描边
     */
    private Boolean stroke;

    /**
     * 边框
     */
    private ST_Box boundary;

    public ClipFactory(AbbreviatedData data, ST_Array ctm) {
        this.data = data;
        this.ctm = ctm;
    }

    public ClipFactory(AbbreviatedData data) {
        this.data = data;
    }

    public ClipFactory() {
    }

    public AbbreviatedData getData() {
        return data;
    }

    public ClipFactory setData(AbbreviatedData data) {
        this.data = data;
        return this;
    }

    public ST_Array getCtm() {
        return ctm;
    }

    public ClipFactory setCtm(ST_Array ctm) {
        this.ctm = ctm;
        return this;
    }

    public void setFill(Boolean fill) {
        this.fill = fill;
    }

    public void setStroke(Boolean stroke) {
        this.stroke = stroke;
    }

    public void setBoundary(ST_Box boundary) {
        this.boundary = boundary;
    }

    /**
     * 构造裁剪对象
     *
     * @return 裁剪徐磊
     */
    public Clips clips() {
        Clips clips = new Clips();
        Area area = new Area();
        if (ctm != null) {
            area.setCTM(ctm.clone());
        }
        area.setClipObj(new CT_Path().setAbbreviatedData(data.clone()).setFill(fill).setStroke(stroke).setBoundary(boundary));
        return clips.addClip(new CT_Clip().addArea(area));
    }

    @Override
    public ClipFactory clone() {
        ClipFactory that = new ClipFactory();
        if (ctm != null) {
            that.ctm = this.ctm.clone();
        }
        if (data != null) {
            that.data = this.data.clone();
        }
        return that;
    }
}
