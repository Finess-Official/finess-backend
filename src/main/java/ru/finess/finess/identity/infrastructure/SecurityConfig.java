package ru.finess.finess.identity.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesUserDetailsService;
import ru.finess.finess.common.date.HttpUtils;
import ru.finess.finess.identity.application.UserRepository;
import ru.finess.finess.identity.domain.Session;
import ru.finess.finess.identity.domain.UserId;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtValidator jwtValidator;
  private final UserRepository userRepository;

  @RequiredArgsConstructor
  public static class JwtFilter extends AbstractPreAuthenticatedProcessingFilter
      implements AuthenticationDetailsSource<HttpServletRequest, AuthenticationDetails> {

    private final JwtValidator jwtValidator;

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
      return getSession(request).map(Session::user).orElse(null);
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
      // This method is not used in this implementation, but it must be overridden
      return "dummy";
    }

    @Override
    public AuthenticationDetails buildDetails(HttpServletRequest context) {
      return getSession(context).map(AuthenticationDetails::authenticated).orElse(null);
    }

    private Optional<Session> getSession(HttpServletRequest request) {
      return HttpUtils.getAccessTokenFromRequest(request).flatMap(jwtValidator::validateSession);
    }
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            matcher ->
                matcher
                    .requestMatchers(HttpMethod.POST, "/api/identity/v1/tokens")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/identity/v1/users")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/identity/v1/tokens:refresh")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .anonymous(AbstractHttpConfigurer::disable)
        .addFilter(preAuthenticatedProcessingFilter())
        .exceptionHandling(
            exception ->
                exception
                    .authenticationEntryPoint(
                        (request, response, authException) ->
                            response.sendError(401, "Unauthorized"))
                    .accessDeniedHandler(
                        (request, response, accessDeniedException) ->
                            response.sendError(403, "Forbidden")))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(HttpBasicConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .csrf(CsrfConfigurer::disable)
        .sessionManagement(SessionManagementConfigurer::disable);
    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public Supplier<Optional<UserId>> currentUserSupplier() {
    return () -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (Objects.isNull(authentication)) {
        return Optional.empty();
      }
      Object details = authentication.getDetails();
      if (details instanceof AuthenticationDetails(UserId user)) {
        return Optional.of(user);
      }
      return Optional.empty();
    };
  }

  private JwtFilter preAuthenticatedProcessingFilter() {
    JwtFilter filter = new JwtFilter(jwtValidator);
    filter.setAuthenticationDetailsSource(filter);
    PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
    provider.setPreAuthenticatedUserDetailsService(
        new PreAuthenticatedGrantedAuthoritiesUserDetailsService());
    filter.setAuthenticationManager(new ProviderManager(provider));
    return filter;
  }
}
