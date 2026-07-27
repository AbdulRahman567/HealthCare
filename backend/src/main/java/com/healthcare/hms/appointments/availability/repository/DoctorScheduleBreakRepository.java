package com.healthcare.hms.appointments.availability.repository;

import com.healthcare.hms.appointments.availability.entity.DoctorScheduleBreak;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorScheduleBreakRepository extends JpaRepository<DoctorScheduleBreak, UUID> {

    List<DoctorScheduleBreak> findByTenantIdAndScheduleIdOrderByDayOfWeekAscStartTimeAsc(
            UUID tenantId, UUID scheduleId);
}
