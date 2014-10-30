/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.security;

import java.util.ResourceBundle;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.zlogic.vogon.data.Constants;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.web.data.UserRepository;

/**
 * The user details service for Spring Security
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Service
public class UserService implements UserDetailsService, InitializingBean {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/web/messages");
	/**
	 * The users repository
	 */
	@Autowired
	private UserRepository userRepository;
	/**
	 * The PasswordEncoder instance
	 */
	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Loads a user by username
	 *
	 * @param username the username
	 * @return the loaded user
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		VogonUser user = userRepository.findByUsername(username);
		if (user == null)
			throw new UsernameNotFoundException(messages.getString("USER_CANNOT_BE_FOUND"));
		else
			return new VogonSecurityUser(user);
	}

	/**
	 * Updates user from database (e.g. to reflect
	 *
	 * @param securityUser user to update
	 */
	public void refreshUser(VogonSecurityUser securityUser) {
		if (securityUser.getUser() != null) {
			VogonUser user = userRepository.findOne(securityUser.getUser().getId());
			securityUser.setUser(user);
		}
	}

	/**
	 * Applies default properties and creates default user if needed
	 *
	 * @throws Exception in case of errors
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (userRepository.count() == 0) {
			VogonUser defaultUser = new VogonUser(Constants.DEFAULT_USERNAME, passwordEncoder.encode(Constants.DEFAULT_PASSWORD));
			userRepository.saveAndFlush(defaultUser);
		}
	}
}
