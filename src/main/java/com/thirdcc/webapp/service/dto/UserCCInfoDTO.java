package com.thirdcc.webapp.service.dto;

import java.io.Serializable;
import java.util.Objects;

import com.thirdcc.webapp.domain.User;
import com.thirdcc.webapp.domain.UserUniInfo;
import com.thirdcc.webapp.domain.enumeration.ClubFamilyCode;
import com.thirdcc.webapp.domain.enumeration.ClubFamilyRole;
import com.thirdcc.webapp.service.mapper.UserMapper;
import com.thirdcc.webapp.service.mapper.UserUniInfoMapperImpl;

/**
 * A DTO for the {@link com.thirdcc.webapp.domain.UserCCInfo} entity.
 */
public class UserCCInfoDTO implements Serializable {

    public UserCCInfoDTO() { }

    public UserCCInfoDTO(Long id, Long userId, ClubFamilyCode clubFamilyCode, ClubFamilyRole familyRole, String yearSession, String fishLevel, String clubFamilyName, String clubFamilySlogan) {
        this.id = id;
        this.userId = userId;
        this.clubFamilyCode = clubFamilyCode;
        this.familyRole = familyRole;
        this.yearSession = yearSession;
        this.fishLevel = fishLevel;
        this.clubFamilyName = clubFamilyName;
        this.clubFamilySlogan = clubFamilySlogan;
    }

    public UserCCInfoDTO(Long id, Long userId, ClubFamilyCode clubFamilyCode, ClubFamilyRole familyRole, String yearSession, String fishLevel, String clubFamilyName, String clubFamilySlogan, UserDTO user, UserUniInfoDTO userUniInfo) {
        this.id = id;
        this.userId = userId;
        this.clubFamilyCode = clubFamilyCode;
        this.familyRole = familyRole;
        this.yearSession = yearSession;
        this.fishLevel = fishLevel;
        this.clubFamilyName = clubFamilyName;
        this.clubFamilySlogan = clubFamilySlogan;
        this.user = user;
        this.userUniInfo = userUniInfo;
    }

    private Long id;

    private Long userId;

    private ClubFamilyCode clubFamilyCode;

    private ClubFamilyRole familyRole;

    private String yearSession;

    private String fishLevel;

    private String clubFamilyName;

    private String clubFamilySlogan;

    private UserDTO user;

    private UserUniInfoDTO userUniInfo;

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

    public ClubFamilyCode getClubFamilyCode() {
        return clubFamilyCode;
    }

    public void setClubFamilyCode(ClubFamilyCode clubFamilyCode) {
        this.clubFamilyCode = clubFamilyCode;
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
            ", clubFamilyCode=" + getClubFamilyCode() +
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

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public UserUniInfoDTO getUserUniInfo() {
        return userUniInfo;
    }

    public void setUserUniInfo(UserUniInfoDTO userUniInfo) {
        this.userUniInfo = userUniInfo;
    }
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long id;
        private Long userId;
        private ClubFamilyCode clubFamilyCode;
        private ClubFamilyRole familyRole;
        private String yearSession;
        private String fishLevel;
        private String clubFamilyName;
        private String clubFamilySlogan;
        private UserDTO user;
        private UserUniInfoDTO userUniInfo;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder clubFamilyCode(ClubFamilyCode clubFamilyCode) {
            this.clubFamilyCode = clubFamilyCode;
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

        public Builder setUser(User user) {
            if(user != null) {
                this.user = new UserMapper().userToUserDTO(user);
            }
            return this;
        }

        public Builder setUserUniInfo (UserUniInfo userUniInfo) {
            if(userUniInfo != null) {
                this.userUniInfo = new UserUniInfoMapperImpl().toDto(userUniInfo);
            }
            return this;
        }

        public UserCCInfoDTO build() {
            return new UserCCInfoDTO(id, userId, clubFamilyCode, familyRole, yearSession, fishLevel, clubFamilyName, clubFamilySlogan, user, userUniInfo);
        }
    }
}
