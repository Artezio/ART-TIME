package com.artezio.arttime.services;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.artezio.arttime.exceptions.ActualTimeRemovalException;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.repositories.HourTypeRepository;

import junitx.framework.ListAssert;
import org.mockito.Mockito;
import org.mockito.internal.verification.Calls;
import org.mockito.verification.VerificationMode;
import org.powermock.api.mockito.PowerMockito;

@RunWith(EasyMockRunner.class)
public class HourTypeServiceTest {
    
    @TestSubject
    private HourTypeService hourTypeService = new HourTypeService();
    @Mock
    private HourTypeRepository hourTypeRepository;

    @Test
    public void testFind_HourTypeExists() {
        HourType hourType = createHourType(1L);
        HourTypeRepository.HourTypeQuery query = Mockito.mock(HourTypeRepository.HourTypeQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hourTypeRepository.query()).andReturn(query);
        Mockito.when(query
                .id(1L)
                .getSingleResultOrNull()).thenReturn(hourType);
        EasyMock.replay(hourTypeRepository);

        HourType actual = hourTypeService.find(1L);

        EasyMock.verify(hourTypeRepository);
        Mockito.verify(query.id(1L)).getSingleResultOrNull();

        assertEquals(hourType, actual);
    }

    @Test
    public void testFind_HourTypeNotExists() {
        HourTypeRepository.HourTypeQuery query = Mockito.mock(HourTypeRepository.HourTypeQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hourTypeRepository.query()).andReturn(query);
        Mockito.when(query
                .id(1L)
                .getSingleResultOrNull()).thenReturn(null);
        EasyMock.replay(hourTypeRepository);

        HourType actual = hourTypeService.find(1L);

        EasyMock.verify(hourTypeRepository);
        Mockito.verify(query.id(1L)).getSingleResultOrNull();

        assertNull(actual);
    }

    @Test
    public void testFindActualTime_HourTypeExists() {
        HourType hourType = createHourType(1L);
        HourTypeRepository.HourTypeQuery query = Mockito.mock(HourTypeRepository.HourTypeQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hourTypeRepository.query()).andReturn(query);
        Mockito.when(query
                .actualTime()
                .getSingleResultOrNull()).thenReturn(hourType);
        EasyMock.replay(hourTypeRepository);

        HourType actual = hourTypeService.findActualTime();

        EasyMock.verify(hourTypeRepository);
        Mockito.verify(query.actualTime()).getSingleResultOrNull();

        assertEquals(hourType, actual);
    }

    @Test
    public void testFindActualTime_HourTypeNotExists() {
        HourTypeRepository.HourTypeQuery query = Mockito.mock(HourTypeRepository.HourTypeQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hourTypeRepository.query()).andReturn(query);
        Mockito.when(query
                .actualTime()
                .getSingleResultOrNull()).thenReturn(null);
        EasyMock.replay(hourTypeRepository);

        HourType actual = hourTypeService.findActualTime();

        EasyMock.verify(hourTypeRepository);
        Mockito.verify(query.actualTime()).getSingleResultOrNull();

        assertNull(actual);
    }

    @Test
    public void testGetAll_HourTypesExist() {
        List<HourType> hourTypes = asList(createHourType(1L), createHourType(2L), createHourType(3L));
        HourTypeRepository.HourTypeQuery query = Mockito.mock(HourTypeRepository.HourTypeQuery.class);

        EasyMock.expect(hourTypeRepository.query()).andReturn(query);
        Mockito.when(query
                .list()).thenReturn(hourTypes);
        EasyMock.replay(hourTypeRepository);

        List<HourType> actual = hourTypeService.getAll();

        EasyMock.verify(hourTypeRepository);
        Mockito.verify(query).list();

        ListAssert.assertEquals(hourTypes, actual);
    }

