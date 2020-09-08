package org.ofdrw.pkg.container;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import org.ofdrw.pkg.util.ZipUtil;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存ZIP容器
 *
 * @author gaoyang
 * @version 1.0
 * @date 2020/09/08 13:37
 * @see org.ofdrw.pkg.container
 */
public class ZipContainer {

    private ByteArrayOutputStream byteArrayOutputStream;
    private ZipOutputStream zipOutputStream;
    private Map<String, byte[]> filePathToFileDataMap;

    public ZipContainer() throws IOException {
        initContainer();
    }

    private void initContainer() throws IOException {
        filePathToFileDataMap = new ConcurrentHashMap<>();
        byteArrayOutputStream = new
                ByteArrayOutputStream();
        zipOutputStream = new
                ZipOutputStream(byteArrayOutputStream);
    }

    public void addFile(Path fileName, byte[] fileData) throws IOException {
        String fileNameStr = fileName.toString();
        try {
            ZipUtil.addFileToZip(fileNameStr, fileData, zipOutputStream);
        } catch (ZipException e) {
            e.printStackTrace();
        }
        if (!filePathToFileDataMap.containsKey(fileNameStr)) {
            filePathToFileDataMap.put(fileNameStr, fileData);
        }
    }

    public byte[] readFile(Path file) {
        String filePathStr = file.toString();
        if (filePathToFileDataMap.containsKey(filePathStr)) {
            return filePathToFileDataMap.get(filePathStr);
        }
        return null;
    }

    public void jar(Path filePath) throws IOException {
        if (zipOutputStream != null) {
            try {
                ZipUtil.closeZipOutputStream(zipOutputStream);
            } catch (ZipException e) {
                e.printStackTrace();
            }
            filePathToFileDataMap.clear();
            byte[] zipData = byteArrayOutputStream.toByteArray();
            if (zipData.length > 0) {
                FileOutputStream fileOutputStream = new FileOutputStream(filePath.toAbsolutePath().toString());
                fileOutputStream.write(zipData);
                fileOutputStream.close();
            }
        }
    }

}
