package com.workpilot.dto;

import com.workpilot.entity.auth.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String phoneNumber;
    private String address;
    private String photoUrl;
    private Role role; // ou Enum si tu préfères
}
