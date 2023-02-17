package com.sivannsan.millidb;

import com.sivannsan.foundation.annotation.Nonnull;

import java.util.List;

/**
 * The instance of this class does not locally work with the data.
 * It always communicates with the database.
 */
public interface MilliDBCollection extends MilliDBFile, Iterable<MilliDBFile> {
    /**
     * Get a list of permitted MilliDBFiles.
     */
    @Nonnull
    List<MilliDBFile> getFiles() throws MilliDBResultFailedException;

    /**
     * Get a list of permitted MilliDBFiles if each of them goes with the filter.
     */
    @Nonnull
    List<MilliDBFile> getFiles(MilliDBFilter filter) throws MilliDBResultFailedException;

    /**
     * Get a permitted MilliDBDocument.
     * If it does not exist, it will automatically create for you.
     *
     * @param name  must end with .mll extension
     */
    @Nonnull
    MilliDBDocument getDocument(@Nonnull String name) throws MilliDBResultFailedException, MilliDBPermissionException;

    /**
     * Get a permitted MilliDBCollection.
     * If it does not exist, it will automatically create for you.
     *
     * @param name  must NOT end with .mll extension
     */
    @Nonnull
    MilliDBCollection getCollection(@Nonnull String name) throws MilliDBResultFailedException, MilliDBPermissionException;
}
