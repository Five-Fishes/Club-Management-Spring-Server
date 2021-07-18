package com.thirdcc.webapp.service.dto;

import java.io.Serializable;
import java.util.Objects;

import com.thirdcc.webapp.domain.enumeration.ClubFamilyCode;
import com.thirdcc.webapp.domain.enumeration.ClubFamilyRole;

/**
 * A DTO for the {@link com.thirdcc.webapp.domain.UserCCInfo} entity.
 */
public class UserCCInfoDTO implements Serializable {

    public UserCCInfoDTO() { }

    public UserCCInfoDTO(Long id, Long userId, ClubFamilyCode clubFamilyId, ClubFamilyRole familyRole, String yearSession, String fishLevel, String clubFamilyName, String clubFamilySlogan) {
        this.id = id;
        this.userId = userId;
        this.clubFamilyId = clubFamilyId;
        this.familyRole = familyRole;
        this.yearSession = yearSession;
        this.fishLevel = fishLevel;
        this.clubFamilyName = clubFamilyName;
        this.clubFamilySlogan = clubFamilySlogan;
    }

    private Long id;

    private Long userId;

    private ClubFamilyCode clubFamilyId;

    private ClubFamilyRole familyRole;

    private String yearSession;

    private String fishLevel;

    private String clubFamilyName;

    private String clubFamilySlogan;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ClubFamilyCode getClubFamilyId() {
        return clubFamilyId;
    }

    public void setClubFamilyId(ClubFamilyCode clubFamilyId) {
        this.clubFamilyId = clubFamilyId;
    }

    public ClubFamilyRole getFamilyRole() {
        return familyRole;
    }

    public void setFamilyRole(ClubFamilyRole familyRole) {
        this.familyRole = familyRole;
    }

    public String getYearSession() {
        return yearSession;
    }

    public void setYearSession(String yearSession) {
        this.yearSession = yearSession;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserCCInfoDTO userCCInfoDTO = (UserCCInfoDTO) o;
        if (userCCInfoDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), userCCInfoDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "UserCCInfoDTO{" +
            "id=" + getId() +
            ", userId=" + getUserId() +
            ", clubFamilyId=" + getClubFamilyId() +
            ", familyRole='" + getFamilyRole() + "'" +
            ", yearSession='" + getYearSession() + "'" +
            ", clubFamilyName='" + getClubFamilyName() + "'" +
            ", clubFamilySlogan='" + getClubFamilySlogan() + "'" +
            ", fishLevel='" + getFishLevel() + "'" +
            "}";
    }

    public String getFishLevel() {
        return fishLevel;
    }

    public void setFishLevel(String fishLevel) {
        this.fishLevel = fishLevel;
    }

    public String getClubFamilySlogan() {
        return clubFamilySlogan;
    }

    public void setClubFamilySlogan(String clubFamilySlogan) {
        this.clubFamilySlogan = clubFamilySlogan;
    }

    public String getClubFamilyName() {
        return clubFamilyName;
    }

    public void setClubFamilyName(String clubFamilyName) {
        this.clubFamilyName = clubFamilyName;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long id;
        private Long userId;
        private ClubFamilyCode clubFamilyId;
        private ClubFamilyRole familyRole;
        private String yearSession;
        private String fishLevel;
        private String clubFamilyName;
        private String clubFamilySlogan;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder clubFamilyId(ClubFamilyCode clubFamilyId) {
            this.clubFamilyId = clubFamilyId;
            return this;
        }

        public Builder familyRole(ClubFamilyRole familyRole) {
            this.familyRole = familyRole;
            return this;
        }

        public Builder yearSession(String yearSession) {
            this.yearSession = yearSession;
            return this;
        }

        public Builder fishLevel(String fishLevel) {
            this.fishLevel = fishLevel;
            return this;
        }

        public Builder clubFamilyName(String clubFamilyName) {
            this.clubFamilyName = clubFamilyName;
            return this;
        }

        public Builder clubFamilySlogan(String clubFamilySlogan) {
            this.clubFamilySlogan = clubFamilySlogan;
            return this;
        }

        public UserCCInfoDTO build() {
            return new UserCCInfoDTO(id, userId, clubFamilyId, familyRole, yearSession, fishLevel, clubFamilyName, clubFamilySlogan);
        }
    }
}
