function WeekSelector() {
    var startDateId = 'form:advancedPeriodSelector:weekSelector:startDate';
    var endDateId = 'form:advancedPeriodSelector:weekSelector:endDate';
    var filterWeekSelectorId = 'form:advancedPeriodSelector:weekSelector:filterWeekSelector';

    var startDate, endDate;
    var locale = getLocale();
    var weekInput;

    this.init = function() {
        var start = $(PrimeFaces.escapeClientId(startDateId)).val().split('.');
        startDate = new Date(start[2], start[1] - 1, start[0]);
        var end = $(PrimeFaces.escapeClientId(endDateId)).val().split('.');
        endDate = new Date(end[2], end[1] - 1, end[0]);
        weekInput = $(PrimeFaces.escapeClientId(filterWeekSelectorId));
        $(weekInput).val(startDate.toLocaleDateString(locale) + ' - ' + endDate.toLocaleDateString(locale));
        $.datepicker.setDefaults($.datepicker.regional[((locale === 'en') ? '' : locale)]);
        $(weekInput).datepicker({
            showOtherMonths: true,
            selectOtherMonths: true,
            onSelect: function () {
                var date = $(this).datepicker('getDate');
                var firstDayOfWeek = $(this).datepicker("option", "firstDay");
                startDate = new Date(date.getFullYear(), date.getMonth(), date.getDate() - date.getDay() + firstDayOfWeek);
                endDate = new Date(date.getFullYear(), date.getMonth(), date.getDate() - date.getDay() + firstDayOfWeek + 6);
                showDates();
            },
            beforeShowDay: function (date) {
                var cssClass = '';
                if (date >= startDate && date <= endDate) {
                    cssClass = 'selectedWeek';
                }
                return [true, cssClass];
            },
            beforeShow: function (input, inst) {
                var rect = input.getBoundingClientRect();
                setTimeout(function () {
                    inst.dpDiv.css({ left: rect.left - 14 });
                }, 0);
            }
        });

        $(weekInput).datepicker({
            beforeShow: function (input, inst) {
                var rect = input.getBoundingClientRect();
                setTimeout(function () {
                    inst.dpDiv.css({ left: rect.left - 14 });
                }, 0);
            }
        });

        $('#ui-datepicker-div')
            .on('mousemove', '.ui-datepicker-calendar tr', function () {
                $(this).find('td').addClass('aWeek');
            })
            .on('mouseleave', '.ui-datepicker-calendar tr', function () {
                $(this).find('td').removeClass('aWeek');
            });
    };

    this.nextWeek = function() {
        roll(7);
    };

    this.prevWeek = function() {
        roll(-7);
    };

    function roll(days) {
        startDate.setDate(startDate.getDate() + days);
        endDate.setDate(endDate.getDate() + days);
        showDates();
    }

    function showDates() {
        $(PrimeFaces.escapeClientId(startDateId)).val(startDate.getDate() + '.' + (startDate.getMonth() + 1) + '.' + startDate.getFullYear());
        $(PrimeFaces.escapeClientId(endDateId)).val(endDate.getDate() + '.' + (endDate.getMonth() + 1) + '.' + endDate.getFullYear());
        $(weekInput).val(startDate.toLocaleDateString(locale) + ' - ' + endDate.toLocaleDateString(locale));
    }

    function getLocale() {
        var locale = ((navigator.userAgent.toLowerCase().indexOf('chrome') !== -1)
            && (navigator.languages !== undefined))
            ? navigator.languages[0]
            : navigator.language || "en";

        return locale.substr(0, 2);
    }
}

function MonthSelector () {
    var firstDayOfMonthId = 'form:advancedPeriodSelector:monthSelector:firstDayOfMonth';
    var lastDayOfMonthId = 'form:advancedPeriodSelector:monthSelector:lastDayOfMonth';

    var locale = getLocale();

    var currentDate = $(PrimeFaces.escapeClientId(firstDayOfMonthId)).val().split('.');
    var currentDay = currentDate[0];
    var currentMonth = currentDate[1];
    var currentYear = currentDate[2];

    this.init = function() {
        $.datepicker.setDefaults($.datepicker.regional[((locale === 'en') ? '': locale)]);
        $("#filterMonthSelector").datepicker({
            inline: true,
            defaultDate: new Date(currentYear,currentMonth-1,currentDay),
            onChangeMonthYear: function(year, month){
                var firstDay = '01.'+month+'.'+year;
                var lastDay =  new Date(year, month, 0);
                var lastDayFormatted = lastDay.getDate()+'.'+month+'.'+year;
                $(PrimeFaces.escapeClientId(firstDayOfMonthId)).val(firstDay);
                $(PrimeFaces.escapeClientId(lastDayOfMonthId)).val(lastDayFormatted);
            }
        });
    };

    function getLocale() {
        var locale = ((navigator.userAgent.toLowerCase().indexOf('chrome') !== -1)
            && (navigator.languages !== undefined))
                ? navigator.languages[0]
                : navigator.language || "en";

        return locale.substr(0, 2);
    }
}