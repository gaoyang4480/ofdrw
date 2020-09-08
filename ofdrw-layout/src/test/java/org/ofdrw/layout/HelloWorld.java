package org.ofdrw.layout;

import org.ofdrw.layout.element.Paragraph;
import org.ofdrw.pkg.enums.ContainerType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author 权观宇
 * @since 2020-03-26 19:21:17
 */
public class HelloWorld {

//    public Appendable toAppendable(final Appendable a) {
//        Appendable out = a;
//        if (out == null) {
//            out = new StringBuilder(36);
//        }
//        try {
//            Hex.append(out, (int) (this.time >> 32)).append('-');
//            Hex.append(out, (short) (this.time >> 16)).append('-');
//            Hex.append(out, (short) this.time).append('-');
//            Hex.append(out, (short) (this.clockSeqAndNode >> 48)).append('-');
//            Hex.append(out, this.clockSeqAndNode, 12);
//        } catch (IOException ex) {
//        }
//        return out;
//    }


    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();

        Path path = Paths.get("ofdrw-layout/target/helloworld.ofd").toAbsolutePath();
        System.out.println(path.toString());
        try (OFDDoc ofdDoc = new OFDDoc(path, ContainerType.ZIP_MEMORY_FILE)) {
            Paragraph p = new Paragraph("你好呀，OFD Reader&Writer！");
            ofdDoc.add(p);
        }
        System.out.println("生成文档位置: " + path.toAbsolutePath());

        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
    }
}
