package com.sivannsan.millidb;

import com.sivannsan.foundation.annotation.Nonnull;

/**
 * The instance of this class does not locally work with the data.
 * It always communicates with the database.
 */
public interface MilliDBFile {
    /**
     * Gets the parent collection of this file
     * Note that the parent must be a collection, since only collection can hold files
     *
     * @return  null if this is the root file
     */
    MilliDBCollection getParent();

    /**
     * @return  the path from the root file
     */
    @Nonnull
    String getPath();

    @Nonnull
    String getName();

    boolean isMilliDBDocument();

    boolean isMilliDBCollection();

    /**
     * Casts this MilliDBFile as MilliDBDocument
     *
     * @throws ClassCastException   when this MilliDBFile is not a MilliDBDocument
     */
    @Nonnull
    MilliDBDocument asMilliDBDocument() throws ClassCastException;

    /**
     * Casts this MilliDBFile as MilliDBCollection
     *
     * @throws ClassCastException   when this MilliDBFile is not a MilliDBCollection
     */
    @Nonnull
    MilliDBCollection asMilliDBCollection() throws ClassCastException;

    void delete();
}
