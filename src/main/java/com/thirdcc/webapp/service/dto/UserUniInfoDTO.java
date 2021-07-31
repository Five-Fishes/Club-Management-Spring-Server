package com.thirdcc.webapp.service.dto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import com.thirdcc.webapp.domain.UserUniInfo;
import com.thirdcc.webapp.domain.enumeration.ClubFamilyCode;
import com.thirdcc.webapp.domain.enumeration.Gender;
import com.thirdcc.webapp.domain.enumeration.UserUniStatus;

/**
 * A DTO for the {@link com.thirdcc.webapp.domain.UserUniInfo} entity.
 */
public class UserUniInfoDTO implements Serializable {

    private Long id;

    private Long userId;

    private String firstName;

    private String lastName;

    private String email;

    private Gender gender;

    private String phoneNumber;

    private LocalDate dateOfBirth;

    private String imageUrl;

    private Long courseProgramId;

    private String yearSession;

    private Integer intakeSemester;

    private String stayIn;

    private UserUniStatus status;

    private String graduateYearSession;

    private Integer totalSemester;

    private ClubFamilyCode clubFamilyCode;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Long getCourseProgramId() {
        return courseProgramId;
    }

    public void setCourseProgramId(Long courseProgramId) {
        this.courseProgramId = courseProgramId;
    }

    public String getYearSession() {
        return yearSession;
    }

    public void setYearSession(String yearSession) {
        this.yearSession = yearSession;
    }

    public Integer getIntakeSemester() {
        return intakeSemester;
    }

    public void setIntakeSemester(Integer intakeSemester) {
        this.intakeSemester = intakeSemester;
    }

    public String getStayIn() {
        return stayIn;
    }

    public void setStayIn(String stayIn) {
        this.stayIn = stayIn;
    }

    public UserUniStatus getStatus() {
        return status;
    }

    public void setStatus(UserUniStatus status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserUniInfoDTO)) {
            return false;
        }
        return id != null && id.equals(((UserUniInfoDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "UserUniInfoDTO{" +
            "id=" + id +
            ", userId=" + userId +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", gender=" + gender +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", dateOfBirth=" + dateOfBirth +
            ", courseProgramId=" + courseProgramId +
            ", yearSession='" + yearSession + '\'' +
            ", intakeSemester=" + intakeSemester +
            ", stayIn='" + stayIn + '\'' +
            ", status=" + status +
            ", imageUrl=" + imageUrl +
            ", clubFamilyCode=" + clubFamilyCode +
            '}';
    }

    public String getGraduateYearSession() {
        return graduateYearSession;
    }

    public void setGraduateYearSession(String graduateYearSession) {
        this.graduateYearSession = graduateYearSession;
    }

    public Integer getTotalSemester() {
        return totalSemester;
    }

    public void setTotalSemester(Integer totalSemester) {
        this.totalSemester = totalSemester;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ClubFamilyCode getClubFamilyCode() {
        return clubFamilyCode;
    }

    public void setClubFamilyCode(ClubFamilyCode clubFamilyCode) {
        this.clubFamilyCode = clubFamilyCode;
    }
}
