package com.artezio.arttime.services.integration.spi.keycloak;

import com.artezio.arttime.config.Setting;
import com.artezio.arttime.config.Settings;
import com.artezio.arttime.services.integration.spi.UserInfo;
import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import java.time.Duration;
import java.util.*;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(EasyMockRunner.class)
public class KeycloakClientTest {

    @TestSubject
    private KeycloakClient keycloakClient = new KeycloakClient();
    private final String KEYCLOAK_CLIENT_ID = "TestClientId";
    private Settings settings;

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Before
    public void setUp() throws NoSuchFieldException {
        environmentVariables.set(Setting.Name.KEYCLOAK_CLIENT_ID.name(), KEYCLOAK_CLIENT_ID);
        settings = new Settings(new HashMap<>());
        setField(keycloakClient, "settings", settings);
    }

    @Test
    public void testIsKeycloakUsedAsTrackingSystem() {
        boolean actual = keycloakClient.isKeycloakUsedAsTrackingSystem();
        assertFalse(actual);
    }

    @Test
    public void testIsKeycloakUsedAsTrackingSystem_departmentTrackingSystem() {
        settings.setDepartmentTrackingSystemName(KeycloakClient.KEYCLOAK_TRACKING_SYSTEM_NAME);
        boolean actual = keycloakClient.isKeycloakUsedAsTrackingSystem();
        assertTrue(actual);
    }

    @Test
    public void testIsKeycloakUsedAsTrackingSystem_employeeTrackingSystem() {
        settings.setEmployeeTrackingSystemName(KeycloakClient.KEYCLOAK_TRACKING_SYSTEM_NAME);
        boolean actual = keycloakClient.isKeycloakUsedAsTrackingSystem();
        assertTrue(actual);
    }

    @Test
    public void testIsKeycloakUsedAsTrackingSystem_teamTrackingSystem() {
        settings.setTeamTrackingSystemName(KeycloakClient.KEYCLOAK_TRACKING_SYSTEM_NAME);
        boolean actual = keycloakClient.isKeycloakUsedAsTrackingSystem();
        assertTrue(actual);
    }

    @Test
    public void testToUserInfo() {
        String username = "username";
        String firstName = "fname";
        String lastName = "lname";
        String email = "email";
        String departmentAttrribute = "department";
        String departmentValue = "dep";
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(departmentAttrribute, Arrays.asList(departmentValue));
        UserRepresentation userRepresentation = mock(UserRepresentation.class);
        expect(userRepresentation.getUsername()).andReturn(username);
        expect(userRepresentation.getFirstName()).andReturn(firstName);
        expect(userRepresentation.getLastName()).andReturn(lastName);
        expect(userRepresentation.getEmail()).andReturn(email);
        expect(userRepresentation.getAttributes()).andReturn(attributes).times(2);
        replay(userRepresentation);
        settings.setKeycloakUserDepartmentAttribute(departmentAttrribute);

        UserInfo actual = keycloakClient.toUserInfo(userRepresentation);

        verify(userRepresentation);
        assertEquals(username, actual.getUsername());
        assertEquals(firstName, actual.getFirstName());
        assertEquals(lastName, actual.getLastName());
        assertEquals(email, actual.getEmail());
        assertEquals(departmentValue, actual.getDepartment());
    }

    @Test
    public void testPopulateCache() {
        keycloakClient = createMockBuilder(KeycloakClient.class)
                .addMockedMethod("loadUsers", RealmResource.class)
                .addMockedMethod("loadDepartments", List.class)
                .addMockedMethod("loadUserGroups", List.class, RealmResource.class)
                .addMockedMethod("toUserInfo", List.class)
                .addMockedMethod("getRealm")
                .createMock();

        RealmResource mockRealm = createMock(RealmResource.class);
        List<UserRepresentation> usersList = new ArrayList<>();
        Set<String> departments = new HashSet<>();
        expect(keycloakClient.getRealm()).andReturn(mockRealm);
        expect(keycloakClient.loadUsers(mockRealm)).andReturn(usersList);
        expect(keycloakClient.loadDepartments(usersList)).andReturn(departments);
        expect(keycloakClient.loadUserGroups(usersList, mockRealm)).andReturn(Collections.emptyMap());
        UserInfo userInfo = new UserInfo("username", null, null, null, null);
        expect(keycloakClient.toUserInfo(anyObject(List.class))).andReturn(Arrays.asList(userInfo)).anyTimes();
        replay(keycloakClient);

        keycloakClient.populateCache();

        verify(keycloakClient);
    }

