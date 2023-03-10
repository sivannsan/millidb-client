package com.sivannsan.millidb;

import com.sivannsan.foundation.annotation.Nonnull;

import java.util.List;

/**
 * The instance of this class does not locally work with the data.
 * It always communicates with the database.
 */
public interface MilliDBClient {
    /**
     * The same as MilliDBCollection#getFiles, but they are root
     */
    @Nonnull
    List<MilliDBFile> getFiles() throws MilliDBResultFailedException;

    /**
     * The same as MilliDBCollection#getFiles, but they are root
     */
    @Nonnull
    List<MilliDBFile> getFiles(MilliDBFilter filter) throws MilliDBResultFailedException;

    /**
     * The same as MilliDBCollection#getDocument, but it is root
     */
    @Nonnull
    MilliDBDocument getDocument(@Nonnull String name) throws MilliDBResultFailedException, MilliDBPermissionException;

    /**
     * The same as MilliDBCollection#getCollection, but it is root
     */
    @Nonnull
    MilliDBCollection getCollection(@Nonnull String name) throws MilliDBResultFailedException, MilliDBPermissionException;
}
