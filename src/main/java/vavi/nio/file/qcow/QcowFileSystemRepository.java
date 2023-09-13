/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.qcow;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import com.github.fge.filesystem.driver.FileSystemDriver;
import com.github.fge.filesystem.provider.FileSystemRepositoryBase;


/**
 * QcowFileSystemRepository.
 * <p>
 * env
 * <ul>
 * <li> volumeName ... string
 * </ul>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/07/24 umjammer initial version <br>
 */
public final class QcowFileSystemRepository extends FileSystemRepositoryBase {

    /** */
    public QcowFileSystemRepository() {
        super("qcow", new QcowFileSystemFactoryProvider());
    }

    /**
     */
    @Override
    public FileSystemDriver createDriver(URI uri, Map<String, ?> env) throws IOException {

        QcowFileStore fileStore = new QcowFileStore(null, factoryProvider.getAttributesFactory());
        return new QcowFileSystemDriver(fileStore, factoryProvider, null, env);
    }
}
