package com.workpilot.dto.GestionRessources;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalRequest {
    private boolean approved;
    private String reason;

    // Optionnel, mais utile si tu ne veux pas dépendre uniquement de Lombok
    public boolean isApproved() {
        return approved;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}