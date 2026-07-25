package com.healthcare.hms.organization.enums;

/**
 * Kind of department affiliation change recorded in assignment history.
 */
public enum AssignmentAction {
    /** First affiliation to a department (staff had none). */
    ASSIGN,
    /** Move from one department to another. */
    TRANSFER,
    /** Clear department affiliation. */
    UNASSIGN
}
