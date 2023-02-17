package com.sivannsan.millidb;

import com.sivannsan.foundation.annotation.Nonnull;
import com.sivannsan.foundation.Validate;
import com.sivannsan.millidata.MilliData;
import com.sivannsan.millidata.MilliMap;
import com.sivannsan.millidata.MilliNull;
import com.sivannsan.millidata.MilliValue;

public final class MilliDBResult {
    private final long id;
    private final boolean isSucceed;
    @Nonnull
    private final MilliData metadata;

    public MilliDBResult(long id, boolean isSucceed, @Nonnull MilliData metadata) {
        this.id = id;
        this.isSucceed = isSucceed;
        this.metadata = Validate.nonnull(metadata);
    }

    public long getID() {
        return id;
    }

    public boolean isSucceed() {
        return isSucceed;
    }

    @Nonnull
    public MilliData getMetadata() {
        return metadata;
    }

    @Nonnull
    public MilliMap toMilliMap() {
        return new MilliMap().append("id", new MilliValue(id)).append("s", new MilliValue(isSucceed)).append("m", metadata);
    }

    @Nonnull
    public static MilliDBResult failedResult(long id) {
        return new MilliDBResult(id, false, MilliNull.INSTANCE);
    }

    @Nonnull
    public static MilliDBResult invalidResult() {
        return failedResult(-1);
    }

    public static final class Parser {
        @Nonnull
        public static MilliDBResult parse(@Nonnull String result, @Nonnull MilliDBResult defaultValue) {
            try {
                return parse(Validate.nonnull(result));
            } catch (MilliDBResultParsedException e) {
                return Validate.nonnull(defaultValue);
            }
        }

        @Nonnull
        public static MilliDBResult parse(@Nonnull String result) throws MilliDBResultParsedException {
            MilliMap map = MilliData.Parser.parse(Validate.nonnull(result), new MilliMap()).asMilliMap(new MilliMap());
            long parsedID = map.get("id").asMilliValue(new MilliValue(-1)).asInteger64();
            if (parsedID < 0) throw new MilliDBResultParsedException();
            boolean parsedIsSucceed = map.get("s").asMilliValue(new MilliValue(false)).asBoolean();
            MilliData parsedMetadata = map.get("m");
            return new MilliDBResult(parsedID, parsedIsSucceed, parsedMetadata);
        }
    }
}
