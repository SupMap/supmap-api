package fr.supmap.supmapapi.repository;

import fr.supmap.supmapapi.model.entity.table.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByName(String name);
}