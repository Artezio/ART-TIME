window.onload = function() {
    bindContainerScrollEvent();
	$(window).trigger('resize');
};


function bindContainerScrollEvent() {
    $("div[id$=':container']").scroll(function () {
        closeOpenedPopups(this);
    });
}

function closeOpenedPopups(container) {
    $(container).find('input.hasDatepicker').blur().datepicker('hide');
    $.each(PrimeFaces.widgets, function (name, widget) {
        var correctType = widget instanceof PrimeFaces.widget.SelectOneMenu
            || widget instanceof PrimeFaces.widget.AutoComplete;
        if (correctType && widget.panel.is(':visible') && $.contains(container, widget.jq[0])) {
            widget.hide();
        }
    });
}