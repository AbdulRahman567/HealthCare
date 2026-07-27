package com.healthcare.hms.appointments.queue.service;

import com.healthcare.hms.appointments.queue.dto.request.CheckInQueueRequest;
import com.healthcare.hms.appointments.queue.dto.request.QueueStatusUpdateRequest;
import com.healthcare.hms.appointments.queue.dto.response.DoctorDayQueueResponse;
import com.healthcare.hms.appointments.queue.dto.response.QueueEntryResponse;
import java.time.LocalDate;
import java.util.UUID;

public interface QueueService {

    QueueEntryResponse checkIn(CheckInQueueRequest request, String ipAddress, String userAgent);

    DoctorDayQueueResponse getDailyQueue(UUID doctorId, LocalDate date);

    DoctorDayQueueResponse getQueueById(UUID queueId);

    QueueEntryResponse getEntryById(UUID entryId);

    QueueEntryResponse markWaiting(UUID entryId, QueueStatusUpdateRequest request, String ipAddress, String userAgent);

    QueueEntryResponse startConsultation(
            UUID entryId, QueueStatusUpdateRequest request, String ipAddress, String userAgent);

    QueueEntryResponse complete(UUID entryId, QueueStatusUpdateRequest request, String ipAddress, String userAgent);

    QueueEntryResponse markMissed(UUID entryId, QueueStatusUpdateRequest request, String ipAddress, String userAgent);

    QueueEntryResponse cancel(UUID entryId, QueueStatusUpdateRequest request, String ipAddress, String userAgent);
}
