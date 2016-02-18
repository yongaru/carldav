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

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@MappedSuperclass
public abstract class HibAuditableObject implements Serializable {

    private static final long serialVersionUID = 8396186357498363587L;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(targetEntity=User.class, fetch= FetchType.LAZY)
    @JoinColumn(name="ownerid", nullable = false)
    private User owner;

    @Column(name = "modifydate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedDate;

    @Column(name = "displayname")
    @NotEmpty
    private String displayName;
    
    @Column(name="etag")
    private String etag;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void updateTimestamp() {
        modifiedDate = new Date();
    }

    public String getEntityTag() {
        return etag;
    }

    public String calculateEntityTag() {
        String uid = getId() != null ? getId().toString() : "-";
        String modTime = getModifiedDate() != null ?
                Long.valueOf(getModifiedDate().getTime()).toString() : "-";
        String etag = uid + ":" + modTime;
        return encodeEntityTag(etag.getBytes(Charset.forName("UTF-8")));
    }

    private static String encodeEntityTag(byte[] bytes) {
        return DigestUtils.md5Hex(bytes);
    }
}
