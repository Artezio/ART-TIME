package com.artezio.arttime.services.integration.spi.keycloak;

import com.artezio.arttime.config.ApplicationSettings;
import com.artezio.arttime.config.Settings;
import com.artezio.arttime.services.integration.spi.UserInfo;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Startup
@Singleton
public class KeycloakClient {

    private final static String TIMER_INFO = "com.artezio.arttime.integration.keycloak";
    static final String KEYCLOAK_TRACKING_SYSTEM_NAME = "Keycloak";

    @Inject
    @ApplicationSettings
    private Settings settings;
    @Resource
    private TimerService timerService;

    private class Cache {
        private List<UserInfo> users = Collections.emptyList();
        private Set<String> departments = Collections.emptySet();
        private Map<String, List<UserInfo>> usersByGroups = Collections.emptyMap();

        Cache(List<UserInfo> users, Set<String> departments, Map<String, List<UserInfo>> usersByGroups) {
            this.users = users;
            this.departments = departments;
            this.usersByGroups = usersByGroups;
        }

        Cache() {
        }

        List<UserInfo> getUsers() {
            return users;
        }

        Set<String> getDepartments() {
            return departments;
        }

        Map<String, List<UserInfo>> getUsersByGroups() {
            return usersByGroups;
        }
    }

    volatile private Cache cache = new Cache();

    @PostConstruct
    void init() {
        if (isKeycloakUsedAsTrackingSystem()) {
            populateCache();
            createCachePopulatingTimer();
        }
    }

    @Timeout
    protected void populateCache() {
        RealmResource realm = getRealm();
        List<UserRepresentation> users = loadUsers(realm);
        Set<String> departments = loadDepartments(users);
        Map<String, List<UserInfo>> groupTeams = loadUserGroups(users, realm);
        this.cache = new Cache(toUserInfo(users), departments, groupTeams);
    }

    protected boolean isKeycloakUsedAsTrackingSystem() {
        return Objects.equals(settings.getDepartmentTrackingSystemName(), KEYCLOAK_TRACKING_SYSTEM_NAME)
                || Objects.equals(settings.getTeamTrackingSystemName(), KEYCLOAK_TRACKING_SYSTEM_NAME)
                || Objects.equals(settings.getEmployeeTrackingSystemName(), KEYCLOAK_TRACKING_SYSTEM_NAME);
    }

    protected void createCachePopulatingTimer() {
        TimerConfig config = new TimerConfig();
        config.setInfo(TIMER_INFO);
        config.setPersistent(false);
        Duration interval = settings.getKeycloakCacheRefreshInterval();
        if (interval.compareTo(Duration.ofMinutes(1)) < 0) {
            interval = Duration.ofMinutes(1);
        }
        long intervalMillis = interval.toMillis();
        timerService.createIntervalTimer(intervalMillis, intervalMillis, config);
    }

    public List<UserInfo> listUsers() {
        return cache.getUsers();
    }

    public List<UserInfo> listUsers(String groupName) {
        Map<String, List<UserInfo>> groups = cache.getUsersByGroups();
        return groups.getOrDefault(groupName, Collections.emptyList());
    }

    public Set<String> listDepartments() {
        return cache.getDepartments();
    }

    protected RealmResource getRealm() {
        return KeycloakBuilder.builder()
                .serverUrl(settings.getKeycloakServerUrl())
                .realm(settings.getKeycloakRealm())
                .clientId(settings.getKeycloakClientId())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientSecret(settings.getKeycloakClientSecret())
                .resteasyClient(new ResteasyClientBuilder()
                        .connectionPoolSize(10)
                        .register(new ResteasyJackson2Provider() {
                        })
                        .build())
                .build()
                .realm(settings.getKeycloakRealm());
    }

    protected Map<String, List<UserInfo>> loadUserGroups(List<UserRepresentation> users, RealmResource realm) {
        Map<String, List<UserInfo>> result = new ConcurrentHashMap<>();
        users.parallelStream()
                .forEach(user -> {
                    listRoles(user, realm)
                            .forEach(role -> {
                                result.computeIfAbsent(role, k -> new CopyOnWriteArrayList<>())
                                        .add(toUserInfo(user));
                            });
                });
        return result;
    }

    protected List<UserRepresentation> loadUsers(RealmResource realm) {
        int usersCount = realm.users().count();
        return realm.users().list(0, usersCount);
    }

    protected Set<String> loadDepartments(List<UserRepresentation> users) {
        return users.parallelStream()
                .map(user -> getUserAttribute(user, settings.getKeycloakUserDepartmentAttribute()))
                .map(department -> Optional.ofNullable(department)
                        .map(String::toLowerCase)
                        .orElse(null))
                .collect(Collectors.toSet());
    }

    protected Set<String> listRoles(UserRepresentation user, RealmResource realm) {
        List<RoleRepresentation> roles = realm.users().get(user.getId()).roles().realmLevel().listAll();
        return roles.stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toSet());
    }

    List<UserInfo> toUserInfo(List<UserRepresentation> userRepresentations) {
        return userRepresentations.parallelStream()
                .map(this::toUserInfo)
                .collect(Collectors.toList());
    }

    UserInfo toUserInfo(UserRepresentation userRepresentation) {
        return new UserInfo(
                userRepresentation.getUsername(),
                userRepresentation.getFirstName(),
                userRepresentation.getLastName(),
                userRepresentation.getEmail(),
                getUserAttribute(userRepresentation, settings.getKeycloakUserDepartmentAttribute()));
    }

    private String getUserAttribute(UserRepresentation user, String attributeName) {
        if (user.getAttributes() == null)
            return null;
        List<?> attributeValues = user.getAttributes().get(attributeName);
        return attributeValues != null && !attributeValues.isEmpty()
                ? attributeValues.get(0).toString()
                : null;
    }

}
