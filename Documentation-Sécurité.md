# Documentation — Sécurité Spring (Phase : Basic Auth)

Résumé
------
Ce document fournit la base théorique et une implémentation POC (Basic Auth) pour sécuriser les APIs REST de l'application SupplyChainX. Il couvre : concepts de sécurité web, architecture moderne de Spring Security (sans WebSecurityConfigurerAdapter), configuration moderne (SecurityFilterChain), fonctionnement et limites de Basic Auth, CSRF/CORS/sessions, Form Login (documentation uniquement), architecture interne (UserDetails, Providers, Encoders) et instructions de tests (Postman / cURL).

1. Sécurité Web & Concepts de Base
----------------------------------

1.1 Authentification vs Autorisation
- Authentification : prouver l'identité (ex. : login + mot de passe).
- Autorisation : vérifier les droits d'accès d'une identité authentifiée (ex. : rôle ROLE_ADMIN autorisé sur /api/admin).

1.2 Attaques Web courantes
- Brute force : essais massifs de combinaisons identifiants/mots de passe.
    - Mitigations : verrouillage temporaire, rate limiting, CAPTCHA côté GUI, logs & alerting.
- Cross-Site Scripting (XSS) : injection de scripts côté client.
    - Mitigations : échapper le contenu, Content Security Policy (CSP), validation côté serveur, header HttpOnly/CSP.
- Cross-Site Request Forgery (CSRF) : requêtes non désirées initiées par un site malveillant pour un utilisateur authentifié.
    - Mitigations : tokens CSRF, SameSite cookies, ou désactivation pour APIs stateless.
- Session fixation / vol de session :
    - Mitigations : régénération de session après authentification, flags Secure & HttpOnly sur cookies, durée d'expiration courte.
- Vol de session (session hijacking) :
    - Mitigations : HTTPS obligatoire, rotation de cookie, détection anomalies.

1.3 Importance de HTTPS
- Chiffrage obligatoire (confidentialité, intégrité).
- Basic Auth transmet des identifiants encodés en Base64 (non chiffrés) -> HTTPS indispensable.

1.4 Principes modernes — Defense in Depth
- Multicouches : réseau (firewall), transport (TLS), application (authN/authZ), stockage (chiffrement DB), monitoring & alerting.
- Principe du moindre privilège, logging, audits réguliers.

1.5 Nécessité d’une sécurité Backend pour API REST
- Les front-ends peuvent être compromis — le backend doit valider & appliquer les règles.
- Ne pas se reposer uniquement sur le client pour limiter l'accès.

2. Architecture moderne de Spring Security
-----------------------------------------

2.1 Composants clefs
- SecurityFilterChain : chaîne de filtres Spring Security (définie via bean SecurityFilterChain).
- DelegatingFilterProxy : point d'entrée servlet qui délègue à la chaîne Spring Security.
- AuthenticationManager : responsable de l'authentification (coordonne Provider(s)).
- AuthenticationProvider : exécute la vérification des credentials (ex. DaoAuthenticationProvider).
- UserDetailsService : charge les données utilisateurs (username, password, authorities).
- PasswordEncoder : encode/compare mot de passe (BCrypt recommandé).
- Roles vs Authorities :
    - ROLE_* (ex. ROLE_ADMIN) est une convention. Authorities peuvent représenter permissions fines (ex. "SUPPLIER:READ").

2.2 Disparition de WebSecurityConfigurerAdapter
- Depuis Spring Security 5.7+, config se fait par beans SecurityFilterChain et PasswordEncoder, plus claire et plus testable.

2.3 Schéma du flux d'une requête sécurisée (simplifié)
Client -> (TLS) -> DispatcherServlet -> DelegatingFilterProxy -> SecurityFilterChain -> BasicAuthenticationFilter / UsernamePasswordAuthenticationFilter -> AuthenticationManager -> AuthenticationProvider -> UserDetailsService -> PasswordEncoder (match) -> SecurityContextHolder (AUTHENTICATED) -> Controller -> Response

3. Configuration moderne — Exemple et explications
------------------------------------------------

3.1 Beans essentiels (exemple Spring Boot 3 / Spring Security moderne)
- SecurityFilterChain bean
- BCryptPasswordEncoder bean
- InMemoryUserDetailsManager (POC) ou UserDetailsService personnalisé pour BD

