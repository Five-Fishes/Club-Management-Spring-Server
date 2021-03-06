package com.thirdcc.webapp.annotations.authorization;

import com.thirdcc.webapp.annotations.cleanup.CleanUpCCAdministrator;
import com.thirdcc.webapp.domain.Administrator;
import com.thirdcc.webapp.domain.YearSession;
import com.thirdcc.webapp.domain.enumeration.AdministratorRole;
import com.thirdcc.webapp.domain.enumeration.AdministratorStatus;
import com.thirdcc.webapp.repository.AdministratorRepository;
import com.thirdcc.webapp.repository.UserRepository;
import com.thirdcc.webapp.security.AuthoritiesConstants;
import com.thirdcc.webapp.service.YearSessionService;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCurrentCCAdministrator.Factory.class)
@CleanUpCCAdministrator
public @interface WithCurrentCCAdministrator {

    String firstName() default "";

    String email() default "";

    String imageUrl() default "";

    String[] authorities() default {AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER};

    class Factory extends AbstractSecurityContextFactoryTemplate<WithCurrentCCAdministrator> {

        private final AdministratorRepository administratorRepository;
        private final YearSessionService yearSessionService;

        public Factory(
            UserRepository userRepository,
            AdministratorRepository administratorRepository,
            YearSessionService yearSessionService
        ) {
            super(userRepository);
            this.administratorRepository = administratorRepository;
            this.yearSessionService = yearSessionService;
        }

        @Override
        public Set<String> configureAuthorityNames(WithCurrentCCAdministrator annotation) {
            return new HashSet<>(Arrays.asList(annotation.authorities()));
        }

        @Override
        public String createUserEmail(WithCurrentCCAdministrator annotation) {
            boolean hasEmail = !annotation.email().isEmpty();
            if (hasEmail) {
                return annotation.email();
            }
            return super.createUserEmail(annotation);
        }

        @Override
        public String configureFirstName(WithCurrentCCAdministrator annotation) {
            return annotation.firstName();
        }

        @Override
        public String configureImageUrl(WithCurrentCCAdministrator annotation) {
            return annotation.imageUrl();
        }

        @Override
        public void onSecurityContextCreatedHook() {
            YearSession currentYearSession = yearSessionService.getCurrentYearSession();
            Administrator savedAdministrator = initAdministratorDB(currentYearSession);
        }

        private Administrator initAdministratorDB(YearSession yearSession) {
            Administrator administrator = new Administrator();
            administrator.setUserId(getUser().getId());
            administrator.setRole(AdministratorRole.SECRETARY);
            administrator.setStatus(AdministratorStatus.ACTIVE);
            administrator.setYearSession(yearSession.getValue());
            return administratorRepository.saveAndFlush(administrator);
        }
    }
}
