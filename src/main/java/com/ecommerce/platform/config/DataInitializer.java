package com.ecommerce.platform.config;

import com.ecommerce.platform.entity.Category;
import com.ecommerce.platform.entity.Role;
import com.ecommerce.platform.entity.User;
import com.ecommerce.platform.repository.CategoryRepository;
import com.ecommerce.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@shop.com")) {
            User admin = new User("Admin", "admin@shop.com",
                    passwordEncoder.encode("admin123"), Role.ROLE_ADMIN);
            userRepository.save(admin);
        }

        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category("Electronics", "Gadgets, devices and accessories"));
            categoryRepository.save(new Category("Fashion", "Clothing, footwear and accessories"));
            categoryRepository.save(new Category("Home & Kitchen", "Appliances and household essentials"));
            categoryRepository.save(new Category("Books", "Fiction, non-fiction and academic books"));
        }
    }
}
