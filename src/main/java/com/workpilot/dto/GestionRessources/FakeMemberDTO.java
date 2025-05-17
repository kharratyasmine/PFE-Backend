package com.workpilot.dto.GestionRessources;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class FakeMemberDTO {
    private String name;
    private String role;
    private String initial;
    private String note;

    // ✅ Constructeur explicite pour l'appel dans le mapping
    public FakeMemberDTO(String name, String role, String initial, String note) {
        this.name = name;
        this.role = role;
        this.initial = initial;
        this.note = note;
    }
}
