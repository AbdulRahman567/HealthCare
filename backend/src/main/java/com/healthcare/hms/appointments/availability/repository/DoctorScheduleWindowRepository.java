package com.healthcare.hms.appointments.availability.repository;

import com.healthcare.hms.appointments.availability.entity.DoctorScheduleWindow;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorScheduleWindowRepository extends JpaRepository<DoctorScheduleWindow, UUID> {

    List<DoctorScheduleWindow> findByTenantIdAndScheduleIdOrderByDayOfWeekAscStartTimeAsc(
            UUID tenantId, UUID scheduleId);
}
