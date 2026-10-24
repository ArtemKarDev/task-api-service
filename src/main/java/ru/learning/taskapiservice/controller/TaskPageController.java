package ru.learning.taskapiservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.learning.taskapiservice.dto.Priority;
import ru.learning.taskapiservice.dto.TaskForm;
import ru.learning.taskapiservice.dto.TaskStatus;
import ru.learning.taskapiservice.entity.Task;
import ru.learning.taskapiservice.service.TaskService;

@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskPageController {

    private final TaskService taskService;

    @GetMapping
    public String getAllTasks(Model model) {
        model.addAttribute("tasks", taskService.getAllTasks());
        return "tasks";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("taskForm", new TaskForm());
        model.addAttribute("priority", Priority.values());
        model.addAttribute("status", TaskStatus.values());

        return "task-form";
    }

    @PostMapping
    public String createTask(
            @Valid @ModelAttribute("taskForm") TaskForm taskForm,
            BindingResult bindingResult,
            Model model
    ) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("priority", Priority.values());
            model.addAttribute("status", TaskStatus.values());
            return "task-form";
        }

        Task task = new Task();
        task.setTitle(taskForm.getTitle());
        task.setDescription(taskForm.getDescription());
        task.setAssignee(taskForm.getAssignee());
        task.setPriority(taskForm.getPriority());
        task.setStatus(taskForm.getStatus());

        taskService.createTask(task);

        return "redirect:/tasks";
    }
}