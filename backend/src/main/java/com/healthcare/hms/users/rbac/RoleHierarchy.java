package com.healthcare.hms.users.rbac;

import com.healthcare.hms.users.enums.RoleType;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Structural role hierarchy for HMS RBAC.
 *
 * <p>Lower {@code hierarchyLevel} means higher privilege. Hierarchy is used for
 * role ordering, parent linkage, and assignment eligibility — not for runtime
 * permission evaluation. Effective permissions remain the explicit
 * {@code role_permissions} grants from {@link SystemRolePermissionMatrix}.
 *
 * <pre>
 * SUPER_ADMIN (0)
 *   └── HOSPITAL_ADMIN (10)
 *         ├── DOCTOR (20)
 *         ├── NURSE (30)
 *         ├── RECEPTIONIST (30)
 *         ├── LAB_TECHNICIAN (30)
 *         ├── PHARMACIST (30)
 *         ├── ACCOUNTANT (30)
 *         └── PATIENT (40)
 * </pre>
 */
public final class RoleHierarchy {

    public static final int SUPER_ADMIN = 0;
    public static final int HOSPITAL_ADMIN = 10;
    public static final int DOCTOR = 20;
    public static final int OPERATIONAL_STAFF = 30;
    public static final int PATIENT = 40;

    private static final Map<RoleType, Integer> LEVELS = new EnumMap<>(RoleType.class);
    private static final Map<RoleType, RoleType> PARENTS = new EnumMap<>(RoleType.class);

    static {
        LEVELS.put(RoleType.SUPER_ADMIN, SUPER_ADMIN);
        LEVELS.put(RoleType.HOSPITAL_ADMIN, HOSPITAL_ADMIN);
        LEVELS.put(RoleType.DOCTOR, DOCTOR);
        LEVELS.put(RoleType.NURSE, OPERATIONAL_STAFF);
        LEVELS.put(RoleType.RECEPTIONIST, OPERATIONAL_STAFF);
        LEVELS.put(RoleType.LAB_TECHNICIAN, OPERATIONAL_STAFF);
        LEVELS.put(RoleType.PHARMACIST, OPERATIONAL_STAFF);
        LEVELS.put(RoleType.ACCOUNTANT, OPERATIONAL_STAFF);
        LEVELS.put(RoleType.PATIENT, PATIENT);

        PARENTS.put(RoleType.HOSPITAL_ADMIN, RoleType.SUPER_ADMIN);
        PARENTS.put(RoleType.DOCTOR, RoleType.HOSPITAL_ADMIN);
        PARENTS.put(RoleType.NURSE, RoleType.HOSPITAL_ADMIN);
        PARENTS.put(RoleType.RECEPTIONIST, RoleType.HOSPITAL_ADMIN);
        PARENTS.put(RoleType.LAB_TECHNICIAN, RoleType.HOSPITAL_ADMIN);
        PARENTS.put(RoleType.PHARMACIST, RoleType.HOSPITAL_ADMIN);
        PARENTS.put(RoleType.ACCOUNTANT, RoleType.HOSPITAL_ADMIN);
        PARENTS.put(RoleType.PATIENT, RoleType.HOSPITAL_ADMIN);
    }

    private RoleHierarchy() {
    }

    public static int levelOf(final RoleType type) {
        Objects.requireNonNull(type, "type");
        final Integer level = LEVELS.get(type);
        if (level == null) {
            throw new IllegalArgumentException("Unknown role type in hierarchy: " + type);
        }
        return level;
    }

    public static Optional<RoleType> parentOf(final RoleType type) {
        Objects.requireNonNull(type, "type");
        return Optional.ofNullable(PARENTS.get(type));
    }

    public static boolean isTenantRoot(final RoleType type) {
        return type == RoleType.HOSPITAL_ADMIN;
    }

    public static boolean isPlatformOnly(final RoleType type) {
        return type == RoleType.SUPER_ADMIN;
    }

    public static boolean isAssignable(final RoleType type) {
        return type != RoleType.SUPER_ADMIN && type != RoleType.PATIENT;
    }

    public static boolean isStrictlyHigher(final RoleType higher, final RoleType lower) {
        return levelOf(higher) < levelOf(lower);
    }
}
