package com.workpilot.entity.ressources;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "client")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Size(max = 100, message = "Company name cannot exceed 100 characters")
    @Column(length = 100)
    private String company;

    @ElementCollection
    @CollectionTable(name = "client_sales_manager", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "sales_manager")
    private List<String> salesManagers = new ArrayList<>();


    @Size(min = 1 ,max = 9, message = "Contact cannot exceed 50 characters")
    private String contact;

    @Size(max = 255, message = "Adress cannot exceed 225 characters")
    private String address;


    @Email(message = "E-mail must be valid")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

   @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("client")
    private List<Project> projects = new ArrayList<>();

}
