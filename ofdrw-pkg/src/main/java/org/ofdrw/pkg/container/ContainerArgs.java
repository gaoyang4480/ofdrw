package org.ofdrw.pkg.container;

import org.ofdrw.pkg.enums.ContainerType;

import java.nio.file.Path;

/**
 * 容器构造参数类
 *
 * @author gaoyang
 * @version 1.0
 * @date 2020/09/08 17:24
 * @see org.ofdrw.pkg.container
 */
public class ContainerArgs {

    private Path fullDir;
    private ContainerType containerType;
    private ZipContainer zipContainer;

    public Path getFullDir() {
        return fullDir;
    }

    public void setFullDir(Path fullDir) {
        this.fullDir = fullDir;
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public void setContainerType(ContainerType containerType) {
        this.containerType = containerType;
    }

    public ZipContainer getZipContainer() {
        return zipContainer;
    }

    public void setZipContainer(ZipContainer zipContainer) {
        this.zipContainer = zipContainer;
    }

}
