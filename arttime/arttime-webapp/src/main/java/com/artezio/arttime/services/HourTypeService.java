package com.artezio.arttime.services;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;

import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.exceptions.ActualTimeRemovalException;
import com.artezio.arttime.repositories.HourTypeRepository;

@Named
@Stateless
@PermitAll
public class HourTypeService implements Serializable {

    @Inject
    private HourTypeRepository hourTypeRepository;

    public HourType find(Long id) {
        return hourTypeRepository.query()
                .id(id)
                .getSingleResultOrNull();
    }

    public HourType findActualTime() {
        return hourTypeRepository.query()
                .actualTime()
                .getSingleResultOrNull();
    }

    public List<HourType> getAll() {
        return hourTypeRepository.query()
                .list();
    }

    public List<HourType> getAll(List<Long> ids) {
        return getAll().stream()
                .filter(ht -> ids.contains(ht.getId()))
                .collect(Collectors.toList());
    }

    public HourType create(HourType hourType) {
        if (!isActualTimeExists()) {
            hourType.setActualTime(true);
        }
        return hourTypeRepository.create(hourType);
    }

    public HourType update(HourType hourType) {
        return hourTypeRepository.update(hourType);
    }

    public void remove(HourType hourType) throws ActualTimeRemovalException {
        hourTypeRepository.remove(hourType);
    }

    public HourType setActualTime(HourType hourType) {
        hourTypeRepository.resetActualTime();
        hourType.setActualTime(true);
        return update(hourType);
    }

    private boolean isActualTimeExists() {
        return hourTypeRepository.query()
                .actualTime()
                .getSingleResultOrNull() != null;
    }

}
