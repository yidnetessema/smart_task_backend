package com.inkomoko.smarttask.task.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inkomoko.smarttask.task.dtos.TaskRequestDto;
import com.inkomoko.smarttask.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
public class TaskController {


    private final TaskService taskService;


    @Operation(summary = "Api for creating task")
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> createTask(@RequestBody TaskRequestDto taskDto) throws JsonProcessingException {

        return ResponseEntity.ok(taskService.createTask(taskDto));
    }

    @Operation(summary = "Api for getting all tasks")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTasks(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "amount", required = false, defaultValue = "10") int amount,
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            @RequestParam(name = "taskPriority", required = false, defaultValue = "ALL") String taskPriority,
            @RequestParam(name = "taskStatus", required = false, defaultValue = "ALL") String taskStatus

    ) {
        return ResponseEntity.ok(taskService.getAllTasks(offset,amount,q, taskPriority,taskStatus));
    }


    @Operation(summary = "Api to update task by id")
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateTask(@RequestBody TaskRequestDto taskDto) {
        return ResponseEntity.ok(taskService.updateTask(taskDto));
    }

    @Operation(summary = "Api to delete task by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.deleteTask(id));
    }

}