    @Test
    public void testLoadUserGroups() throws NoSuchFieldException {
        keycloakClient = createMockBuilder(KeycloakClient.class)
                .addMockedMethod("listGroups", UserRepresentation.class, RealmResource.class)
                .createMock();
        setField(keycloakClient, "settings", settings);

        String departmentAttribute = "department";
        settings.setKeycloakUserDepartmentAttribute(departmentAttribute);
        String username1 = "user1";
        String username2 = "user2";
        String username3 = "user3";
        String group1 = "grp1";
        String group2 = "grp2";
        String group3 = "grp3";
        Map<String, List<String>> user1attributes = new HashMap<>();
        Map<String, List<String>> user2attributes = new HashMap<>();
        Map<String, List<String>> user3attributes = new HashMap<>();
        List<String> user1groups = Arrays.asList(group1, group2);
        List<String> user2groups = Arrays.asList(group1, group3);
        List<String> user3groups = Arrays.asList(group3);
        user1attributes.put(departmentAttribute, user1groups);
        user2attributes.put(departmentAttribute, user2groups);
        user3attributes.put(departmentAttribute, user3groups);
        UserRepresentation user1 = new UserRepresentation();
        UserRepresentation user2 = new UserRepresentation();
        UserRepresentation user3 = new UserRepresentation();
        user1.setUsername(username1);
        user2.setUsername(username2);
        user3.setUsername(username3);
        user1.setAttributes(user1attributes);
        user2.setAttributes(user2attributes);
        user3.setAttributes(user3attributes);

        RealmResource mockRealm = mock(RealmResource.class);

        expect(keycloakClient.listGroups(user1, mockRealm)).andReturn(new HashSet<>(user1groups));
        expect(keycloakClient.listGroups(user2, mockRealm)).andReturn(new HashSet<>(user2groups));
        expect(keycloakClient.listGroups(user3, mockRealm)).andReturn(new HashSet<>(user3groups));
        replay(keycloakClient);

        Map<String, List<UserInfo>> actual = keycloakClient.loadUserGroups(Arrays.asList(user1, user2, user3), mockRealm);

        verify(keycloakClient);

        assertTrue(actual.containsKey(group1));
        assertTrue(actual.containsKey(group2));
        assertTrue(actual.containsKey(group3));

        List<UserInfo> userInfosGroup1 = actual.get(group1);
        List<UserInfo> userInfosGroup2 = actual.get(group2);
        List<UserInfo> userInfosGroup3 = actual.get(group3);

        assertTrue(userInfosGroup1.stream().anyMatch(u -> u.getUsername().equals(username1)));
        assertTrue(userInfosGroup1.stream().anyMatch(u -> u.getUsername().equals(username2)));
        assertFalse(userInfosGroup1.stream().anyMatch(u -> u.getUsername().equals(username3)));
        assertTrue(userInfosGroup2.stream().anyMatch(u -> u.getUsername().equals(username1)));
        assertFalse(userInfosGroup2.stream().anyMatch(u -> u.getUsername().equals(username2)));
        assertFalse(userInfosGroup2.stream().anyMatch(u -> u.getUsername().equals(username3)));
        assertFalse(userInfosGroup3.stream().anyMatch(u -> u.getUsername().equals(username1)));
        assertTrue(userInfosGroup3.stream().anyMatch(u -> u.getUsername().equals(username2)));
        assertTrue(userInfosGroup3.stream().anyMatch(u -> u.getUsername().equals(username3)));
    }

    @Test
    public void testCreateIntervalTimer() throws Exception {
        settings.setKeycloakCacheRefreshIntervalMinutes(1);
        javax.ejb.Timer timer = createMock(Timer.class);
        TimerService timerService = createMock(TimerService.class);
        long millis = Duration.ofMinutes(1).toMillis();
        expect(timerService.createIntervalTimer(eq(millis), eq(millis), anyObject(TimerConfig.class))).andReturn(timer);
        setField(keycloakClient, "timerService", timerService);
        replay(timerService);

        keycloakClient.createCachePopulatingTimer();

        verify(timerService);
    }

