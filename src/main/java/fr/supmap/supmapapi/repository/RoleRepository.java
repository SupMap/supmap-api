package fr.supmap.supmapapi.repository;

import fr.supmap.supmapapi.model.entity.table.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}