3.2 Exemple Java (configuration SecurityFilterChain, basic auth, utilisateurs in-memory)
```java
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // strength default 10
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.withUsername("admin")
            .password(encoder.encode("adminPass!23"))
            .roles("ADMIN")
            .build();

        UserDetails gestionnaire = User.withUsername("gest")
            .password(encoder.encode("gestPass!23"))
            .roles("GESTIONNAIRE_APPROVISIONNEMENT")
            .build();

        // ... autres utilisateurs ...
        return new InMemoryUserDetailsManager(admin, gestionnaire /*, ... */);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
          .csrf(csrf -> csrf.disable()) // APIs REST stateless
          .cors(cors -> cors.configurationSource(corsConfigurationSource()))
          .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/api/admin/**").hasRole("ADMIN")
              .requestMatchers(HttpMethod.GET, "/api/suppliers/**").hasAnyRole("ADMIN","GESTIONNAIRE_APPROVISIONNEMENT","RESPONSABLE_ACHATS")
              .anyRequest().authenticated()
          )
          .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    // Exemple CORS basique
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://frontend.supplychainx.example")); // restrict
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type"));
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

3.3 Choix du PasswordEncoder : BCrypt
- BCrypt : adaptation face au progrès matériel (work factor), sel intégré, résistant aux attaques rainbow-tables.
- Utiliser BCryptPasswordEncoder.
- Ne pas stocker de mot de passe en clair.

3.4 Gestion des utilisateurs
- POC : InMemoryUserDetailsManager avec users + roles.
- Production : implémenter UserDetailsService personnalisé (charger depuis BD, LDAP, etc.).
- Stockage : mots de passe hachés, métadonnées (enabled, accountNonExpired, etc.).

3.5 Pipeline de vérification des identifiants (schéma)
1. BasicAuthenticationFilter extrait header Authorization
2. Crée UsernamePasswordAuthenticationToken non authentifié
3. Delegue à AuthenticationManager
4. AuthenticationManager -> AuthenticationProvider (DaoAuthenticationProvider)
5. Provider appelle UserDetailsService.loadUserByUsername()
6. Compare password encodé via PasswordEncoder.matches()
7. Si ok, Authentication token authentifié retourné -> SecurityContextHolder

4. Basic Auth — Fonctionnement & sécurisation
--------------------------------------------

4.1 Définition
- Basic Auth : mécanisme HTTP standard qui envoie login:password encodé en Base64 dans l'en-tête Authorization.

4.2 Format header
- Authorization: Basic <base64-credentials>
- Exemple : "Authorization: Basic dXNlcjpwYXNz" où "dXNlcjpwYXNz" == Base64("user:pass")

4.3 Base64
- Encodage reversible (pas de chiffrement). Ne protège pas contre l'interception -> HTTPS requis.

4.4 Rôle du BasicAuthenticationFilter
- Intercepte les requêtes, lit l'en-tête Authorization, décode Base64, construit Authentication token et le soumet à l'AuthenticationManager.

4.5 Sécurisation d’une API REST via Basic Auth (bonnes pratiques)
- N'utiliser Basic Auth qu'avec HTTPS.
- Coupler avec rate limiting / détection brute force.
- Utiliser des secrets forts, rotation mot de passe, verrouillage après N échecs.
- Pour production considérer des mécanismes plus évolués (tokens JWT, OAuth2) pour sécurité, révocation, scopes.

4.6 Limites de Basic Auth
- Credentials envoyés à chaque requête (risque si fuite côté client).
- Pas de révocation simple (à part changer mot de passe).
- Pas de granularité fine (à gérer via roles/authorities).
- Pas adapté pour applications mobiles/SPA sans renforcement (ex : stockage sécurisé côté client).


5. CSRF, CORS & Sécurité basée sur les Sessions
-----------------------------------------------

5.1 CSRF
- Mécanisme : l'application délivre un token lié à la session; le client l'envoie avec requêtes mutantes pour prouver provenance.
- Pour APIs REST stateless (authentification par token / Basic + HTTPS), CSRF est souvent désactivé car pas de session/state côté serveur.
- CSRF reste pertinent si vous utilisez des cookies d'authentification (JSESSIONID).

5.2 Pourquoi CSRF désactivé par défaut pour APIs REST (POC)
- Les APIs REST stateless n'utilisent généralement pas les cookies pour l'authentification.
- Désactiver CSRF simplifie les appels API depuis des clients (Postman, mobile).
- Restez vigilant : si frontend utilise cookies, activer tokens CSRF.

5.3 CORS
- CORS permet au navigateur d'autoriser des requêtes cross-origin selon la politique serveurs.
- Configurer seulement les origines nécessaires (ne pas mettre "*").

5.4 Sessions & JSESSIONID (Documentation)
- JSESSIONID : cookie session côté serveur (stateful).
- Protection contre session fixation : invalider / régénérer la session après login.
- remember-me : fonctionnalité Spring Security pour se souvenir d'un utilisateur (mais crée de la persistance côté client).
- logout : invalider session, supprimer cookies, effacer SecurityContext.

6. Form Login (Documentation seulement)
---------------------------------------
- formLogin() génère un formulaire HTML d'authentification et utilise UsernamePasswordAuthenticationFilter.
- Flux :
  Form (POST /login -> UsernamePasswordAuthenticationFilter) -> AuthenticationManager -> Provider -> authentification -> SecurityContext stocké en session -> JSESSIONID cookie renvoyé.
- Form Login est stateful : nécessite sessions et active CSRF par défaut.
- Différence clé : Form Login conserve une session ; Basic Auth envoie credentials à chaque requête (stateless si session disabled).
- Personnalisation possible de login page, successHandler, failureHandler, param names.

Schéma : Form Login → UsernamePasswordAuthenticationFilter → AuthenticationManager → Provider → Session (JSESSIONID)

7. Architecture interne : UserDetails, Providers, Encoders
---------------------------------------------------------

7.1 UserDetails & UserDetailsService
- UserDetails : interface avec username, password, authorities, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked.
- UserDetailsService : méthode loadUserByUsername(String username) -> UserDetails.

7.2 AuthenticationProvider
- Ex. DaoAuthenticationProvider : utilise UserDetailsService + PasswordEncoder.
- Peut créer des providers custom (ex. SAML, OAuth, LDAP).

7.3 BCryptPasswordEncoder
- Fournit encodage + matches(raw, encoded).
- Usage : stocker encodedPassword dans DB, lors de login appeler matches() pour vérifier.

7.4 Mapping roles → authorities
- Bonnes pratiques : stocker roles (ROLE_...) et permissions si besoin.
- Autorisations fines en plus des roles (ex. "ORDER:CREATE").

7.5 Bonnes pratiques mot de passe
- AUCUN mot de passe en clair dans le dépôt.
- Utiliser hashing avec salt (BCrypt intègre le salt).
- Politique de rotation, expiration, 2FA pour comptes sensibles.
- Loguer mais ne jamais logguer passwords.

7.6 Schéma AuthenticationProvider
User credentials -> AuthenticationProvider -> UserDetailsService -> DB -> UserDetails (encoded password) -> PasswordEncoder.matches -> success/failure -> AuthenticationResult