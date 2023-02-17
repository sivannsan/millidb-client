package com.sivannsan.millidb;

import com.sivannsan.foundation.annotation.Nonnull;

public class MilliDBResultParsedException extends RuntimeException {
    public MilliDBResultParsedException() {
        super();
    }

    public MilliDBResultParsedException(@Nonnull String message) {
        super(message);
    }
}
