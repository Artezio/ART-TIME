var warningMessage;

function initWarningMessage(message){
	warningMessage = message;
}
function setWindowUnloadMessage() {
	window.onbeforeunload = function() {		
		return warningMessage;
	};
}

function removeWindowUnloadMessage() {
    var crossPageMark = $("input[id$='crossPageModificationAttribute']");
    if ($(crossPageMark).length && window.onbeforeunload != null) {
        $(crossPageMark).val("true");
    }
    window.onbeforeunload = null;
}
	
function checkUnsavedModifications() {
	var crossPageMark = $("input[id$='crossPageModificationAttribute']");
	if ($(crossPageMark).length) {
		if ($(crossPageMark).val() == "true") {
			setWindowUnloadMessage();
		}
	}
	setupModificationListeners();
}

function setupModificationListeners() {
	$("input:not(.avoidChangeTracking input)").on("change", setWindowUnloadMessage);
	$("textarea:not(.avoidChangeTracking textarea)").on("change", setWindowUnloadMessage);
	$("select:not(.avoidChangeTracking select)").on("change", setWindowUnloadMessage);
	$(".ui-datepicker td").on("click", setWindowUnloadMessage);
	$(".ui-handsontable").on("change", setWindowUnloadMessage);
	$("button:not(.avoidChangeTracking button, .ui-autocomplete button)").on("click", setWindowUnloadMessage);
	$(".hasDatepicker:not(.avoidChangeTracking .hasDatepicker)").on("click", setWindowUnloadMessage);
	$(".ui-autocomplete-token-icon.ui-icon-close:not(.avoidChangeTracking)").on("click", setWindowUnloadMessage);

	$(".avoidChangeTracking").off("click change", setWindowUnloadMessage);

	$(".cancelTrackedChanges").click(removeWindowUnloadMessage);
}

window.addEventListener("load", setupModificationListeners, false);
$(document).on("pfAjaxComplete", setupModificationListeners);