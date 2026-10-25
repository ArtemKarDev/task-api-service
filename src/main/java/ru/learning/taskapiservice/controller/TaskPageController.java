package ru.learning.taskapiservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
    public String getAllTasks(@RequestParam(required = false) String q,
                              @RequestParam(required = false) TaskStatus status,
                              Model model) {
        model.addAttribute("tasks", taskService.searchTasks(q, status));
        model.addAttribute("status", TaskStatus.values());
        return "tasks";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("taskForm", new TaskForm());
        addEnums(model);
        return "task-form";
    }

    @PostMapping
    public String createTask(@Valid @ModelAttribute("taskForm") TaskForm taskForm,
                             BindingResult bindingResult, Model model,
                             RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            addEnums(model);
            return "task-form";
        }
        taskService.createTask(toTask(taskForm));
        ra.addFlashAttribute("message", "Задача создана");
        return "redirect:/tasks";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("taskForm", TaskForm.from(taskService.getTaskById(id)));
        model.addAttribute("taskId", id);
        addEnums(model);
        return "task-edit";
    }

    @PostMapping("/{id}")
    public String updateTask(@PathVariable Long id,
                             @Valid @ModelAttribute("taskForm") TaskForm taskForm,
                             BindingResult bindingResult, Model model,
                             RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("taskId", id);
            addEnums(model);
            return "task-edit";
        }
        taskService.updateTask(id, toTask(taskForm));
        ra.addFlashAttribute("message", "Изменения сохранены");
        return "redirect:/tasks";
    }

    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id, RedirectAttributes ra) {
        taskService.deleteTask(id);
        ra.addFlashAttribute("message", "Задача удалена");
        return "redirect:/tasks";
    }

    private void addEnums(Model model) {
        model.addAttribute("priority", Priority.values());
        model.addAttribute("status", TaskStatus.values());
    }

    private Task toTask(TaskForm f) {
        Task task = new Task();
        task.setTitle(f.getTitle());
        task.setDescription(f.getDescription());
        task.setAssignee(f.getAssignee());
        task.setPriority(f.getPriority());
        task.setStatus(f.getStatus());
        return task;
    }
}