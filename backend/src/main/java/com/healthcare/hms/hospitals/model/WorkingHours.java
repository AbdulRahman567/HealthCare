package com.healthcare.hms.hospitals.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

/**
 * Weekly working-hours schedule stored as JSON on the hospital settings row.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class WorkingHours {

    private WorkingDayHours monday;
    private WorkingDayHours tuesday;
    private WorkingDayHours wednesday;
    private WorkingDayHours thursday;
    private WorkingDayHours friday;
    private WorkingDayHours saturday;
    private WorkingDayHours sunday;

    public WorkingHours() {
    }

    public WorkingHours(
            final WorkingDayHours monday,
            final WorkingDayHours tuesday,
            final WorkingDayHours wednesday,
            final WorkingDayHours thursday,
            final WorkingDayHours friday,
            final WorkingDayHours saturday,
            final WorkingDayHours sunday
    ) {
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
    }

    public WorkingDayHours getMonday() {
        return monday;
    }

    public void setMonday(final WorkingDayHours monday) {
        this.monday = monday;
    }

    public WorkingDayHours getTuesday() {
        return tuesday;
    }

    public void setTuesday(final WorkingDayHours tuesday) {
        this.tuesday = tuesday;
    }

    public WorkingDayHours getWednesday() {
        return wednesday;
    }

    public void setWednesday(final WorkingDayHours wednesday) {
        this.wednesday = wednesday;
    }

    public WorkingDayHours getThursday() {
        return thursday;
    }

    public void setThursday(final WorkingDayHours thursday) {
        this.thursday = thursday;
    }

    public WorkingDayHours getFriday() {
        return friday;
    }

    public void setFriday(final WorkingDayHours friday) {
        this.friday = friday;
    }

    public WorkingDayHours getSaturday() {
        return saturday;
    }

    public void setSaturday(final WorkingDayHours saturday) {
        this.saturday = saturday;
    }

    public WorkingDayHours getSunday() {
        return sunday;
    }

    public void setSunday(final WorkingDayHours sunday) {
        this.sunday = sunday;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WorkingHours that)) {
            return false;
        }
        return Objects.equals(monday, that.monday)
                && Objects.equals(tuesday, that.tuesday)
                && Objects.equals(wednesday, that.wednesday)
                && Objects.equals(thursday, that.thursday)
                && Objects.equals(friday, that.friday)
                && Objects.equals(saturday, that.saturday)
                && Objects.equals(sunday, that.sunday);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monday, tuesday, wednesday, thursday, friday, saturday, sunday);
    }
}