    @Test
    public void testCreateIntervalTimer_durationLessThanOneMinute() throws Exception {
        settings.setKeycloakCacheRefreshIntervalMinutes(0);
        javax.ejb.Timer timer = createMock(Timer.class);
        TimerService timerService = createMock(TimerService.class);
        long expectedMillis = Duration.ofMinutes(1).toMillis();
        expect(timerService.createIntervalTimer(eq(expectedMillis), eq(expectedMillis), anyObject(TimerConfig.class)))
                .andReturn(timer);
        setField(keycloakClient, "timerService", timerService);
        replay(timerService);

        keycloakClient.createCachePopulatingTimer();

        verify(timerService);
    }

    @Test
    public void testLoadDepartments() {
        String attribute = "department";
        String department1 = "dep1";
        String department2 = "2";
        String department3 = null;
        settings.setKeycloakUserDepartmentAttribute(attribute);
        UserRepresentation user1 = new UserRepresentation();
        UserRepresentation user2 = new UserRepresentation();
        UserRepresentation user3 = new UserRepresentation();
        user1.setUsername("user1");
        user2.setUsername("user2");
        user3.setUsername("user3");
        user1.singleAttribute(attribute, department1);
        user2.singleAttribute(attribute, department2);
        user3.singleAttribute(attribute, department3);

        Set<String> actual = keycloakClient.loadDepartments(Arrays.asList(user1, user2, user3));

        assertFalse(actual.isEmpty());
        assertTrue(actual.contains(department1));
        assertTrue(actual.contains(department2));
        assertTrue(actual.contains(department3));
    }

    @Test
    public void testInit() throws NoSuchFieldException {
        settings.setDepartmentTrackingSystemName("someOtherSystem");
        settings.setTeamTrackingSystemName("someOtherSystem");
        settings.setEmployeeTrackingSystemName("someOtherSystem");
        keycloakClient = createMockBuilder(KeycloakClient.class)
                .addMockedMethod("populateCache")
                .addMockedMethod("createCachePopulatingTimer")
                .createMock();
        setField(keycloakClient, "settings", settings);
        replay(keycloakClient);

        keycloakClient.init();

        verify(keycloakClient);
    }

    @Test
    public void testInit_keycloakIsUsedAsTrackingSystem() throws NoSuchFieldException {
        settings.setDepartmentTrackingSystemName(KeycloakClient.KEYCLOAK_TRACKING_SYSTEM_NAME);
        keycloakClient = createMockBuilder(KeycloakClient.class)
                .addMockedMethod("populateCache")
                .addMockedMethod("createCachePopulatingTimer")
                .createMock();
        setField(keycloakClient, "settings", settings);
        keycloakClient.populateCache();
        expectLastCall();
        keycloakClient.createCachePopulatingTimer();
        expectLastCall();
        replay(keycloakClient);

        keycloakClient.init();

        verify(keycloakClient);
    }

    @Test
    public void testListGroups() {
        UserRepresentation user = new UserRepresentation();
        RealmResource realm = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        UserResource userResource = mock(UserResource.class);
        String group1 = "Group1";
        String group2 = "Group_2";
        String userId = "id-user";
        user.setId(userId);
        expect(realm.users()).andReturn(usersResource);
        expect(usersResource.get(userId)).andReturn(userResource);
        GroupRepresentation groupRepresentation1 = new GroupRepresentation();
        GroupRepresentation groupRepresentation2 = new GroupRepresentation();
        groupRepresentation1.setName(group1);
        groupRepresentation2.setName(group2);
        expect(userResource.groups()).andReturn(Arrays.asList(groupRepresentation1, groupRepresentation2));
        replay(realm, usersResource, userResource);

        Set<String> actual = keycloakClient.listGroups(user, realm);

        verify(realm);
        assertFalse(actual.isEmpty());
        assertTrue(actual.contains(group1));
        assertTrue(actual.contains(group2));
    }
}