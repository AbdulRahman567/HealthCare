/**
 * Hospital Administration — organizational structure and staff (Phases 4.1–4.4).
 *
 * <p>Owns employment profiles, departments, and staff ↔ department assignment.
 *
 * <h2>Scope</h2>
 * <ul>
 *   <li>Phase 4.1 — {@link com.healthcare.hms.organization.entity.Staff}, employment enums</li>
 *   <li>Phase 4.2 — {@link com.healthcare.hms.organization.entity.Department} CRUD API</li>
 *   <li>Phase 4.3 — Doctor / Nurse / Receptionist / LaboratoryStaff / Pharmacist CRUD</li>
 *   <li>Phase 4.4 — Staff department assignment, transfer, department head, assignment history</li>
 * </ul>
 *
 * <h2>Isolation</h2>
 * All organization entities extend {@link com.healthcare.hms.common.persistence.TenantOwnedEntity}
 * and inherit Hibernate {@code tenantFilter} enforcement from
 * {@link com.healthcare.hms.tenant}.
 */
package com.healthcare.hms.organization;
