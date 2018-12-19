$(document).ready(function() {
    var leftPanel = $('.left-panel');
    var styleStates = {
        open: {
            left: 0
        },
        closed: {
            left: -1 * leftPanel.width() + 30
        }
    };

    var timeout;

    leftPanel.on('mouseenter', function (e) {
        clearTimeout(timeout);
        timeout = setTimeout(function () {

            e.preventDefault();
            e.stopPropagation();
            leftPanel.addClass('over');
            changeState(false);
        }, 200);
    });

    leftPanel.on('mouseleave', function (e) {
        clearTimeout(timeout);
        timeout = setTimeout(function() {
            e.preventDefault();
            e.stopPropagation();
            leftPanel.removeClass('over');
            changeState(false);
        }, 200);
    });

    function changeState(saveState){
        var isPin = leftPanel.hasClass('pin');
        var isOver = leftPanel.hasClass('over');

        if (!isPin && !isOver){
            leftPanel.animate(styleStates.closed, 50, 'linear');
            if (saveState) {
                onFilterClose();
            }
        } else {
            leftPanel.animate(styleStates.open, 50, 'linear');
            if (saveState) {
                onFilterOpen();
            }
        }
    }

    function pin(e){
        var target = $(e.target);
        if (target.is('.js-toggle-left-panel_inside')){
            leftPanel.addClass('over')
        } else {
            leftPanel.removeClass('over');
        }
        leftPanel.toggleClass('pin');
        changeState(true);
    }

    if (isPanelOpen()) {
        leftPanel.addClass('pin')
            .animate(styleStates.closed, 50, 'linear')
            .animate(styleStates.open, 175, 'linear');
    } else {
        leftPanel.css(styleStates.closed);
    }

    $('.js-toggle-left-panel').on('click', pin)
});
