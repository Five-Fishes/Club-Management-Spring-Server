package com.thirdcc.webapp.domain;
import com.thirdcc.webapp.domain.enumeration.ClubFamilyCode;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;

import com.thirdcc.webapp.domain.enumeration.ClubFamilyRole;

/**
 * A UserCCInfo.
 */
@Entity
@Table(name = "user_cc_info")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserCCInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "club_family_code")
    private ClubFamilyCode clubFamilyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "family_role")
    private ClubFamilyRole familyRole;

    @Column(name = "year_session")
    private String yearSession;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public UserCCInfo userId(Long userId) {
        this.userId = userId;
        return this;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ClubFamilyCode getClubFamilyCode() {
        return clubFamilyCode;
    }

    public UserCCInfo clubFamilyCode(ClubFamilyCode clubFamilyCode) {
        this.clubFamilyCode = clubFamilyCode;
        return this;
    }

    public void setClubFamilyCode(ClubFamilyCode clubFamilyCode) {
        this.clubFamilyCode = clubFamilyCode;
    }

    public ClubFamilyRole getFamilyRole() {
        return familyRole;
    }

    public UserCCInfo familyRole(ClubFamilyRole familyRole) {
        this.familyRole = familyRole;
        return this;
    }

    public void setFamilyRole(ClubFamilyRole familyRole) {
        this.familyRole = familyRole;
    }

    public String getYearSession() {
        return yearSession;
    }

    public UserCCInfo yearSession(String yearSession) {
        this.yearSession = yearSession;
        return this;
    }

    public void setYearSession(String yearSession) {
        this.yearSession = yearSession;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserCCInfo)) {
            return false;
        }
        return id != null && id.equals(((UserCCInfo) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "UserCCInfo{" +
            "id=" + getId() +
            ", userId=" + getUserId() +
            ", clubFamilyCode=" + getClubFamilyCode() +
            ", familyRole='" + getFamilyRole() + "'" +
            ", yearSession='" + getYearSession() + "'" +
            "}";
    }
}
