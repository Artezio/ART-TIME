package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.exceptions.SaveApprovedHoursException;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.HoursService;
import com.artezio.arttime.services.NotificationManagerLocal;
import com.artezio.arttime.services.ProjectService;
import com.artezio.arttime.web.spread_sheet.SpreadSheet;
import com.artezio.arttime.web.spread_sheet.SpreadSheetRow;
import com.artezio.arttime.web.spread_sheet.strategies.EmployeeEffortsSpreadSheetBuildingStrategy;
import com.artezio.arttime.web.spread_sheet.strategies.ProjectEffortsSpreadSheetBuildingStrategy;
import com.artezio.arttime.web.spread_sheet.strategies.SpreadSheetBuildingStrategy;
import org.omnifaces.util.Faces;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Named
@ViewScoped
public class ManageEffortsBean extends EffortsBean {

    private static final long serialVersionUID = -2955420544322390401L;

    @Inject
    private EmployeeService employeeService;
    @Inject
    private ProjectService projectService;
    @Inject
    private HoursService hoursService;
    @Inject
    private NotificationManagerLocal notificationManager;
    private Set<Hours> updatedStatus = new HashSet<>();

    protected SpreadSheet initSpreadSheet() {
        Filter currentFilter = filterBean.getCurrentFilter();
        return getSpreadSheetBuildingStrategy().buildSpreadSheet(currentFilter);
    }

    public void approveSelectedHours() {
        List<Hours> selectedHours = getSpreadSheet().getSelectedHours();
        updatedStatus.addAll(selectedHours);
        selectedHours.parallelStream()
                .forEach(hour -> hour.setApproved(true));
        getSpreadSheet().updateSelectedRows();
    }

    public void approveAllHours() {
        List<Hours> hours = getSpreadSheet().getHours();
        updatedStatus.addAll(hours);
        hours.parallelStream()
                .forEach(hour -> hour.setApproved(true));
        getSpreadSheet().updateAllRows();
    }

    public void disapproveAllHours() {
        List<Hours> hours = getSpreadSheet().getHours();
        updatedStatus.addAll(hours);
        hours.parallelStream()
                .forEach(hour -> hour.setApproved(false));
        getSpreadSheet().updateAllRows();
    }

    public void disapproveSelectedHours() {
        List<Hours> selectedHours = getSpreadSheet().getSelectedHours();
        updatedStatus.addAll(selectedHours);
        selectedHours.parallelStream()
                .forEach(hour -> hour.setApproved(false));
        getSpreadSheet().updateSelectedRows();
    }

    public void setFilterBean(FilterBean filterBean) {
        this.filterBean = filterBean;
    }

    public void saveHours() throws SaveApprovedHoursException, ReflectiveOperationException {
        Set<Hours> hours = getSpreadSheet().getUpdatedHours();
        hours.addAll(updatedStatus);
        hoursService.manageHours(hours);
        resetData();
    }

    @Override
    public void resetData() {
        super.resetData();
        updatedStatus = new HashSet<>();
    }

    public void setCurrentFilterAndResetData(Filter currentFilter) {
        filterBean.setCurrentFilter(currentFilter);
        resetData();
    }

    public boolean showMasterProjectTotalsLabel(SpreadSheetRow<?> row) {
        if (getGrouping() == EffortsGrouping.BY_PROJECTS) {
            int rowIndex = getSpreadSheet().getSheet().getRowIndex();
            SpreadSheetRow<?> previousRow = getSpreadSheet().getRows().get(rowIndex - 1);
            return !row.getProject().equals(previousRow.getProject());
        }
        return false;
    }

    public void requestReport() throws MessagingException {
        String recipientEmail = Faces.getRequestParameter("recipientEmail");
        notificationManager.requestWorkTimeReport(recipientEmail, filterBean.getCurrentFilter().getPeriod());
    }

    private SpreadSheetBuildingStrategy getSpreadSheetBuildingStrategy() {
        switch (getGrouping()) {
            case BY_EMPLOYEES:
                return new EmployeeEffortsSpreadSheetBuildingStrategy(hoursService, projectService, employeeService);
            case BY_PROJECTS:
            default:
                return new ProjectEffortsSpreadSheetBuildingStrategy(hoursService, projectService, employeeService);
        }
    }

}
