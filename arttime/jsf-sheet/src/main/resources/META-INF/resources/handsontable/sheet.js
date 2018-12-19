/*
 * The MIT License (MIT)
 * Copyright (c) 2013 Lassiter Consulting Group, LLC
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Sheet component widget as Primefaces extension.
 */

PrimeFaces.widget.Sheet = PrimeFaces.widget.BaseWidget.extend({	
	
	// flag tracking whether or not an update ajax event needs fired after a select cell
	updated: false,
	// flag tracking whether a filter event needs fired after a focusin
	filterChanged: false,	
	
	// initialize the component
	init: function(cfg) {		
		var $this = this;		
		
		this._super(cfg);
		// store off jquery wrappers 
		this.sheetDiv = $(this.jqId);
		this.tableDiv = $(this.jqId + '_tbl');
		this.dataInput = $(this.jqId + '_input');
		this.selectionInput = $(this.jqId + '_selection');
		this.commentInput = $(this.jqId + '_comment');
		this.sortByInput = $(this.jqId + '_sortby');
		this.sortOrderInput = $(this.jqId + '_sortorder');
		this.focusInput = $(this.jqId + '_focus');
		
		for (var i = 0; i < $this.cfg.columns.length; i++) {
			var key = $this.id + ".width.column_" + i;
			if (localStorage.getItem(key)) {
				var width = localStorage.getItem(key); 
				$this.cfg.columns[i].width = width;
			}
		}
		// create table		
		this.setupHandsonTable();				
	},
		
	// true if sheet has assigned behavior, otherwise false
	hasBehavior: function(event) {
		if (this.cfg.behaviors) {
			return this.cfg.behaviors[event] != undefined;			
		}
		return false;
	},
	
	// fired when a filter input is edited. firenow indicates the filter event should be fired immediately (select)
	filterchange: function(sheet, col, v, firenow) {
		$(sheet.jqId + '_filter_' + col).val(v);
		sheet.filterChanged = true;
		
		if (firenow) {
			if (sheet.hasBehavior('filter')) {
				sheet.filterChanged = false;
				sheet.cfg.behaviors['filter'].call(this, 'filter');
			}
		}
	},

	// fired when a sortable column is clicked
	sortClick: function(sheet, e, col) {
        if ($(e.target).is(':not(th,span,div)')) return;
        var sc = sheet.sortByInput.val();
        if (col == sc) {
            var so = sheet.sortOrderInput.val();
            sheet.sortOrderInput.val((so == 'ascending' ? 'descending' : 'ascending'));
        } else {
            sheet.sortOrderInput.val('ascending');
            sheet.sortByInput.val(col);
        }
		if (sheet.hasBehavior('sort'))
			sheet.cfg.behaviors['sort'].call(this, 'sort');	                		
	},
	
	// eat enter keys for filter inputs so they do not submit form 
	keyDown: function(sheet, e) {
		e.stopImmediatePropagation();
		var key = e.which,
		keyCode = $.ui.keyCode;
		if ((key === keyCode.ENTER || key === keyCode.NUMPAD_ENTER)) {
			e.preventDefault();
		}
	},
	
	// again, eat enter key.  but also fire filter event on enter
	keyUp: function(sheet, e) {
		e.stopImmediatePropagation();
		var key = e.which,
		keyCode = $.ui.keyCode;
		if ((key === keyCode.ENTER || key === keyCode.NUMPAD_ENTER)) {
			$(e.target).change();
			if (sheet.hasBehavior('filter')) {
				sheet.filterChanged = false;
				sheet.cfg.behaviors['filter'].call(this, 'filter');
			}
			e.preventDefault();
		}
	},
	
	// keep track of focused filter input. if previous filter altered, fire filter event
	filterFocusIn: function(sheet, inp) {
        sheet.focusInput.val($(inp).attr('id'));
		if (sheet.filterChanged && sheet.hasBehavior('filter')) {
			sheet.filterChanged = false;
			sheet.cfg.behaviors['filter'].call(this, 'filter');	
		}
	},
	
	// remove focused filter tracking when tabbing off
	filterFocusOut: function(sheet, inp) {
		sheet.focusInput.val(null);		
	},
	
	// setup the handson table
	setupHandsonTable: function() {
		var $this = this;
		var commentDelta = {};
		var initialized = false;		
		var options = {
			data: $this.cfg.data,
            maxRows: $this.cfg.data.length,
			colHeaders: $this.cfg.colHeaders,
			rowHeaders: $this.cfg.rowHeaders,
            manualColumnResize: $this.cfg.manualColumnResize,
			columns: $this.cfg.columns,
			currentRowClassName: 'currentRow',
		    currentColClassName: 'currentCol',
			stretchH: $this.cfg.stretchH || 'all',
			comments: true,
	        enterMoves: {
	            row: 0,
	            col: 1
	        },
	        cell : $this.cfg.comments,
	        cellRenderer: function (instance, td, row, col, prop, value, cellProperties) {
	        	if (cellProperties.escape === false) {
	        		Handsontable.renderers.HtmlRenderer.apply(this, arguments);
	        	} else {	        		        	
	        		Handsontable.TextCell.renderer.apply(this, arguments);	        		
	        	}	    			    		
	    		
	        	var styleClass = '';
	    		// append row style (if we have one)
	    		var rowClass = $this.cfg.rowStyles[row];
	    		if (rowClass) 
	    			styleClass = rowClass;	    		
	    		// append cell style (if we have one)
	            var cellClass = $this.cfg.styles['r' + row + '_c' + col];
	            if (cellClass)
	            	styleClass = styleClass.concat(' ').concat(cellClass);
	            // check for errors
	    		var badmessage = $this.cfg.errors['r' + row + '_c' + col];
	    		if (badmessage) {
	    			styleClass = styleClass.concat(' ui-message-error');
	    			td.innerHTML = "<span class='ui-sheet-error' title='" + badmessage + "'><span class='ui-outputlabel-rfi'>*</span>" + value + "</span>";
	    		}		
	    		// every other row highlighting
	    		if (row % 2 == 1) styleClass = styleClass.concat(' ui-datatable-odd');
	    		td.className = td.className.concat(' ').concat(styleClass);
	    		
	    		var passThroughAttrs = $this.cfg.passThroughAttrs['r' + row + '_c' + col];
	    		if (passThroughAttrs) {	    			
		    		passThroughAttrs.split(";").forEach(function (item) {
		    			var entry = item.split(":");	    			
		    			td.setAttribute(entry[0], entry[1]);
		    		});
	    		}	    			    			    	
	        },
	        cells: function (row, col, prop) {
	            var cp = { };
		        cp.renderer = this.cellRenderer;
		        var readonly = $this.cfg.readOnly['r' + row + '_c' + col];
		        if (readonly) cp.readOnly = true;
	            return cp;
	        },
	        contextMenu: {	            
	            items: {
	            	"commentsAddEdit": {
	            		name: function () {
	            	        var hasComment = Handsontable.Comments.checkSelectionCommentsConsistency();
	            	        return hasComment ? getInternationalizedText('contextMenu.editComment') : getInternationalizedText('contextMenu.addComment');

	            	      },	            			            	      
	            		disabled: function () {
	            			var cell = this.getSelectedRange().from;	            			
	            			var readonly = $this.cfg.readOnly['r' + cell.row + '_c' + cell.col] === 'true';
	            			return readonly;
	            		}	           			            	    
	            	},
	            	"commentsRemove": {
	            		name : getInternationalizedText('contextMenu.removeComment'),
	            		disabled: function () {
	            			var cell = this.getSelectedRange().from;
	            			var readonly = $this.cfg.readOnly['r' + cell.row + '_c' + cell.col] === 'true';	            			        		  
	            			var hasComment = Handsontable.Comments.checkSelectionCommentsConsistency();	            			
	            			return readonly || !hasComment;
  		  
	            		},
	              	  	callback: function (key, selection, event) {
	              	  		var row = selection.start.row;
	              	  		var col = selection.start.col;
	              	  		Handsontable.Comments.removeComment(row, col);	              	  		
	              	  		commentDelta['r' + row + '_c' + col] = [row, col, key];
	              	  		$this.commentInput.val(JSON.stringify(commentDelta));
                            $this.tableDiv.trigger('change');
	              	  	}
	            	}	              	              
	            }
	        },	        
	        
	        afterInit: function() {
	        	initialized = true;	        	
	        }, 
	        afterSetCellMeta: function (row, col, key, val) {	        	
	        	if (initialized && key === 'comment') {
	        		val = val.split(/\r\n|\n\r|\n|\r/g);
	        		commentDelta['r' + row + '_c' + col] = [row, col, key, val];
	        		$this.commentInput.val(JSON.stringify(commentDelta));	        		 	        		
	            }	            
	        },	        
	        afterChange: function (change, source) {
	            if (source === 'loadData') {
	                return;
	            }
	            for (var i = 0; i < change.length; i++) {
	                $this.cfg.delta['r' + change[i][0] + '_c' + change[i][1]] = change[i];
	            }
	            $this.dataInput.val(JSON.stringify($this.cfg.delta)).change();
	            $this.updated = true;
	        },
	        afterSelectionEnd: function (r, c, r2, c2) {
	            var sel = [r, c, r2, c2];
	            $this.selectionInput.val(JSON.stringify(sel));
	            if ($this.updated) {
	                $this.updated = false;
	    			if ($this.hasBehavior('change'))
	    				$this.cfg.behaviors['change'].call(this, 'change');
	            } else {
	    			if ($this.hasBehavior('cellSelect'))
	    				$this.cfg.behaviors['cellSelect'].call(this, 'cellSelect');	            	
	            }
	        },
	        afterDeselect: function () {
	            if ($this.updated) {
	                $this.updated = false;
	    			if ($this.hasBehavior('change'))
	    				$this.cfg.behaviors['change'].call(this, 'change');	                
	            }
	        },
	        afterColumnResize: function (col, newSize) {
	        	localStorage.setItem($this.id + ".width.column_" + col, newSize);
	        },
	        afterGetColHeader: function (col, TH) {
	        	// handle sorting
	            var sortable = $this.cfg.sortable[col];
	            var styleClass = $this.cfg.columns[col]['headerStyleClass'];	           
	            $(TH).addClass(styleClass);
	            
	            if (sortable) {
	                $(TH).find('.relative .ui-sortable-column-icon').remove();
	                var sortCol = $this.sortByInput.val();
	                var sortOrder = $this.sortOrderInput.val();
	                var iconclass = 'ui-sortable-column-icon ui-icon ui-icon ui-icon-carat-2-n-s ';
	                if (sortCol == col) {
	                    iconclass = iconclass + (sortOrder == 'ascending' ? 'ui-icon-triangle-1-n' : 'ui-icon-triangle-1-s');
	                    $(TH).addClass('ui-state-active');
	                } else {
	                	$(TH).removeClass('ui-state-active');
	                }
	                $(TH).find('.relative').append("<span class='" + iconclass + "'></span>");
	                $(TH).addClass('ui-sortable');
	                $(TH).unbind('click').click(function (e) {
	                	$this.sortClick($this, e, col);
	                });
	            } else {
                	$(TH).removeClass('ui-state-active');
                }
	            
	            // handle filtering
	            var f = $this.cfg.filters[col];
	            if (f != 'false') {
	                $(TH).find('.handson-filter').remove();
	                var v = $($this.jqId + '_filter_' + col).val();
	                if (f == 'true') {
	                    $(TH).append('<span class="handson-filter"><input type="text" id="frmMain:tblBooks_f' + col + 
	                    		'" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all" role="textbox" aria-disabled="false" aria-readonly="false" aria-multiline="false" value="' + v + '" /></span>');
	                    $(TH).find('input')
	                    	.change( function() { $this.filterchange($this, col, this.value, false) } )
	                    	.keydown(function(e) { $this.keyDown($this, e) })
	                    	.keyup(function(e) { $this.keyUp($this, e) })
	                    	.focusin(function() { $this.filterFocusIn($this, this) })
	                    	.focusout(function() { $this.filterFocusOut($this, this) });
	                } else {
	                    $(TH).append('<span class="handson-filter"><select id="frmMain:tblBooks_f' + col + 
	                    		'" class="ui-column-filter ui-widget ui-state-default ui-corner-left" ></select></span>');
	                    var s = $(TH).find('select');
	                    for (var i = 0; i < f.length; i++) {
	                        s.append('<option value="' + f[i].value + '"' + (f[i].value == v ? ' selected="selected"' : '') + '>' + f[i].label + '</option>');
	                    }
	                    $(TH).find('select')
	                    	.change( function() { $this.filterchange($this, col, this.value, true) } )
	                    	.keydown(function(e) { $this.keyDown($this, e) })
	                    	.keyup(function(e) { $this.keyUp($this, e) })
	                    	.focusin(function() { $this.filterFocusIn($this, this) })
	                    	.focusout(function() { $this.filterFocusOut($this, this) });
	                }
	            }
	        }
		};
		
		if ($this.cfg.fixedColumnsLeft)
			options.fixedColumnsLeft = $this.cfg.fixedColumnsLeft;
		
		if ($this.cfg.fixedRowsTop)
			options.fixedRowsTop = $this.cfg.fixedRowsTop;
		
		if ($this.cfg.height)
			options.height = $this.cfg.height;

		if ($this.cfg.currentColClassName)
			options.currentColClassName = $this.cfg.currentColClassName;

		if ($this.cfg.currentRowClassName)
			options.currentRowClassName = $this.cfg.currentRowClassName;
			
		$this.tableDiv.handsontable(options);
		$this.ht = $this.tableDiv.data('handsontable');
		//Check if data exist.  If not insert No Records Found message
		if (options.data.length == 0) {
			$this.tableDiv.find('tbody').html("<tr><td colspan='" + options.columns.length + "'>" + $this.cfg.emptyMessage  + "</td></tr>")
		} 
		
	    var selval = $this.selectionInput.val();
	    if (selval && selval.length > 0) {
	        var sel = JSON.parse(selval);
	        $this.ht.selectCell(sel[0], sel[1], sel[2], sel[3], true);
	    }
	    var focusId = $this.focusInput.val();
	    if (focusId && focusId.length > 0) {
	    	focusId = focusId.replace(":", "\\:");
	    	// for some reason does not work when focused immediately, dom node hasn't attached
	    	setTimeout(function() {$('#' + focusId).focus() }, 100);
	    }
	}	
});

