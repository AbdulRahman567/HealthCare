package com.healthcare.hms.organization.staff;

import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.organization.entity.Staff;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.enums.EmploymentType;
import com.healthcare.hms.organization.service.DepartmentQueryService;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.service.UserQueryService;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Shared staff-administration rules (tenant, user role, department, uniqueness, soft-delete).
 */
@Component
public class StaffAdministrationSupport {

    public static final int MAX_PAGE_SIZE = 100;
    public static final Set<String> DEFAULT_SORT_PROPERTIES = Set.of(
            "employeeCode",
            "jobTitle",
            "employmentStatus",
            "employmentType",
            "hiredAt",
            "createdAt",
            "updatedAt"
    );

    private final UserQueryService userQueryService;
    private final DepartmentQueryService departmentQueryService;

    public StaffAdministrationSupport(
            final UserQueryService userQueryService,
            final DepartmentQueryService departmentQueryService
    ) {
        this.userQueryService = userQueryService;
        this.departmentQueryService = departmentQueryService;
    }

    public void assertUserAndDepartment(
            final UUID tenantId,
            final UUID userId,
            final RoleType expectedRole,
            final UUID departmentId
    ) {
        userQueryService.requireTenantUserWithRole(tenantId, userId, expectedRole);
        departmentQueryService.assertBelongsToTenant(departmentId, tenantId);
    }

    public void assertUniqueEmployeeCode(
            final UUID tenantId,
            final String employeeCode,
            final UUID excludeId,
            final BiPredicate<UUID, String> existsByCode,
            final TriPredicate<UUID, String, UUID> existsByCodeExcluding
    ) {
        final String normalized = normalizeEmployeeCode(employeeCode);
        final boolean exists = excludeId == null
                ? existsByCode.test(tenantId, normalized)
                : existsByCodeExcluding.test(tenantId, normalized, excludeId);
        if (exists) {
            throw new ConflictException("STAFF_EMPLOYEE_CODE_EXISTS", "Employee code is already in use for this tenant");
        }
    }

    public void assertUniqueUserLink(
            final UUID tenantId,
            final UUID userId,
            final UUID excludeId,
            final BiPredicate<UUID, UUID> existsByUser,
            final TriPredicate<UUID, UUID, UUID> existsByUserExcluding
    ) {
        final boolean exists = excludeId == null
                ? existsByUser.test(tenantId, userId)
                : existsByUserExcluding.test(tenantId, userId, excludeId);
        if (exists) {
            throw new ConflictException("STAFF_USER_ALREADY_LINKED", "User already has this staff profile in the tenant");
        }
    }

    public void assertUserNotLinkedElsewhere(final UUID userId, final Predicate<UUID> existsAnywhere) {
        if (existsAnywhere.test(userId)) {
            throw new ConflictException(
                    "STAFF_USER_ALREADY_EMPLOYED",
                    "User already has another staff employment profile in this tenant"
            );
        }
    }

    public void applyEmployment(
            final Staff staff,
            final UUID hospitalId,
            final UUID userId,
            final UUID departmentId,
            final String employeeCode,
            final String jobTitle,
            final EmploymentStatus employmentStatus,
            final EmploymentType employmentType,
            final LocalDate hiredAt,
            final LocalDate terminatedAt,
            final UUID reportsToStaffId,
            final boolean create
    ) {
        if (create) {
            staff.setHospitalId(hospitalId);
            staff.setUserId(userId);
        }
        staff.setDepartmentId(departmentId);
        staff.setEmployeeCode(normalizeEmployeeCode(employeeCode));
        staff.setJobTitle(trimToNull(jobTitle));
        staff.setEmploymentStatus(employmentStatus);
        staff.setEmploymentType(employmentType);
        staff.setHiredAt(hiredAt);
        staff.setTerminatedAt(terminatedAt);
        staff.setReportsToStaffId(reportsToStaffId);
    }

    public void markSoftDeleted(final Staff staff, final UUID actorId) {
        staff.setEmployeeCode(releaseUniqueValue(staff.getEmployeeCode(), staff.getId(), 50));
        staff.setDepartmentId(null);
        staff.setReportsToStaffId(null);
        staff.setEmploymentStatus(EmploymentStatus.TERMINATED);
        staff.markDeleted(actorId);
    }

    /**
     * Soft-delete unique marker that never truncates back to the live value.
     */
    public static String releaseUniqueValue(final String original, final UUID id, final int maxLength) {
        final String compactId = id.toString().replace("-", "");
        final String suffix = "__DEL__" + compactId.substring(0, 8);
        if (original.length() + suffix.length() <= maxLength) {
            return original + suffix;
        }
        final String replacement = "DEL-" + compactId;
        return replacement.length() <= maxLength ? replacement : replacement.substring(0, maxLength);
    }

    public Pageable sanitizePageable(final Pageable pageable, final Set<String> allowedSortProperties) {
        final int page = Math.max(pageable.getPageNumber(), 0);
        final int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);

        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "employeeCode"));
        }

        final Sort safeSort = Sort.by(pageable.getSort().stream()
                .filter(order -> allowedSortProperties.contains(order.getProperty()))
                .map(order -> new Sort.Order(order.getDirection(), order.getProperty()))
                .toList());

        if (safeSort.isUnsorted()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "employeeCode"));
        }
        return PageRequest.of(page, size, safeSort);
    }

    public static String normalizeEmployeeCode(final String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    public static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String truncate(final String value, final int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    @FunctionalInterface
    public interface TriPredicate<A, B, C> {
        boolean test(A a, B b, C c);
    }
}
