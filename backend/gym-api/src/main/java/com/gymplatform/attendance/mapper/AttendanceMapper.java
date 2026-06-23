package com.gymplatform.attendance.mapper;

import com.gymplatform.attendance.domain.Attendance;
import com.gymplatform.attendance.dto.AttendanceResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    AttendanceResponse toResponse(Attendance attendance);
}
