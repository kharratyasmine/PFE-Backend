package com.workpilot.controller.GestionRessources;

import com.workpilot.dto.GestionRessources.TeamMemberDTO;
import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.repository.ressources.TeamMemberRepository;
import com.workpilot.service.GestionRessources.teamMember.TeamMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teamMembers")
@CrossOrigin(origins = "http://localhost:4200")
public class TeamMemberController {

    @Autowired
    private TeamMemberService teamMemberService;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @GetMapping
    public List<TeamMemberDTO> getAllTeamMembers() {
        return teamMemberService.getAllTeamMembers();
    }

    @GetMapping("/{id}")
    public TeamMemberDTO getTeamMemberById(@PathVariable Long id) {
        return teamMemberService.getTeamMemberById(id);
    }

    @PostMapping
    public TeamMemberDTO createTeamMember(@RequestBody TeamMemberDTO teamMemberDTO) {
        return teamMemberService.createTeamMember(teamMemberDTO);
    }

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<?> uploadTeamMemberImage(@PathVariable Long id,
                                                   @RequestParam("image") MultipartFile file) {
        try {
            TeamMember member = teamMemberRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Membre non trouvé"));

            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (ext.equals(".jfif")) ext = ".jpg";

            if (!ext.matches("\\.(jpg|jpeg|png|gif|bmp|webp)$")) {
                throw new RuntimeException("Type de fichier non supporté : " + ext);
            }

            String fileName = "member_" + id + "_" + System.currentTimeMillis() + ext;
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", fileName);
            Files.createDirectories(uploadPath.getParent());
            Files.copy(file.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);

            member.setImage("/uploads/" + fileName);
            teamMemberRepository.save(member);

            return ResponseEntity.ok().body(Map.of("imagePath", "/uploads/" + fileName));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur upload image : " + e.getMessage());
        }
    }



    @GetMapping("/project/{projectId}/members")
    public List<TeamMemberDTO> getMembersByProject(@PathVariable Long projectId) {
        return teamMemberService.getMembersByProjectId(projectId);
    }

    // TeamMemberController.java
    @GetMapping("/team/{teamId}")
    public List<TeamMemberDTO> getMembersByTeam(@PathVariable Long teamId) {
        return teamMemberService.getTeamMembersByTeamId(teamId);
    }

    @GetMapping("/team/{teamId}/members")
    public List<TeamMemberDTO> getTeamMembersByTeam(@PathVariable Long teamId) {
        return teamMemberService.getTeamMembersByTeamId(teamId);
    }


    @PutMapping("/{id}")
    public TeamMemberDTO updateTeamMember(@PathVariable Long id, @RequestBody TeamMemberDTO teamMemberDTO) {
        return teamMemberService.updateTeamMember(id, teamMemberDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteTeamMember(@PathVariable Long id) {
        teamMemberService.deleteTeamMember(id);
    }




}