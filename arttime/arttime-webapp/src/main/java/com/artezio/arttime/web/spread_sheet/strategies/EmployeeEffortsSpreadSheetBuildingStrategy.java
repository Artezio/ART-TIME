package com.artezio.arttime.web.spread_sheet.strategies;

import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.HoursService;
import com.artezio.arttime.services.ProjectService;
import com.artezio.arttime.web.spread_sheet.EmployeeEffortsSpreadSheet;
import com.artezio.arttime.web.spread_sheet.SpreadSheet;

public class EmployeeEffortsSpreadSheetBuildingStrategy implements SpreadSheetBuildingStrategy {

    private static final long serialVersionUID = 5931331107250880865L;

    private HoursService hoursService;
    private ProjectService projectService;
    private EmployeeService employeeService;

    public EmployeeEffortsSpreadSheetBuildingStrategy(HoursService hoursService, ProjectService projectService,
            EmployeeService employeeService) {
        this.hoursService = hoursService;
        this.projectService = projectService;
        this.employeeService = employeeService;
    }

    @Override
    public SpreadSheet buildSpreadSheet(Filter filter) {
        return new EmployeeEffortsSpreadSheet(hoursService, projectService, employeeService, filter);
    }

}
