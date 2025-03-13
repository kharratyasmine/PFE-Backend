package com.workpilot.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jira/webhook")
public class JiraWebhookController {

/*    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectService projectService;

    @PostMapping
    public void handleJiraWebhook(@RequestBody Map<String, Object> payload) {
        System.out.println("🔔 Webhook reçu de JIRA : " + payload);

        // Extraire les données de la tâche depuis le webhook
        Map<String, Object> issue = (Map<String, Object>) payload.get("issue");
        String taskTitle = (String) ((Map<String, Object>) issue.get("fields")).get("summary");
        String taskDescription = (String) ((Map<String, Object>) issue.get("fields")).get("description");
        String status = (String) ((Map<String, Object>) ((Map<String, Object>) issue.get("fields")).get("status")).get("name");
        String projectName = (String) ((Map<String, Object>) issue.get("fields")).get("project");

        System.out.println("📌 Tâche détectée : " + taskTitle);

        // Vérifier si le projet existe, sinon le créer
        Project project = projectService.getProjectByName(projectName);
        if (project == null) {
            project = new Project(projectName, "Projet JIRA", "EN_COURS", new java.util.Date(), new java.util.Date());
            projectService.createProject(project);
        }

        // Créer et sauvegarder la tâche en base de données
        Task task = new Task();
        task.setTitle(taskTitle);
        task.setDescription(taskDescription);
        task.setStatus(status);
        task.setProject(project);

        taskService.createTache(task);
        System.out.println("✅ Tâche ajoutée à WorkPilot !");
    }*/
}
