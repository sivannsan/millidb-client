package com.sivannsan.millidb;

import com.sivannsan.foundation.annotation.Nonnull;
import com.sivannsan.foundation.Validate;
import com.sivannsan.millidata.MilliData;
import com.sivannsan.millidata.MilliMap;
import com.sivannsan.millidata.MilliValue;

public abstract class MilliDBFilter {
    /**
     * <p>Used to check</p>
     * <p>1. If the searched files are documents</p>
     * <p>2. If the content of the searched documents are super-data of the inputted subMilliData</p>
     * <p/>
     * <p>Notice that</p>
     * <p>* MilliMap can be super of another MilliMap</p>
     * <p>* MilliList can be super of another MilliList</p>
     * <p>* MilliValue can NOT be super of another MilliValue</p>
     */
    public static SuperOf superOf(@Nonnull MilliData subMilliData, int level) {
        return new SuperOf(subMilliData, level);
    }

    /**
     * <p>Used to check</p>
     * <p>1. If the searched files are documents</p>
     * <p>2. If the content of the searched documents are super-data of the inputted subMilliData</p>
     * <p/>
     * <p>Notice that</p>
     * <p>* MilliMap can be super of another MilliMap</p>
     * <p>* MilliList can be super of another MilliList</p>
     * <p>* MilliValue can NOT be super of another MilliValue</p>
     */
    public static SuperOf superOf(@Nonnull MilliData subMilliData) {
        return superOf(subMilliData, 0);
    }

    @Nonnull
    public abstract String getType();

    @Nonnull
    public abstract MilliMap toMilliMap();

    public static final class Parser {
        public static MilliDBFilter parse(@Nonnull String filter) {
            return parse(MilliData.Parser.parse(filter, new MilliMap()).asMilliMap(new MilliMap()));
        }

        public static MilliDBFilter parse(@Nonnull MilliMap filter) {
            if (!filter.get("_t").isMilliValue()) return null;
            switch (filter.get("_t").asMilliValue().asString()) {
                case "so":
                    return superOf(filter.get("s"), filter.get("l").asMilliValue(new MilliValue(0)).asInteger32());
                default:
                    return null;
            }
        }
    }

    public static final class SuperOf extends MilliDBFilter {
        private final MilliData subMilliData;
        private final int level;

        private SuperOf(@Nonnull MilliData subMilliData, int level) {
            this.subMilliData = Validate.nonnull(subMilliData);
            this.level = level;
        }

        @Override
        @Nonnull
        public String getType() {
            return "so";
        }

        @Nonnull
        public MilliData getSubMilliData() {
            return subMilliData;
        }

        public int getLevel() {
            return level;
        }

        @Override
        @Nonnull
        public MilliMap toMilliMap() {
            return new MilliMap("_t", new MilliValue(getType())).append("s", subMilliData).append("l", new MilliValue(level));
        }
    }
}
