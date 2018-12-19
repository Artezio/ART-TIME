window.FilterUtils = window.FilterUtils || {
    // Remove 'Select All - Deselect All' behaviour and allow 'Deselect' only
    replaceToggler: function(filter, clearLabel) {
        filter.togglerBox
            .off('click.selectCheckboxMenu')
            .on('click.selectCheckboxMenu', function() {
                filter.uncheckAll();
            })
            .prop('title', clearLabel);
    },

    // Default label is 'All' as specified in attribute 'label' only when no items were selected during page load,
    // but when there were selected items, it becomes Item1,Item2,... without 'All' label. Fix:
    replaceDefaultLabel: function(filter, defaultText) {
        if (filter !== undefined) {
            filter.defaultLabel = defaultText;
        }
    },

    // Adds 'Clear' clickable icon to filter input field instead of default 'Search' icon
    addClearableInput: function(filter, clearLabel) {
        filter.filterInput.addClass("clearable-input");
        filter.filterInput.on('keyup.selectCheckboxMenu',
            function(){
                var searchField = $(this)
                var searchIcon = filter.filterInputWrapper.children(".ui-icon");
                searchIcon.off('click').prop('title','');
                if (searchField.val().length > 0) {
                    filter.filterInputWrapper.addClass("has-clear-icon");
                    searchIcon.on('click', function() {
                        searchField.val("").trigger("keyup");
                    }).prop('title', clearLabel);
                } else {
                    filter.filterInputWrapper.removeClass("has-clear-icon");
                }
            });
    },

    addHighlightingFoundTerms: function() {
        PrimeFaces.widget.SelectCheckboxMenu.prototype.filter = function(e) {
            var f = this.cfg.caseSensitive ? $.trim(e) : $.trim(e).toLowerCase();
            if (f === "") {
                this.itemContainer
                    .children("li.ui-selectcheckboxmenu-item")
                    .filter(function() {
                        var $this = $(this);
                        if (!$this.is(':hidden')) {
                            var $label = $this.children('label');
                            if ($label.has('span')) {
                                $label.html($label.text());
                            }
                            return false;
                        }
                        return true;
                    })
                    .show()
            } else {
                for (var b = 0; b < this.labels.length; b++) {
                    var a = this.labels.eq(b), d = a.parent(), c = this.cfg.caseSensitive ? a.text() : a.text().toLowerCase();
                    if (this.filterMatcher(c, f)) {
                        a.html(a.text().replace(new RegExp('(' + f + ')', 'ig'), '<span style="font-weight: bold">$1</span>'));
                        d.show()
                    } else {
                        var $label = d.find('label');
                        $label.html($label.text());
                        d.hide()
                    }
                }
            }
            if (this.cfg.scrollHeight) {
                if (this.itemContainer.height() < this.cfg.initialHeight) {
                    this.itemContainerWrapper.css("height", "auto")
                } else {
                    this.itemContainerWrapper.height(this.cfg.initialHeight)
                }
            }
            this.updateToggler()
        }
    },

    addAutofocusOnSearchField: function () {
        PrimeFaces.widget.SelectCheckboxMenu.prototype.postShow = function() {
            if (this.filterInput) {
                var input = this.filterInput;
                setTimeout(function() {
                    input.focus();
                }, 120);
                setTimeout(function() {
                    input.focus();
                }, 0);
            }
            if (this.cfg.onShow) {
                this.cfg.onShow.call(this);
            }
        };
    },

    reconfigureFilter: function(selector, allSelectedLabel, clearButtonLabel, clearTextFieldLabel) {
        if (PrimeFaces.widgets[selector]) {
            var filter = PF(selector);
            if (filter !== undefined) {
                this.replaceDefaultLabel(filter, allSelectedLabel);
                this.replaceToggler(filter, clearButtonLabel);
                this.addClearableInput(filter, clearTextFieldLabel);
            }
        }
    }

};