var i18n = {
		"en": {
			"contextMenu.editComment" : "Edit Comment",
			"contextMenu.addComment" : "Add Comment",
			"contextMenu.removeComment" : "Remove Comment"
		},
		"ru": {
			"contextMenu.editComment" : "Редактировать комментарий",
			"contextMenu.addComment" : "Добавить комментарий",
			"contextMenu.removeComment" : "Удалить комментарий"
		}
	};

function getInternationalizedText(key) {
	var lang = getUserLanguage();
	if (window.navigator && i18n[lang]) {
		return getText(key, lang);
	} else {		
		return getText(key, "en");
	}
}

function getUserLanguage() {
	// BUGFIX: ATIME-1048 (v3.47), Evgeny.Shestakov@artezio.ru
	// Bug source: When browsing with Android 4, stock browser / Windows Phone 10, Edge browser,
	// userAgent string contains 'chrome' substring but navigator.languages is not defined
	// Solution: check whether navigator.languages is defined before accessing its [0] element
	var language = ((navigator.userAgent.toLowerCase().indexOf('chrome') !== -1) && (navigator.languages !== undefined))
				? navigator.languages[0]
				: (window.navigator.language || window.navigator.userLanguage);

    if (language) {
        return language.substring(0, 2);
    }
    return "en";
}

function getText(key, lang) {
	return (i18n[lang][key])
		? i18n[lang][key]
		: key;	        
}