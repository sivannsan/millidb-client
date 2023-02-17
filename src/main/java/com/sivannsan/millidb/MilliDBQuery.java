package com.sivannsan.millidb;

import com.sivannsan.foundation.annotation.Nonnull;
import com.sivannsan.foundation.Validate;
import com.sivannsan.millidata.MilliData;
import com.sivannsan.millidata.MilliMap;
import com.sivannsan.millidata.MilliNull;
import com.sivannsan.millidata.MilliValue;

public final class MilliDBQuery {
    private final long id;
    @Nonnull
    private final String path;
    @Nonnull
    private final Function function;
    @Nonnull
    private final MilliData metadata;

    public MilliDBQuery(long id, @Nonnull String path, @Nonnull Function function, @Nonnull MilliData metadata) {
        this.id = id;
        this.path = path;
        this.function = Validate.nonnull(function);
        this.metadata = Validate.nonnull(metadata);
    }

    public MilliDBQuery(@Nonnull String path, @Nonnull Function function, @Nonnull MilliData metadata) {
        this(IDGenerator.generateNewID(), path, function, metadata);
    }

    public long getID() {
        return id;
    }

    @Nonnull
    public String getPath() {
        return path;
    }

    @Nonnull
    public Function getFunction() {
        return function;
    }

    @Nonnull
    public MilliData getMetadata() {
        return metadata;
    }

    @Nonnull
    public MilliMap asMilliMap() {
        return new MilliMap()
                .append("id", new MilliValue(id))
                .append("p", new MilliValue(path))
                .append("f", new MilliValue(function.toString()))
                .append("m", metadata);
    }

    @Nonnull
    public static MilliDBQuery invalid() {
        return new MilliDBQuery(-1, "", Function.NONE, MilliNull.INSTANCE);
    }

    public static final class Parser {
        @Nonnull
        public static MilliDBQuery parse(@Nonnull String query, @Nonnull MilliDBQuery defaultValue) {
            try {
                return parse(query);
            } catch (MilliDBQueryParsedException e) {
                return defaultValue;
            }
        }

        @Nonnull
        public static MilliDBQuery parse(@Nonnull String query) throws MilliDBQueryParsedException {
            MilliMap map = MilliData.Parser.parse(Validate.nonnull(query), new MilliMap()).asMilliMap(new MilliMap());
            long parsedID = map.get("id").asMilliValue(new MilliValue(-1)).asInteger64();
            if (parsedID < 0) throw new MilliDBQueryParsedException("Invalid ID");
            String parsedPath = map.get("p").asMilliValue(new MilliValue()).asString();
            Function parsedFunction = Function.fromString(map.get("f").asMilliValue(new MilliValue()).asString());
            if (parsedFunction == null) throw new MilliDBQueryParsedException("Invalid Function");
            MilliData parsedMetadata = map.get("m");
            return new MilliDBQuery(parsedID, parsedPath, parsedFunction, parsedMetadata);
        }
    }

    private static final class IDGenerator {
        private static long LAST_ID = -1;

        public static long generateNewID() {
            return ++LAST_ID;
        }
    }

    public enum Function {
        NONE,
        HAS_USER,
        /**
         * Uses query metadata as the filter
         * Returns result metadata as the list of files
         */
        GET_FILES,
        GET_COLLECTION,
        GET_DOCUMENT,
        GET,
        SET,
        DELETE,
        CLOSE;

        @Override
        public String toString() {
            switch (this) {
                case HAS_USER: return "hu";
                case GET_FILES: return "gfs";
                case GET_COLLECTION: return "gcll";
                case GET_DOCUMENT: return "gdoc";
                case GET: return "g";
                case SET: return "s";
                case DELETE: return "d";
                case CLOSE: return "c";
                default: return "n";
            }
        }

        public static Function fromString(@Nonnull String string) {
            Validate.nonnull(string);
            for (Function f : values()) if (f.toString().equalsIgnoreCase(string)) return f;
            return null;
        }

        @Nonnull
        public static Function fromString(@Nonnull String string, @Nonnull Function defaultValue) {
            Function f = fromString(string);
            return f == null ? Validate.nonnull(defaultValue) : f;
        }
    }
}
