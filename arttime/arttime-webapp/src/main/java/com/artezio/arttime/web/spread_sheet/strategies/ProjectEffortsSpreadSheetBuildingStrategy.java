package com.artezio.arttime.web.spread_sheet.strategies;

import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.HoursService;
import com.artezio.arttime.services.ProjectService;
import com.artezio.arttime.web.spread_sheet.ProjectEffortsSpreadSheet;
import com.artezio.arttime.web.spread_sheet.SpreadSheet;

public class ProjectEffortsSpreadSheetBuildingStrategy implements SpreadSheetBuildingStrategy {
    
    private static final long serialVersionUID = 1562100707868533296L;
    
    private HoursService hoursService;
    private EmployeeService employeeService;
    private ProjectService projectService;

    public ProjectEffortsSpreadSheetBuildingStrategy(HoursService hoursService, ProjectService projectService,
            EmployeeService employeeService) {
        this.hoursService = hoursService;
        this.projectService = projectService;
        this.employeeService = employeeService;
    }

    @Override
    public SpreadSheet buildSpreadSheet(Filter filter) {
        return new ProjectEffortsSpreadSheet(hoursService, projectService, employeeService, filter);
    }

}
