package com.inkomoko.smarttask.task.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequestDto {
    private Long id;
    private String title;
    private String description;
    private String dueDate;
    private String taskStatus;
    private String taskPriority;
    private Integer status;
    private String createdAt;
    private String updatedAt;
}
