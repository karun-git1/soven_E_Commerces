package com.ecommerce.platform.service;

import com.ecommerce.platform.dto.RegisterRequest;
import com.ecommerce.platform.entity.Role;
import com.ecommerce.platform.entity.User;
import com.ecommerce.platform.exception.ResourceNotFoundException;
import com.ecommerce.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CartService cartService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        User user = new User(request.getName(), request.getEmail(),
                passwordEncoder.encode(request.getPassword()), Role.ROLE_CUSTOMER);
        user.setPhone(request.getPhone());
        User saved = userRepository.save(user);
        cartService.createCartForUser(saved);
        return saved;
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public User updateProfile(Long userId, String name, String phone, String address) {
        User user = getById(userId);
        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);
        return userRepository.save(user);
    }
}
