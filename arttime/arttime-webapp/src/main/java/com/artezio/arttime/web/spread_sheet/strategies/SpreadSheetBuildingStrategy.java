package com.artezio.arttime.web.spread_sheet.strategies;

import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.web.spread_sheet.SpreadSheet;

import java.io.Serializable;

public interface SpreadSheetBuildingStrategy extends Serializable {

    SpreadSheet buildSpreadSheet(Filter filter);

}
