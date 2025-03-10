package br.com.taina.copy_twitter.configuration;

import br.com.taina.copy_twitter.entity.Role;
import br.com.taina.copy_twitter.entity.User;
import br.com.taina.copy_twitter.repository.RoleRepository;
import br.com.taina.copy_twitter.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Configuration

public class AdminUserConfig implements CommandLineRunner {

    private RoleRepository roleRepository;

    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    public AdminUserConfig(RoleRepository roleRepository,
                           UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        var roleAdmin = roleRepository.findByName(Role.Values.ADMIN.name());

        var userAdmin = userRepository.findByUsername("admin");

        // A função ifPresentOrElse é usada para verificar se o Optional contém um valor.
        userAdmin.ifPresentOrElse(
                user -> {
                    System.out.println("Admin já existe!");
                },
                // Caso contrário (o Optional está vazio), a segunda lambda será executada.
                () -> {
                    var user = new User();
                    user.setUsername("admin");
                    // Define a senha como "123" criptografada usando o passwordEncoder.
                    user.setPassword(passwordEncoder.encode("123"));
                    // Associa o papel de ADMIN ao novo usuário.
                    //O Set.of() cria um Set imutável, ou seja, após ser criado,
                    // você não pode adicionar, remover ou alterar os elementos do conjunto.
                    // Isso é importante quando você quer garantir que os dados não sejam alterados acidentalmente.
                    user.setRoles(Set.of(roleAdmin));
                    userRepository.save(user);
                }
        );
    }
}

