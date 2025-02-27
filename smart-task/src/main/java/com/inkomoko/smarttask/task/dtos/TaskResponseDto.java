package com.inkomoko.smarttask.task.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponseDto {
    private String title;
    private String description;
    private String dueDate;
    private String taskStatus;
    private String taskPriority;

}
