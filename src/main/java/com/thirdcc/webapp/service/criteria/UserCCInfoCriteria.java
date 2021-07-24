package com.thirdcc.webapp.service.criteria;

import com.thirdcc.webapp.domain.enumeration.ClubFamilyCode;
import com.thirdcc.webapp.domain.enumeration.ClubFamilyRole;
import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the {@link com.thirdcc.webapp.domain.UserCCInfo} entity. This class is used
 * in {@link com.thirdcc.webapp.web.rest.UserCCInfoResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /user-cc-infos?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class UserCCInfoCriteria implements Serializable, Criteria {

  /**
   * Class for filtering ClubFamilyRole
   */
  public static class ClubFamilyRoleFilter extends Filter<ClubFamilyRole> {

    public ClubFamilyRoleFilter() {}

    public ClubFamilyRoleFilter(ClubFamilyRoleFilter filter) {
      super(filter);
    }

    @Override
    public ClubFamilyRoleFilter copy() {
      return new ClubFamilyRoleFilter(this);
    }
  }

    /**
     * Class for filtering ClubFamilyCode
     */
    public static class ClubFamilyCodeFilter extends Filter<ClubFamilyCode> {

        public ClubFamilyCodeFilter() {}

        public ClubFamilyCodeFilter(ClubFamilyCodeFilter filter) {
            super(filter);
        }

        @Override
        public ClubFamilyCodeFilter copy() {
            return new ClubFamilyCodeFilter(this);
        }
    }

  private static final long serialVersionUID = 1L;

  private LongFilter id;

  private LongFilter userId;

  private ClubFamilyCodeFilter clubFamilyCode;

  private ClubFamilyRoleFilter familyRole;

  private StringFilter yearSession;

  public UserCCInfoCriteria() {}

  public UserCCInfoCriteria(UserCCInfoCriteria other) {
    this.id = other.id == null ? null : other.id.copy();
    this.userId = other.userId == null ? null : other.userId.copy();
    this.clubFamilyCode = other.clubFamilyCode == null ? null : other.clubFamilyCode.copy();
    this.familyRole = other.familyRole == null ? null : other.familyRole.copy();
    this.yearSession = other.yearSession == null ? null : other.yearSession.copy();
  }

  @Override
  public UserCCInfoCriteria copy() {
    return new UserCCInfoCriteria(this);
  }

  public LongFilter getId() {
    return id;
  }

  public LongFilter id() {
    if (id == null) {
      id = new LongFilter();
    }
    return id;
  }

  public void setId(LongFilter id) {
    this.id = id;
  }

  public LongFilter getUserId() {
    return userId;
  }

  public LongFilter userId() {
    if (userId == null) {
      userId = new LongFilter();
    }
    return userId;
  }

  public void setUserId(LongFilter userId) {
    this.userId = userId;
  }

  public ClubFamilyCodeFilter getClubFamilyCode() {
    return clubFamilyCode;
  }

  public ClubFamilyCodeFilter clubFamilyCode() {
    if (clubFamilyCode == null) {
      clubFamilyCode = new ClubFamilyCodeFilter();
    }
    return clubFamilyCode;
  }

  public void setClubFamilyCode(ClubFamilyCodeFilter clubFamilyCode) {
    this.clubFamilyCode = clubFamilyCode;
  }

  public ClubFamilyRoleFilter getFamilyRole() {
    return familyRole;
  }

  public ClubFamilyRoleFilter familyRole() {
    if (familyRole == null) {
      familyRole = new ClubFamilyRoleFilter();
    }
    return familyRole;
  }

  public void setFamilyRole(ClubFamilyRoleFilter familyRole) {
    this.familyRole = familyRole;
  }

  public StringFilter getYearSession() {
    return yearSession;
  }

  public StringFilter yearSession() {
    if (yearSession == null) {
      yearSession = new StringFilter();
    }
    return yearSession;
  }

  public void setYearSession(StringFilter yearSession) {
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
    final UserCCInfoCriteria that = (UserCCInfoCriteria) o;
    return (
      Objects.equals(id, that.id) &&
      Objects.equals(userId, that.userId) &&
      Objects.equals(clubFamilyCode, that.clubFamilyCode) &&
      Objects.equals(familyRole, that.familyRole) &&
      Objects.equals(yearSession, that.yearSession)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId, clubFamilyCode, familyRole, yearSession);
  }

  // prettier-ignore
    @Override
    public String toString() {
        return "UserCCInfoCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (userId != null ? "userId=" + userId + ", " : "") +
            (clubFamilyCode != null ? "clubFamilyId=" + clubFamilyCode + ", " : "") +
            (familyRole != null ? "familyRole=" + familyRole + ", " : "") +
            (yearSession != null ? "yearSession=" + yearSession + ", " : "") +
            "}";
    }
}
