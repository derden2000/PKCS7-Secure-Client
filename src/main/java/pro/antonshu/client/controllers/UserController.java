package pro.antonshu.client.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pro.antonshu.client.entities.User;
import pro.antonshu.client.repositories.RoleRepository;
import pro.antonshu.client.services.UserService;

import javax.validation.Valid;
import java.util.Collections;

@Controller
public class UserController {

    private UserService userService;
    private RoleRepository roleRepository;
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Autowired
    public void setPasswordEncoder(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String regNewUser(@ModelAttribute(name = "user") @Valid User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(roleRepository.findOneByTitle("ROLE_CUSTOMER")));
        userService.regNewUser(user);
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registration(Model model) {
        User user_new = new User();
        model.addAttribute("user", user_new);
        return "register";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
