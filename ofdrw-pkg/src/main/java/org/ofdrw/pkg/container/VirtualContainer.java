package org.ofdrw.pkg.container;

import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.ofdrw.core.DefaultElementProxy;
import org.ofdrw.core.basicType.ST_Loc;
import org.ofdrw.pkg.enums.ContainerType;
import org.ofdrw.pkg.tool.ElemCup;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 虚拟容器对象
 *
 * @author 权观宇
 * @since 2020-04-02 19:01:04
 */
public class VirtualContainer implements Closeable {

    /**
     * 容器类型
     */
    protected ContainerType containerType;

    /**
     * ZIP内存容器
     */
    protected ZipContainer zipContainer;

    /**
     * 文件根路径(完整路径包含当前文件名)
     */
    private String fullPath;

    /**
     * 目录名称
     */
    private String name;

    /**
     * 所属容器
     */
    private VirtualContainer parent;

    /**
     * 文件缓存
     */
    private Map<String, Element> fileCache;

    /**
     * 目录中的虚拟容器缓存
     */
    private Map<String, VirtualContainer> dirCache;

    /**
     * 获取虚拟容器的名称
     *
     * @return 名称
     */
    public String getContainerName() {
        return name;
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    private VirtualContainer() {
        fileCache = new HashMap<>(7);
        dirCache = new HashMap<>(5);
        this.parent = this;
    }

    /**
     * 通过完整路径构造一个虚拟容器
     *
     * @param fullDir 容器完整路径
     * @throws IllegalArgumentException 参数错误
     */
    public VirtualContainer(Path fullDir) throws IllegalArgumentException {
        this(fullDir, ContainerType.FILE_SYSTEM);
    }

    public VirtualContainer(Path fullDir, ContainerType containerType) throws IllegalArgumentException {
        this();
        setContainer(fullDir, containerType, zipContainer);
    }

    public VirtualContainer(ContainerArgs containerArgs) throws IllegalArgumentException {
        this();
        setContainer(containerArgs.getFullDir(), containerArgs.getContainerType(), containerArgs.getZipContainer());
    }

    public void setContainer(Path fullDir, ContainerType containerType, ZipContainer zipContainer) {
        this.containerType = containerType;
        if (containerType == ContainerType.FILE_SYSTEM) {
            if (fullDir == null) {
                throw new IllegalArgumentException("完整路径(fullDir)为空");
            }
            // 目录不存在或不是一个目录
            if (Files.notExists(fullDir) || !Files.isDirectory(fullDir)) {
                try {
                    // 创建目录
                    fullDir = Files.createDirectories(fullDir);
                } catch (IOException e) {
                    throw new RuntimeException("无法创建指定目录", e);
                }
            }
            this.fullPath = fullDir.toAbsolutePath().toString();
            this.name = fullDir.getFileName().toString();
        } else if (containerType == ContainerType.ZIP_MEMORY_FILE) {
            if (fullDir != null) {
                this.fullPath = fullDir.toString();
                this.name = fullDir.getFileName().toString();
            }
            if (zipContainer != null) {
                this.zipContainer = zipContainer;
            } else {
                try {
                    this.zipContainer = new ZipContainer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 创建一个虚拟容器
     *
     * @param parent  根目录
     * @param dirName 新建目录的名称
     * @throws IllegalArgumentException 参数异常
     */
    public VirtualContainer(Path parent, String dirName) throws IllegalArgumentException {
        this();
        if (parent == null) {
            throw new IllegalArgumentException("根路径(parent)为空");
        }
        Path fullPath = Paths.get(parent.toAbsolutePath().toString(), dirName);
        if (Files.notExists(fullPath) || !Files.isDirectory(fullPath)) {
            try {
                fullPath = Files.createDirectories(fullPath);
            } catch (IOException e) {
                throw new RuntimeException("无法创建指定目录", e);
            }
        }
        if (!Files.isDirectory(parent)) {
            throw new IllegalStateException("请传入基础目录路径，而不是文件");
        }
        this.fullPath = fullPath.toAbsolutePath().toString();
        this.name = dirName;
    }

    /**
     * 获取当前容器完整路径
     *
     * @return 容器完整路径（绝对路径）
     */
    public String getSysAbsPath() {
        return fullPath;
    }

    /**
     * 向虚拟容器中加入文件
     *
     * @param file 文件路径对象
     * @return this
     * @throws IOException IO异常
     */
    public VirtualContainer putFile(Path file) throws IOException {
        if (file == null || Files.notExists(file) || Files.isDirectory(file)) {
            // 为空或是一个文件夹，或者不存在
            return this;
        }
        String fileName = file.getFileName().toString();
        Path target = Paths.get(fullPath, fileName);
        // 如果文件已经在目录中那么不做任何事情
        if (target.toAbsolutePath().toString()
                .equals(file.toAbsolutePath().toString())) {
            return this;
        }
        // 复制文件到指定目录
        Files.copy(file, target);
        return this;
    }

    public VirtualContainer putFile(String resourceFileName, byte[] resourceData) throws IOException {
        if (resourceData == null || resourceFileName == null || resourceFileName.isEmpty()) {
            return this;
        }
        Path target = Paths.get(fullPath, resourceFileName);
        if (containerType == ContainerType.FILE_SYSTEM) {
            // 写文件到指定目录.
            FileOutputStream outputStream = new FileOutputStream(target.toAbsolutePath().toString());
            outputStream.write(resourceData);
            outputStream.close();
        } else if (containerType == ContainerType.ZIP_MEMORY_FILE) {
            zipContainer.addFile(target, resourceData);
        }

        return this;
    }

    /**
     * 向虚拟容器加入对象
     *
     * @param fileName 文件名
     * @param element  元素对象
     * @return this
     */
    public VirtualContainer putObj(String fileName, Element element) {
        if (fileName == null || fileName.length() == 0) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (element == null) {
            return this;
        }
        while (element instanceof DefaultElementProxy) {
            // 如果是代理元素对象那么取出被代理的对象存储
            element = ((DefaultElementProxy) element).getProxy();
        }
        fileCache.put(fileName, element);
        return this;
    }

    /**
     * 通过文件名获取元素对象
     *
     * @param fileName 文件名
     * @return 元素对象（不含代理）
     * @throws FileNotFoundException 文件不存在
     * @throws DocumentException     元素序列化异常
     */
    public Element getObj(String fileName) throws FileNotFoundException, DocumentException {
        if (fileName == null || fileName.length() == 0) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        Element element = fileCache.get(fileName);
        if (element == null) {
            // 缓存中不存在，从文件目录中尝试读取
            Path file = getFile(fileName);
            if (containerType == ContainerType.FILE_SYSTEM) {
                // 反序列化文件为对象
                element = ElemCup.inject(file);
            } else if (containerType == ContainerType.ZIP_MEMORY_FILE) {
                byte[] fileData = zipContainer.readFile(file);
                if (fileData == null) {
                    throw new FileNotFoundException("无法在ZIP: " + fullPath + "中找到，文件 [ " + fileName + " ]");
                }
                element = ElemCup.inject(fileData);
            }
            if (element != null) {
                // 从文件加载元素，那么缓存该元素对象
                fileCache.put(fileName, element);
            }
        }
        return element;
    }

    /**
     * 获取容器中的文件对象
     *
     * @param fileName 文件名称
     * @return 文件路径对象
     * @throws FileNotFoundException 文件不存在
     */
    public Path getFile(String fileName) throws FileNotFoundException {
        if (fileName == null || fileName.length() == 0) {
            throw new IllegalArgumentException("文件名为空");
        }
        Path res = Paths.get(fullPath, fileName);
        if (containerType == ContainerType.FILE_SYSTEM) {
            if (Files.isDirectory(res) || Files.notExists(res)) {
                throw new FileNotFoundException("无法在目录: " + fullPath + "中找到，文件 [ " + fileName + " ]");
            }
        }
        return res;
    }

    /**
     * 获取一个虚拟容器对象
     * <p>
     * 如果容器存在，那么取出元素
     * <p>
     * 如果容器不存在，那么创建一个新的对象
     *
     * @param name          容器名称
     * @param containerType 容器类型
     * @param zipContainer  容器对象
     * @param mapper        容器构造器引用
     * @param <R>           容器子类
     * @return 新建或已经存在的容器
     */
    public <R extends VirtualContainer> R obtainContainer(String name, ContainerType containerType, ZipContainer zipContainer, Function<ContainerArgs, R> mapper) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("容器名称（name）为空");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("容器构建对象（mapper）为空");
        }
        // 检查缓存
        VirtualContainer target = dirCache.get(name);
        if (target == null) {
            Path p = Paths.get(name);
            if (this.fullPath != null) {
                p = Paths.get(this.fullPath, name);
            }
            // 如果目录不存在那么创建，如果已经存在那么就是加载
            ContainerArgs containerArgs = new ContainerArgs();
            containerArgs.setFullDir(p);
            containerArgs.setContainerType(containerType);
            containerArgs.setZipContainer(zipContainer);
            R ct = mapper.apply(containerArgs);
            // 设置父母路径
            ct.setParent(this);
            // 加入缓存中
            dirCache.put(name, ct);
            return ct;
        } else {
            return (R) target;
        }
    }

    public <R extends VirtualContainer> R obtainContainer(String name, Function<Path, R> mapper) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("容器名称（name）为空");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("容器构建对象（mapper）为空");
        }
        // 检查缓存
        VirtualContainer target = dirCache.get(name);
        if (target == null) {
            Path p = Paths.get(name);
            if (this.fullPath != null) {
                p = Paths.get(this.fullPath, name);
            }
            // 如果目录不存在那么创建，如果已经存在那么就是加载
            R ct = mapper.apply(p);
            // 设置父母路径
            ct.setParent(this);
            // 加入缓存中
            dirCache.put(name, ct);
            return ct;
        } else {
            return (R) target;
        }
    }

    /**
     * 获取虚拟容器
     *
     * @param <R>    容器子类
     * @param name   容器名称
     * @param mapper 容器构造器引用
     * @return 容器对象
     * @throws FileNotFoundException 文件不存在
     */
    public <R extends VirtualContainer> R getContainer(String name, Function<Path, R> mapper) throws FileNotFoundException {
        Path p = Paths.get(name);
        if (this.fullPath != null) {
            p = Paths.get(this.fullPath, name);
        }
        if (containerType == ContainerType.FILE_SYSTEM) {
            if (Files.notExists(p) || !Files.isDirectory(p)) {
                throw new FileNotFoundException("容器内无法找名为：" + name + "目录");
            }
        }
        // 检查缓存
        VirtualContainer target = dirCache.get(name);
        if (target == null) {
            // 调用指定构造器创建容器对象
            R ct = mapper.apply(p);
            // 设置所属容器为创建者
            ct.setParent(this);
            // 设置容器.
            ct.setContainer(p, containerType, zipContainer);
            // 加入缓存中
            dirCache.put(name, ct);
            return ct;
        } else {
            return (R) target;
        }
    }

    public <R extends VirtualContainer> R getContainerByContainerArgs(String name, Function<ContainerArgs, R> mapper) throws FileNotFoundException {
        Path p = Paths.get(name);
        if (this.fullPath != null) {
            p = Paths.get(this.fullPath, name);
        }
        if (containerType == ContainerType.FILE_SYSTEM) {
            if (Files.notExists(p) || !Files.isDirectory(p)) {
                throw new FileNotFoundException("容器内无法找名为：" + name + "目录");
            }
        }
        // 检查缓存
        VirtualContainer target = dirCache.get(name);
        if (target == null) {
            // 调用指定构造器创建容器对象
            ContainerArgs containerArgs = new ContainerArgs();
            containerArgs.setFullDir(p);
            containerArgs.setContainerType(this.containerType);
            containerArgs.setZipContainer(this.zipContainer);
            R ct = mapper.apply(containerArgs);
            // 设置所属容器为创建者
            ct.setParent(this);
            // 加入缓存中
            dirCache.put(name, ct);
            return ct;
        } else {
            return (R) target;
        }
    }

    /**
     * 获取该容器所属容器
     *
     * @return 所属容器对象
     */
    public VirtualContainer getParent() {
        return parent;
    }

    /**
     * 设置所属容器
     *
     * @param parent 容器
     * @return this
     */
    protected VirtualContainer setParent(VirtualContainer parent) {
        this.parent = parent;
        return this;
    }

    /**
     * 获取虚拟容器所处的文件系统路径
     *
     * @return 文件系统路径
     */
    public Path getContainerPath() {
        return Paths.get(fullPath);
    }

    /**
     * 判断文件或对象是否存在
     *
     * @param fileName 文件名称
     * @return true - 存在;false - 不存在
     */
    public boolean exit(String fileName) {
        if (fileName == null || fileName.length() == 0) {
            return false;
        }
        Element element = fileCache.get(fileName);
        if (element == null) {
            // 缓存中不存在，从文件目录中尝试读取
            Path res = Paths.get(fullPath, fileName);
            if (Files.isDirectory(res) || Files.notExists(res)) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }


    /**
     * 删除整个虚拟容器
     */
    public void clean() {
        if (containerType == ContainerType.FILE_SYSTEM) {
            try {
                Path path = getContainerPath();
                // 删除整个文件目录
                if (Files.exists(path)) {
                    FileUtils.deleteDirectory(path.toFile());
                }
                this.fileCache.clear();
                this.dirCache.clear();
            } catch (Exception e) {
                System.err.println("容器删除异常: " + e.getMessage());
            }
        }
    }

    /**
     * 将缓存中的对象写入到文件系统中
     *
     * @throws IOException 文件读写IO异常
     */
    public void flush() throws IOException {
        if (containerType == ContainerType.FILE_SYSTEM) {
            // 刷新元素对象到指定目录
            for (Map.Entry<String, Element> kv : fileCache.entrySet()) {
                Path filePath = Paths.get(fullPath, kv.getKey());
                //System.out.println(filePath.toString());
                // 序列化为文件.
                ElemCup.dump(kv.getValue(), filePath);
            }
        } else if (containerType == ContainerType.ZIP_MEMORY_FILE) {
            for (Map.Entry<String, Element> kv : fileCache.entrySet()) {
                Path filePath = Paths.get(kv.getKey());
                if (this.fullPath != null) {
                    filePath = Paths.get(this.fullPath, kv.getKey());
                }
                //System.out.println(filePath.toString());
                // 序列化为内存对象.
                byte[] fileData = ElemCup.dump(kv.getValue());
                zipContainer.addFile(filePath, fileData);
            }
        }
        // 递归的刷新容器中包含的其他容器
        for (VirtualContainer container : dirCache.values()) {
            container.flush();
        }
        fileCache.clear();
        dirCache.clear();
    }

    /**
     * 从缓存中刷新指定容器到文件系统中
     *
     * @param name 容器名称
     * @return this
     * @throws IOException 写入文件IO异常
     */
    public VirtualContainer flushContainerByName(String name) throws IOException {
        if (name == null || name.trim().isEmpty()) {
            return this;
        }
        VirtualContainer virtualContainer = dirCache.get(name);
        if (virtualContainer != null) {
            virtualContainer.flush();
        }
        return this;
    }

    /**
     * 从缓存将指定对象写入到文件系统中
     *
     * @param name 文件名称
     * @return this
     * @throws IOException 写入文件IO异常
     */
    public VirtualContainer flushFileByName(String name) throws IOException {
        if (name == null || name.trim().isEmpty()) {
            return this;
        }
        Element element = fileCache.get(name);
        if (element != null) {
            Path filePath = Paths.get(fullPath, name);
            ElemCup.dump(element, filePath);
        }
        return this;
    }

    /**
     * 获取在容器中的绝对路径
     *
     * @return 绝对路径对象
     */
    public ST_Loc getAbsLoc() {
        ST_Loc absRes = null;
        if (parent == this) {
            absRes = new ST_Loc("/");
        } else {
            absRes = parent.getAbsLoc().cat(this.name);
        }
        return absRes;
    }

    @Override
    public void close() throws IOException {
        // 删除工作过程中存放于虚拟容器中的文件和目录
        flush();
    }

}
