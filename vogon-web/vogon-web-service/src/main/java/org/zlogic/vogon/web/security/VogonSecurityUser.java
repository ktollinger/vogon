/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.zlogic.vogon.data.VogonUser;

/**
 * The user details class for Spring Security
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class VogonSecurityUser implements UserDetails {

	/**
	 * The user authority
	 */
	public static final String AUTHORITY = "ROLE_VOGON_USER"; //NOI18N
	/**
	 * The user authorities list
	 */
	private static final List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority(AUTHORITY));

	/**
	 * The VogonUser form JPA
	 */
	private VogonUser user;

	/**
	 * Constructs a VogonSecurityUser from a JPA VogonUser
	 *
	 * @param user the JPA VogonUser
	 */
	protected VogonSecurityUser(VogonUser user) {
		this.user = user;
	}

	/**
	 * Returns the associated VogonUser
	 *
	 * @return the associated VogonUser
	 */
	public VogonUser getUser() {
		return user;
	}

	/**
	 * Sets the associated VogonUser
	 *
	 * @param user the associated VogonUser
	 */
	protected void setUser(VogonUser user) {
		this.user = user;
	}

	/**
	 * Returns all associated authorities
	 *
	 * @return all associated authorities
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	/**
	 * Returns the user's username
	 *
	 * @return the user's username
	 */
	@Override
	public String getUsername() {
		return user != null ? user.getUsername() : null;
	}

	/**
	 * Returns the user's password
	 *
	 * @return the user's password
	 */
	@Override
	public String getPassword() {
		return user != null ? user.getPassword() : null;
	}

	/**
	 * Returns true (for now)
	 *
	 * @return true
	 */
	@Override
	public boolean isAccountNonExpired() {
		return user != null;
	}

	/**
	 * Returns true (for now)
	 *
	 * @return true
	 */
	@Override
	public boolean isAccountNonLocked() {
		return user != null;
	}

	/**
	 * Returns true (for now)
	 *
	 * @return true
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return user != null;
	}

	/**
	 * Returns true (for now)
	 *
	 * @return true
	 */
	@Override
	public boolean isEnabled() {
		return user != null;
	}
}
