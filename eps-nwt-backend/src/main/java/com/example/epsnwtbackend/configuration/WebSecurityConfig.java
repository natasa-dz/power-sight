package com.example.epsnwtbackend.configuration;

import com.example.epsnwtbackend.security.RestAuthenticationEntryPoint;
import com.example.epsnwtbackend.security.TokenAuthenticationFilter;
import com.example.epsnwtbackend.service.UserService;
import com.example.epsnwtbackend.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig implements WebMvcConfigurer{

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost","http://localhost:4200")
                .allowedMethods("GET", "PUT", "POST", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserService();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Autowired
    private TokenUtils tokenUtils;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults());
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.cors(Customizer.withDefaults());

        http.exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint));

        http.authorizeRequests()
                .requestMatchers("/users/register").permitAll()
                .requestMatchers("/users/login").permitAll()
                .requestMatchers("/main").permitAll()
                .requestMatchers("/users/{email}").permitAll()
                .requestMatchers("/users/byId/{userId}").permitAll()
                .requestMatchers("/users").permitAll()
                .requestMatchers("/household/find-by-id/{id}").permitAll()
                .requestMatchers("/household/search/{address}/{apartmentNumber}").permitAll()
                .requestMatchers("/real-estate-request/registration").permitAll()
                .requestMatchers("/real-estate-request").permitAll()
                .requestMatchers("/real-estate-request/{ownerId}/all").permitAll()
                .requestMatchers("/real-estate-request/admin/requests").permitAll()
                .requestMatchers("/real-estate-request/admin/request/{requestId}").permitAll()
                .requestMatchers("/real-estate-request/admin/finish/{requestId}").permitAll()
                .requestMatchers("/real-estate-request/images/{realEstateId}").permitAll()
                .requestMatchers("/real-estate-request/documentation/{realEstateId}").permitAll()
                .requestMatchers("/real-estate-request/docs/{realEstateId}").permitAll()
                .requestMatchers("/household/docs/**").permitAll()
                .requestMatchers("/employee/search").permitAll()
                .requestMatchers("/employee/search?username").permitAll()
                .requestMatchers("ownership-requests/**").permitAll()
                .requestMatchers("ownership-requests/requestOwnership").permitAll()
                .requestMatchers("ownership-requests/pending").permitAll()
                .requestMatchers("ownership-requests/{userId}").permitAll()
                .requestMatchers("ownership-requests/process/{id}").permitAll()

                .requestMatchers("/employee/search/{username}").permitAll()
                .requestMatchers("/employee/all-employees").permitAll()
                .requestMatchers("/employee/all-employees-no-pagination").permitAll()

                .requestMatchers("/employee/find-by-id/{id}").permitAll()
                .requestMatchers("/employee/find-by-user-id/{id}").permitAll()
                .requestMatchers("/employee/image").permitAll()
                .requestMatchers("employee/suspend/{employeeId}").permitAll()
                .requestMatchers("/appointments/create").permitAll()
                .requestMatchers("/appointments/available-slots/{employeeId}").permitAll()
                .requestMatchers("/appointments/get-employees-appointments-for-date/{employeeId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/citizen/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/getForOwner/{ownerId}").permitAll()
                .requestMatchers(HttpMethod.PUT, "/household/allow-access/{householdId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/search/{municipality}/{address}").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/search/{municipality}/{address}?apartmentNumber").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/search/**").permitAll()
                .requestMatchers("/household/no-owner").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/search-no-owner/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/search-no-owner/{municipality}/{address}").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/search-no-owner/{municipality}/{address}?apartmentNumber").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/availability/{name}/{timeRange}").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/graph/{name}/{timeRange}").permitAll()
                .requestMatchers(HttpMethod.GET, "/consumption/{city}/{timeRange}").permitAll()
                .requestMatchers(HttpMethod.GET, "/consumption/graph/{city}/{timeRange}").permitAll()
                .requestMatchers(HttpMethod.GET, "/consumption/household/{householdId}/{timeRange}").permitAll()
                .requestMatchers(HttpMethod.GET, "/consumption/household/graph/{householdId}/{timeRange}").permitAll()
                .requestMatchers(HttpMethod.GET, "/consumption/municipalities").permitAll()
                .requestMatchers(HttpMethod.GET, "/consumption/cities").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/current/{name}").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/owner").permitAll()
                .requestMatchers(HttpMethod.GET, "/socket/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/socket/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/socket/info?t").permitAll()
                .requestMatchers(HttpMethod.POST, "/price-list/create").permitAll()
                .requestMatchers(HttpMethod.POST, "/receipts/create/{month}/{year}").permitAll()
                .requestMatchers(HttpMethod.GET, "/receipts/by-id/{receiptId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/receipts/all-for-household/{householdId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/receipts/all-for-owner/{ownerId}").permitAll()
                .requestMatchers(HttpMethod.PUT, "/receipts/pay/{receiptId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/price-list/find-by-id/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/price-list/find-all").permitAll()
                .requestMatchers(HttpMethod.GET, "/price-list/find-for-date/{date}").permitAll()
                .requestMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/users/auth/activate").permitAll()
                .requestMatchers(HttpMethod.OPTIONS,"/users/auth/activate").permitAll()
                .requestMatchers(HttpMethod.PATCH,"/users/auth/activate").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated().and()
                .cors().and()
                .addFilterAfter(new TokenAuthenticationFilter(tokenUtils,  userDetailsService()), UsernamePasswordAuthenticationFilter.class);

        http.headers(headers -> headers.frameOptions().disable());
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(HttpMethod.POST, "/users/login").requestMatchers(HttpMethod.POST, "/users/register")
                .requestMatchers(HttpMethod.POST, "/real-estate-request/registration")
                .requestMatchers(HttpMethod.POST, "/real-estate-request/docs")
                .requestMatchers(HttpMethod.GET, "/users/auth/activate")
                .requestMatchers(HttpMethod.OPTIONS,"/users/auth/activate")
                .requestMatchers(HttpMethod.PATCH,"/users/auth/activate")
                .requestMatchers(HttpMethod.POST, "/employee/image")
                .requestMatchers(HttpMethod.POST, "/appointments/create")
                .requestMatchers(HttpMethod.POST, "/price-list/create")
                .requestMatchers(HttpMethod.POST, "/receipts/create/{month}/{year}")
                .requestMatchers(HttpMethod.PUT, "/real-estate-request/admin/finish/{requestId}")
                .requestMatchers(HttpMethod.PUT, "/employee/suspend/")
                .requestMatchers(HttpMethod.PUT, "/household/allow-access/{householdId}")
                .requestMatchers(HttpMethod.PUT, "/receipts/pay/{receiptId}")
                .requestMatchers(HttpMethod.GET, "/", "/webjars/**", "/*.html", "favicon.ico",
                        "/**.html", "/**.css", "/**.js",
                        "/ownership-requests/**","/ownership-requests/requestOwnership", "/ownership-requests/pending",
                        "/ownership-requests/process/{id}", "/ownership-requests/{userId}",
                        "/household/docs/**", "/household/find-by-id/", "/household/search/", "household/search-no-owner/","household/no-owner",
                        "/household/availability/", "/real-estate-request", "/real-estate-request/{ownerId}/all",
                        "/real-estate-request/admin/requests", "/real-estate-request/admin/request/{requestId}",
                        "/users/byId/{userId}", "/real-estate-request/documentation/{realEstateId}",
                        "/real-estate-request/images/{realEstateId}", "household/graph/",
                        "/consumption/{city}/{timeRange}", "/consumption/municipalities", "/consumption/graph/{city}/{timeRange}",
                        "/consumption/cities", "consumption/household/graph/{householdId}/{timeRange}", "consumption/household/{householdId}/{timeRange}",
                        "/socket/info/", "/socket/", "/employee/search", "employee/find-by-id/",
                        "appointments/available-slots/", "employee/find-by-user-id/",
                        "appointments/get-employees-appointments-for-date/", "employee/all-employees",
                        "household/current/", "household/owner", "citizen/search", "household/getForOwner/{ownerId}",
                        "price-list/find-all", "price-list/find-by-id/{id}", "price-list/find-for-date/{date}",
                        "/receipts/all-for-household/{householdId}", "/receipts/all-for-owner/{ownerId}",
                        "/receipts/by-id/{receiptId}", "/employee/all-employees-no-pagination");


    }

}