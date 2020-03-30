package pro.antonshu.client.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CompositeFilter;
import pro.antonshu.client.services.UserService;
import pro.antonshu.client.services.oauth2.FacebookUserInfoTokenServices;
import pro.antonshu.client.services.oauth2.GoogleUserInfoTokenServices;
import pro.antonshu.client.services.oauth2.VkUserInfoTokenServices;
import pro.antonshu.client.services.oauth2.YandexUserInfoTokenServices;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

@EnableOAuth2Client
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecureConfig extends WebSecurityConfigurerAdapter {

    private UserService userService;
    private OAuth2ClientContext oAuth2ClientContext;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setoAuth2ClientContext(OAuth2ClientContext oAuth2ClientContext) {
        this.oAuth2ClientContext = oAuth2ClientContext;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/edit/**").hasAnyRole("MANAGER", "ADMIN")
                .antMatchers("/admin/**").hasAnyRole("MANAGER", "ADMIN")
                .antMatchers("/profile/**").authenticated()
                .antMatchers("/order/**").authenticated()
                .antMatchers("/user/updatePassword*",
                        "/user/savePassword*",
                        "/updatePassword*")
                .hasAuthority("CHANGE_PASSWORD_PRIVILEGE")
                .anyRequest().permitAll()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/auth")
                .permitAll()
                .and()
                .logout()
                .logoutSuccessUrl("/")
                .permitAll()
        ;
        http.csrf()
                .ignoringAntMatchers("/api/v1/products/**")
                .ignoringAntMatchers("/ws/**");
        http.addFilterBefore(ssoFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    @Bean
    public FilterRegistrationBean oAuth2ClientFilterRegistration(OAuth2ClientContextFilter oAuth2ClientContextFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(oAuth2ClientContextFilter);
        registration.setOrder(-100);
        return registration;
    }

    private Filter ssoFilter() {

        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();

        OAuth2ClientAuthenticationProcessingFilter googleFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/google");
        OAuth2RestTemplate googleTemplate = new OAuth2RestTemplate(google(), oAuth2ClientContext);
        googleFilter.setRestTemplate(googleTemplate);
        GoogleUserInfoTokenServices tokenServices = new GoogleUserInfoTokenServices(googleResource().getUserInfoUri(), google().getClientId());
        tokenServices.setRestTemplate(googleTemplate);
        googleFilter.setTokenServices(tokenServices);
        tokenServices.setUserService(userService);
        tokenServices.setPasswordEncoder(passwordEncoder());
        filters.add(googleFilter);

        OAuth2ClientAuthenticationProcessingFilter facebookFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/facebook");
        OAuth2RestTemplate facebookTemplate = new OAuth2RestTemplate(facebook(), oAuth2ClientContext);
        facebookFilter.setRestTemplate(facebookTemplate);
        FacebookUserInfoTokenServices fbTokenServices = new FacebookUserInfoTokenServices(facebookResource().getUserInfoUri(), facebook().getClientId());
        fbTokenServices.setRestTemplate(facebookTemplate);
        facebookFilter.setTokenServices(fbTokenServices);
        fbTokenServices.setUserService(userService);
        fbTokenServices.setPasswordEncoder(passwordEncoder());
        filters.add(facebookFilter);

        OAuth2ClientAuthenticationProcessingFilter vkFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/vk");
        OAuth2RestTemplate vkTemplate = new OAuth2RestTemplate(vkontakte(), oAuth2ClientContext);
        vkFilter.setRestTemplate(vkTemplate);
        VkUserInfoTokenServices vkTokenServices = new VkUserInfoTokenServices(vkResource().getUserInfoUri(), vkontakte().getClientId());
        vkTokenServices.setRestTemplate(vkTemplate);
        vkFilter.setTokenServices(vkTokenServices);
        vkTokenServices.setUserService(userService);
        vkTokenServices.setPasswordEncoder(passwordEncoder());
        filters.add(vkFilter);

        OAuth2ClientAuthenticationProcessingFilter yaFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/yandex");
        OAuth2RestTemplate yaTemplate = new OAuth2RestTemplate(yandex(), oAuth2ClientContext);
        yaFilter.setRestTemplate(yaTemplate);
        YandexUserInfoTokenServices yaTokenServices = new YandexUserInfoTokenServices(yaResource().getUserInfoUri(), yandex().getClientId());
        yaTokenServices.setRestTemplate(yaTemplate);
        yaFilter.setTokenServices(yaTokenServices);
        yaTokenServices.setUserService(userService);
        yaTokenServices.setPasswordEncoder(passwordEncoder());
        filters.add(yaFilter);

        filter.setFilters(filters);

        return filter;
    }

    @Bean
    @ConfigurationProperties("google.client")
    public AuthorizationCodeResourceDetails google() {
        return new AuthorizationCodeResourceDetails();
    }

    @Bean
    @ConfigurationProperties("facebook.client")
    public AuthorizationCodeResourceDetails facebook() {
        return new AuthorizationCodeResourceDetails();
    }

    @Bean
    @ConfigurationProperties("vkontakte.client")
    public AuthorizationCodeResourceDetails vkontakte() {
        return new AuthorizationCodeResourceDetails();
    }

    @Bean
    @ConfigurationProperties("yandex.client")
    public AuthorizationCodeResourceDetails yandex() {
        return new AuthorizationCodeResourceDetails();
    }

    @Bean
    @ConfigurationProperties("google.resource")
    public ResourceServerProperties googleResource() {
        return new ResourceServerProperties();
    }

    @Bean
    @ConfigurationProperties("facebook.resource")
    public ResourceServerProperties facebookResource() {
        return new ResourceServerProperties();
    }

    @Bean
    @ConfigurationProperties("vkontakte.resource")
    public ResourceServerProperties vkResource() {
        return new ResourceServerProperties();
    }

    @Bean
    @ConfigurationProperties("yandex.resource")
    public ResourceServerProperties yaResource() {
        return new ResourceServerProperties();
    }
}
