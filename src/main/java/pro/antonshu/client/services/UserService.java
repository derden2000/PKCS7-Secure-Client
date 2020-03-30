package pro.antonshu.client.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import pro.antonshu.client.entities.User;
import pro.antonshu.client.repositories.UserRepository;

import java.util.List;

@Service
public interface UserService extends UserDetailsService {

    User findByLogin(String login);

    User findById(Long id);

    boolean isUserExist(String login);

    List<User> getAllUsers();

    User regNewUser(User user);

    User save(User user);

    void createPasswordResetTokenForUser(User user, String token);

    void changeUserPassword(User user, String newPassword);
}
