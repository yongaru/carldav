/*
 * Copyright 2006 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.model.hibernate;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.unitedinternet.cosmo.model.User;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="users")
public class HibUser implements User, Serializable {

    private static final long serialVersionUID = -5401963358119490736L;

    public static final int EMAIL_LEN_MIN = 1;
    public static final int EMAIL_LEN_MAX = 128;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id = Long.valueOf(-1);

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "password")
    @NotNull
    private String password;

    @Column(name = "email", nullable=true, unique=true)
    @Length(min=EMAIL_LEN_MIN, max=EMAIL_LEN_MAX)
    @Email
    @NaturalId
    private String email;

    @Column(name = "locked")
    private boolean locked;

    @Column(name = "roles")
    private String roles;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<HibItem> items;

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#getPassword()
     */
    public String getPassword() {
        return password;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#setPassword(java.lang.String)
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#getEmail()
     */
    public String getEmail() {
        return email;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#setEmail(java.lang.String)
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#isLocked()
     */
    public boolean isLocked() {
        return locked;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#setLocked(java.lang.Boolean)
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public String getRoles() {
        return roles;
    }

    @Override
    public void setRoles(final String roles) {
        this.roles = roles;
    }

    /**
     * Username determines equality 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || email == null) {
            return false;
        }
        if (! (obj instanceof User)) {
            return false;
        }
        
        return email.equals(((User) obj).getEmail());
    }

    @Override
        public int hashCode() {
        if (email == null) {
            return super.hashCode();
        }
        else {
            return email.hashCode();
        }
    }
}
