package com.inkomoko.smarttask.task.service;

import com.inkomoko.smarttask.task.dtos.TaskRequestDto;
import com.inkomoko.smarttask.task.enums.TaskPriority;
import com.inkomoko.smarttask.task.enums.TaskStatus;
import com.inkomoko.smarttask.task.model.Task;
import com.inkomoko.smarttask.task.repository.TaskRepository;
import com.inkomoko.smarttask.user.models.User;
import com.inkomoko.smarttask.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    // Create Task
    public Map<String, Object> createTask(TaskRequestDto taskDto) {

        Map<String, Object> response = new LinkedHashMap<>();
        Optional<User> makerUser = userRepository.findByEmail(((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());

        Task task = new Task();
        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setDueDate(LocalDate.parse(taskDto.getDueDate()));
        task.setTaskStatus(TaskStatus.valueOf("TODO"));
        task.setPriority(TaskPriority.valueOf(taskDto.getTaskPriority()));
        task.setCreatedAt(LocalDateTime.now());
        task.setCreatedBy(makerUser.get());
        Task savedTask = taskRepository.save(task);

        if (savedTask.getId() != null) {
            response.put("status","SUCCESS");
            response.put("message", "Task created successfully");
        }else{
            response.put("status","FAILURE");
            response.put("message", "Task creation failed");
        }
        return response;
    }

    // Get All Tasks
    public Map<String, Object> getAllTasks(int offset, int amount, String q, String taskPriority,String taskStatus) {

        Map<String, Object> response = new LinkedHashMap<>();

        Page<Task> tasks = taskRepository.findAll((root, query, criteriaBuilder) -> {

            String s = String.format("%%%s%%",q).toLowerCase();

            return criteriaBuilder.and(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),s),
                    (taskPriority.equals("ALL")) ? criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE)) : criteriaBuilder.equal(root.get("priority"),TaskPriority.valueOf(taskPriority)),
                    (taskStatus.equals("ALL")) ? criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE)) : criteriaBuilder.equal(root.get("taskStatus"),TaskStatus.valueOf(taskStatus))
            );
        }, PageRequest.of(offset, amount, Sort.by("id").descending()));

        response.put("status","SUCCESS");
        response.put("message","Tasks found");
        response.put("tasks", tasks.getContent().stream().map((m) -> modelMapper.map(m, TaskRequestDto.class)));
        response.put("isLast", tasks.isLast());
        response.put("totalElements", tasks.getTotalElements());


        return response;

    }

    // Update Task
    public Map<String,Object> updateTask(TaskRequestDto taskDto) {

        Map<String, Object> response = new LinkedHashMap<>();

        Optional<Task> task = taskRepository.findById(taskDto.getId());

        if(task.isPresent()) {
            task.get().setTitle(taskDto.getTitle());
            task.get().setDescription(taskDto.getDescription());
            task.get().setDueDate(LocalDate.parse(taskDto.getDueDate()));
            task.get().setTaskStatus(TaskStatus.valueOf(taskDto.getTaskStatus()));
            task.get().setPriority(TaskPriority.valueOf(taskDto.getTaskPriority()));

            Task updatedTask = taskRepository.save(task.get());
            if(updatedTask.getId() != 0) {
                response.put("status","SUCCESS");
                response.put("message","Task updated successfully");
            }else{
                response.put("status","FAILURE");
                response.put("message", "Something went wrong!");
            }
        }else{
            response.put("status","FAILURE");
            response.put("message", "Task not found");
        }

       return response;
    }

    // Delete Task
    public Map<String,Object> deleteTask(Long id) {

        Map<String, Object> response = new LinkedHashMap<>();
        taskRepository.deleteById(id);

        response.put("status","SUCCESS");
        response.put("message","Task deleted successfully");

        return response;
    }

}
