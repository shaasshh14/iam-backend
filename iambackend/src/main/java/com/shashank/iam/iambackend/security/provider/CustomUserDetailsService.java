package com.shashank.iam.iambackend.security.provider;

import com.shashank.iam.iambackend.modules.role.entity.Role;
import com.shashank.iam.iambackend.modules.user.entity.User;
import com.shashank.iam.iambackend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	@Override
	public UserDetails loadUserByUsername(String email)
			throws UsernameNotFoundException {

		User user = userRepository
				.findByEmail(email.trim().toLowerCase())
				.orElseThrow(() -> new UsernameNotFoundException(
						"User not found"));

		Set<SimpleGrantedAuthority> authorities = user.getRoles()
				.stream()
				.map(Role::getName)
				.map(role -> new SimpleGrantedAuthority(
						"ROLE_" + role.toUpperCase()))
				.collect(Collectors.toSet());

		return org.springframework.security.core.userdetails.User
				.withUsername(user.getEmail())
				.password(user.getPassword())
				.disabled(!user.isEnabled())
				.authorities(authorities)
				.build();
	}
}