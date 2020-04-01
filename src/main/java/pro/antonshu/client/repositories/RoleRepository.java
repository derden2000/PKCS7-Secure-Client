package pro.antonshu.client.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.antonshu.client.entities.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findOneByTitle(String title);
}
