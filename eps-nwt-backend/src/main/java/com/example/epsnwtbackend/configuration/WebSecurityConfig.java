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
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
@Configuration
// Injektovanje bean-a za bezbednost
@EnableWebSecurity
// Ukljucivanje podrske za anotacije "@Pre*" i "@Post*" koje ce aktivirati autorizacione provere za svaki pristup metodi
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig{

    // Servis koji se koristi za citanje podataka o korisnicima aplikacije
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserService();
    }

    // Implementacija PasswordEncoder-a koriscenjem BCrypt hashing funkcije.
    // BCrypt po defalt-u radi 10 rundi hesiranja prosledjene vrednosti.
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

    // Definisemo prava pristupa za zahteve ka odredjenim URL-ovima/rutama
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // sve neautentifikovane zahteve obradi uniformno i posalji 401 gresku
        http.exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint);
        http.authorizeRequests()
                .requestMatchers("/main").permitAll()
                .requestMatchers("/users/register").permitAll()
                .requestMatchers("/users/login").permitAll()
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
                .requestMatchers("/real-estate-request/docs").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/search/{municipality}/{address}").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/search/{municipality}/{address}?apartmentNumber").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/search/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/search-no-owner/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/search-no-owner/{municipality}/{address}").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/search-no-owner/{municipality}/{address}?apartmentNumber").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/availability/{name}/{timeRange}").permitAll()
                .requestMatchers(HttpMethod.GET, "/household/graph/{name}/{timeRange}").permitAll()
                .requestMatchers(HttpMethod.GET, "/socket/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/socket/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/socket/info?t").permitAll()
                .requestMatchers("/users/auth/activate").permitAll()  // Allow unauthenticated access to activate endpoint
                // ukoliko ne zelimo da koristimo @PreAuthorize anotacije nad metodama kontrolera, moze se iskoristiti hasRole() metoda da se ogranici
                // koji tip korisnika moze da pristupi odgovarajucoj ruti. Npr. ukoliko zelimo da definisemo da ruti 'admin' moze da pristupi
                // samo korisnik koji ima rolu 'ADMIN', navodimo na sledeci nacin:
                // .antMatchers("/admin").hasRole("ADMIN") ili .antMatchers("/admin").hasAuthority("ROLE_ADMIN")

                // za svaki drugi zahtev korisnik mora biti autentifikovan
                .anyRequest().authenticated().and()
                // za development svrhe ukljuci konfiguraciju za CORS iz WebConfig klase
                .cors().and()

                // umetni custom filter TokenAuthenticationFilter kako bi se vrsila provera JWT tokena umesto cistih korisnickog imena i lozinke (koje radi BasicAuthenticationFilter)
                .addFilterBefore(new TokenAuthenticationFilter(tokenUtils,  userDetailsService()), BasicAuthenticationFilter.class);

        // zbog jednostavnosti primera ne koristimo Anti-CSRF token (https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html)
        http.csrf().disable();
        http.headers().frameOptions().disable();

        // ulancavanje autentifikacije
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    // metoda u kojoj se definisu putanje za igorisanje autentifikacije
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // Autentifikacija ce biti ignorisana ispod navedenih putanja (kako bismo ubrzali pristup resursima)
        // Zahtevi koji se mecuju za web.ignoring().antMatchers() nemaju pristup SecurityContext-u
        // Dozvoljena POST metoda na ruti /auth/login, za svaki drugi tip HTTP metode greska je 401 Unauthorized
        return (web) -> web.ignoring().requestMatchers(HttpMethod.POST, "/users/login").requestMatchers(HttpMethod.POST, "/users/register")
                .requestMatchers(HttpMethod.POST, "/real-estate-request/registration")
                .requestMatchers(HttpMethod.POST, "/real-estate-request/docs")
                .requestMatchers(HttpMethod.PUT, "/real-estate-request/admin/finish/{requestId}")
                .requestMatchers(HttpMethod.GET, "/", "/webjars/**", "/*.html", "favicon.ico",
                        "/**.html", "/**.css", "/**.js",
                        "/household/find-by-id/", "/household/search/", "household/search-no-owner/",
                        "/household/availability/", "/real-estate-request", "/real-estate-request/{ownerId}/all",
                        "/real-estate-request/admin/requests", "/real-estate-request/admin/request/{requestId}",
                        "/users/byId/{userId}", "/real-estate-request/images/{realEstateId}", "household/graph/", 
                        "/socket/info/", "/socket/");

        // Ovim smo dozvolili pristup statickim resursima aplikacije
//                .requestMatchers(HttpMethod.GET, "/", "/webjars/**", "/*.html", "favicon.ico",
//                        "/**/*.html", "/**/*.css", "/**/*.js");

    }

}