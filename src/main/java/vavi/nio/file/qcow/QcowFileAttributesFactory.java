/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.qcow;

import com.github.fge.filesystem.driver.ExtendedFileSystemDriverBase.ExtendedFileAttributesFactory;

import static vavi.nio.file.qcow.jna.libqcow.LibqcowLibrary.FILE;


/**
 * QcowFileAttributesFactory.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/07/24 umjammer initial version <br>
 */
public final class QcowFileAttributesFactory extends ExtendedFileAttributesFactory {

    public QcowFileAttributesFactory() {
        setMetadataClass(FILE.class);
        addImplementation("basic", QcowBasicFileAttributesProvider.class);
    }
}
