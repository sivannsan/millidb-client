package com.sivannsan.millidb;

import com.sivannsan.foundation.annotation.Nonnull;

public class MilliDBResultFailedException extends RuntimeException {
    public MilliDBResultFailedException() {
        super();
    }

    public MilliDBResultFailedException(@Nonnull String message) {
        super(message);
    }
}
