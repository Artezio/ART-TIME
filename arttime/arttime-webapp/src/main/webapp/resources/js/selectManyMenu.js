$(document).ready(function() {
	clickUnbind();
});

function clickUnbind(){
	$(".ui-selectmanymenu .ui-selectlistbox-item").unbind('click');
}

function selectAll(componentId) {     
	$( "div[id$=':" + componentId + "'] li" ).each(function() {  
		if (! $(this).hasClass('ui-state-highlight')) {                           
			$( this ).find("div.ui-chkbox-box").trigger("click");            
		}
	});                           
}        

function selectNone(componentId) {     
	$( "div[id$=':" + componentId + "'] li.ui-state-highlight" ).each(function() {      
		$( this ).find("div.ui-chkbox-box").trigger( "click" );           
	});
}

function invertSelection(componentId) {                                                                                                                                               
	$( "div[id$=':" + componentId + "'] li" ).each(function() {
		$( this ).find("div.ui-chkbox-box").trigger( "click" );      
	});                                      
}
