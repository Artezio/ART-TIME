(function() {
    function limitPanelsWidth() {
        $.each(PrimeFaces.widgets, function (name, widget) {
            if (widget instanceof PrimeFaces.widget.SelectOneMenu) {
                var width = widget.jq.outerWidth();
                widget.panel.width(width);
            }
        })
    }

    $(window).on('load', limitPanelsWidth);
    $(document).on('pfAjaxComplete', limitPanelsWidth);
})();