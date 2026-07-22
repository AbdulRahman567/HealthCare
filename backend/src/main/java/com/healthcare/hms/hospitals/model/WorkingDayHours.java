package com.healthcare.hms.hospitals.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

/**
 * Open/close window for a single weekday. When {@code closed} is true, open/close are ignored.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class WorkingDayHours {

    private boolean closed;
    private String open;
    private String close;

    public WorkingDayHours() {
    }

    public WorkingDayHours(final boolean closed, final String open, final String close) {
        this.closed = closed;
        this.open = open;
        this.close = close;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(final boolean closed) {
        this.closed = closed;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(final String open) {
        this.open = open;
    }

    public String getClose() {
        return close;
    }

    public void setClose(final String close) {
        this.close = close;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WorkingDayHours that)) {
            return false;
        }
        return closed == that.closed
                && Objects.equals(open, that.open)
                && Objects.equals(close, that.close);
    }

    @Override
    public int hashCode() {
        return Objects.hash(closed, open, close);
    }
}
