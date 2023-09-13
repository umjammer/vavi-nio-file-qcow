/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.qcow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileStore;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.fge.filesystem.driver.DoubleCachedFileSystemDriver;
import com.github.fge.filesystem.exceptions.IsDirectoryException;
import com.github.fge.filesystem.exceptions.UnsupportedOptionException;
import com.github.fge.filesystem.provider.FileSystemFactoryProvider;

import static vavi.nio.file.qcow.jna.libqcow.LibqcowLibrary.FILE;


/**
 * QcowFileSystemDriver.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/07/24 umjammer initial version <br>
 */
public final class QcowFileSystemDriver extends DoubleCachedFileSystemDriver<FILE> {

    private FILE fileSystem;

    public QcowFileSystemDriver(FileStore fileStore,
                                FileSystemFactoryProvider provider,
                                FILE fileSystem,
                                Map<String, ?> env) throws IOException {
        super(fileStore, provider);
        this.fileSystem = fileSystem;
        setEnv(env);
    }

    @Override
    protected String getFilenameString(FILE entry) {
        return null;
    }

    @Override
    protected boolean isFolder(FILE entry) {
        return false;
    }

    @Override
    protected FILE getRootEntry(Path path) throws IOException {
        return new FILE(null);
    }

    @Override
    protected InputStream downloadEntryImpl(FILE remoteFile, Path path, Set<? extends OpenOption> set) throws IOException {
        return null;
    }

    @Override
    protected OutputStream uploadEntry(FILE parentEntry, Path path, Set<? extends OpenOption> options) throws IOException {
        return null;
    }

    @Override
    protected List<FILE> getDirectoryEntries(FILE dirEntry, Path dir) throws IOException {
        return null;
    }

    @Override
    protected FILE createDirectoryEntry(FILE parentEntry, Path dir) throws IOException {
        throw new UnsupportedOptionException("createDirectory");
    }

    @Override
    protected boolean hasChildren(FILE dirEntry, Path dir) throws IOException {
        return getDirectoryEntries(dirEntry, dir).size() > 0;
    }

    @Override
    protected void removeEntry(FILE entry, Path path) throws IOException {
        throw new UnsupportedOptionException("remove");
    }

    @Override
    protected FILE copyEntry(FILE sourceEntry, FILE targetParentEntry, Path source, Path target, Set<CopyOption> options) throws IOException {
        throw new UnsupportedOptionException("copy");
    }

    @Override
    protected FILE moveEntry(FILE sourceEntry, FILE targetParentEntry, Path source, Path target, boolean targetIsParent) throws IOException {
        throw new UnsupportedOptionException("move");
    }

    @Override
    protected FILE moveFolderEntry(FILE sourceEntry, FILE targetParentEntry, Path source, Path target, boolean targetIsParent) throws IOException {
        // TODO java spec. allows empty folder
        throw new IsDirectoryException("source can not be a folder: " + source);
    }

    @Override
    protected FILE renameEntry(FILE sourceEntry, FILE targetParentEntry, Path source, Path target) throws IOException {
        throw new UnsupportedOptionException("rename");
    }
}
