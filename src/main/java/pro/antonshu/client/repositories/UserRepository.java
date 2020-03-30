package pro.antonshu.client.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.antonshu.client.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findOneByLogin(String login);

    boolean existsByLogin(String login);
}
