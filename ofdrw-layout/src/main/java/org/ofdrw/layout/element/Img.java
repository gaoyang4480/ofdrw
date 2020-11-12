package org.ofdrw.layout.element;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 图片对象
 * <p>
 * 为了防止与Image对象命名冲突采用Img缩写
 *
 * @author 权观宇
 * @since 2020-02-03 03:34:31
 */
public class Img extends Div {

    /**
     * 图片文件路径
     */
    private Path src;

    /**
     * 图片文件名
     */
    private String imageName;

    /**
     * 图片数据
     */
    private byte[] imageData;

    /**
     * CTM
     */
    private double[] ctm;

//    /**
//     * 是否保持比例缩放
//     * <p>
//     * true - 保持比例缩放
//     * false - 拉伸适应width和height
//     */
//    private boolean fit;

    private Img() {
//        this.fit = true;
        // 图片对象不可拆分
        this.setIntegrity(true);
    }

    public Img(double width, double height, Path src) throws IOException {
        this();
        if (src == null || Files.notExists(src)) {
            throw new IllegalArgumentException("图片文件为空或不存在");
        }
        this.src = src;
        this.setWidth(width / 5)
                .setHeight(height / 5);
    }

    public Img(double width, double height, String imageName, byte[] imageData) throws IOException {
        this();
        if (imageData == null || imageName == null || imageName.isEmpty()) {
            throw new IllegalArgumentException("图片数据或图片名为空");
        }
        this.imageName = imageName;
        this.imageData = imageData;
        this.setWidth(width / 5)
                .setHeight(height / 5);
    }

    public Img(Path src) throws IOException {
        this();
        if (src == null || Files.notExists(src)) {
            throw new IllegalArgumentException("图片文件为空或不存在");
        }
        this.src = src;
        parseImg();
    }

    private void parseImg() throws IOException {
        File picture = src.toFile();
        try (FileInputStream fIn = new FileInputStream(picture);) {
            BufferedImage sourceImg = ImageIO.read(fIn);
            this.setWidth((double) sourceImg.getWidth() / 5);
            this.setHeight((double) sourceImg.getHeight() / 5);
        }
    }

    public Path getSrc() {
        return src;
    }

    public String getImageName() {
        return imageName;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public double[] getCtm() {
        return ctm;
    }

    public void setCtm(double[] ctm) {
        this.ctm = ctm;
    }

//    public boolean isFit() {
//        return fit;
//    }
//
//    public Img setFit(boolean fit) {
//        this.fit = fit;
//        return this;
//    }

    public Img setSrc(Path src) {
        this.src = src;
        return this;
    }

    /**
     * 不允许切分
     */
    @Override
    public Div[] split(double sHeight) {
        throw new RuntimeException("图片对象不支持分割操作");
    }
}
