package com.sivannsan.millidb;

import com.sivannsan.foundation.annotation.Nonnull;
import com.sivannsan.millidata.MilliData;

/**
 * The instance of this class does not locally work with the data.
 * It always communicates with the database.
 */
public interface MilliDBDocument extends MilliDBFile {
    /**
     * If the path is empty, this method will get the content of this MilliDBDocument, just like #getContent.
     * <p>
     * Putting number, integer without followed by zeros, in the path is treated as MilliList index.
     */
    @Nonnull
    MilliData get(@Nonnull String path) throws MilliDBResultFailedException;

    /**
     * If the path is empty, this method will set the content of this MilliDBDocument, just like #setContent.
     * If it points inside a MilliMap, this method will be like MilliMap#put.
     * And If it points inside a MilliList, this method will be like MilliList#update.
     * <p>
     * Putting number, integer without followed by zeros, in the path is treated as MilliList index.
     * <p>
     * MilliMaps are automatically created if they do not exist as in the path, but MilliLists.
     */
    void set(@Nonnull String path, @Nonnull MilliData value) throws MilliDBResultFailedException;

    @Nonnull
    MilliData getContent() throws MilliDBResultFailedException;

    void setContent(@Nonnull MilliData value) throws MilliDBResultFailedException;
}