    @Test
    public void testGetAll_HourTypesNotExist() {
        HourTypeRepository.HourTypeQuery query = Mockito.mock(HourTypeRepository.HourTypeQuery.class);

        EasyMock.expect(hourTypeRepository.query()).andReturn(query);
        Mockito.when(query
                .list()).thenReturn(emptyList());
        EasyMock.replay(hourTypeRepository);

        List<HourType> actual = hourTypeService.getAll();

        EasyMock.verify(hourTypeRepository);
        Mockito.verify(query).list();

        assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetAll_ByIds_HourTypesExist() {
        List<HourType> hourTypes = asList(createHourType(1L), createHourType(2L), createHourType(3L));
        List<HourType> expected = asList(hourTypes.get(0), hourTypes.get(2));
        HourTypeRepository.HourTypeQuery query = Mockito.mock(HourTypeRepository.HourTypeQuery.class);

        EasyMock.expect(hourTypeRepository.query()).andReturn(query);
        Mockito.when(query
                .list()).thenReturn(hourTypes);
        EasyMock.replay(hourTypeRepository);

        List<HourType> actual = hourTypeService.getAll(asList(1L, 3L));

        EasyMock.verify(hourTypeRepository);
        Mockito.verify(query).list();

        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetAll_ByIds_HourTypesNotExist() {
        HourTypeRepository.HourTypeQuery query = Mockito.mock(HourTypeRepository.HourTypeQuery.class);

        EasyMock.expect(hourTypeRepository.query()).andReturn(query);
        Mockito.when(query
                .list()).thenReturn(emptyList());
        EasyMock.replay(hourTypeRepository);

        List<HourType> actual = hourTypeService.getAll(asList(1L, 3L));

        EasyMock.verify(hourTypeRepository);
        Mockito.verify(query).list();

        assertTrue(actual.isEmpty());
    }

    @Test
    public void testCreate_ActualTimeNotExists() {
        HourType hourType = createHourType(1L);
        hourType.setActualTime(true);
        HourTypeRepository.HourTypeQuery query = Mockito.mock(HourTypeRepository.HourTypeQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hourTypeRepository.query()).andReturn(query);
        EasyMock.expect(hourTypeRepository.create(hourType)).andReturn(hourType);
        Mockito.when(query
                .actualTime()
                .getSingleResultOrNull()).thenReturn(null);
        EasyMock.replay(hourTypeRepository);

        HourType actual = hourTypeService.create(hourType);

        EasyMock.verify(hourTypeRepository);
        Mockito.verify(query.actualTime()).getSingleResultOrNull();

        assertEquals(hourType, actual);
    }

    @Test
    public void testCreate_ActualTimeExists() {
        HourType hourType = createHourType(1L);
        HourTypeRepository.HourTypeQuery query = Mockito.mock(HourTypeRepository.HourTypeQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hourTypeRepository.query()).andReturn(query);
        EasyMock.expect(hourTypeRepository.create(hourType)).andReturn(hourType);
        Mockito.when(query
                .actualTime()
                .getSingleResultOrNull()).thenReturn(createHourType(2L));
        EasyMock.replay(hourTypeRepository);

        HourType actual = hourTypeService.create(hourType);

        EasyMock.verify(hourTypeRepository);
        Mockito.verify(query.actualTime()).getSingleResultOrNull();

        assertEquals(hourType, actual);
    }

    @Test
    public void testUpdate() {
        HourType hourType = createHourType(1L);

        EasyMock.expect(hourTypeRepository.update(hourType)).andReturn(hourType);
        EasyMock.replay(hourTypeRepository);

        HourType actual = hourTypeService.update(hourType);

        EasyMock.verify(hourTypeRepository);

        assertEquals(hourType, actual);
    }

    @Test
    public void testRemove() throws ActualTimeRemovalException {
        HourType hourType = createHourType(1L);

        hourTypeRepository.remove(hourType);
        EasyMock.expectLastCall();
        EasyMock.replay(hourTypeRepository);

        hourTypeService.remove(hourType);

        EasyMock.verify(hourTypeRepository);
    }

    @Test
    public void testSetActualTime() {
        HourType hourType = createHourType(1L);

        hourTypeRepository.resetActualTime();
        EasyMock.expectLastCall();
        EasyMock.expect(hourTypeRepository.update(hourType)).andReturn(hourType);
        EasyMock.replay(hourTypeRepository);

        HourType actual = hourTypeService.setActualTime(hourType);

        EasyMock.verify(hourTypeRepository);

        assertTrue(actual.isActualTime());
    }

    private HourType createHourType(Long id) {
        HourType hourType = new HourType();
        try {
            setField(hourType, "id", id);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Error during creating test HourType", e);
        }
        hourType.setType(id.toString());
        return hourType;
    }

}
