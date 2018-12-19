function showTitlesInsideSheet() {				
	$("td[class*='tdLongTextContainer']").bind('mouseenter', function(){
		var $this = $(this);		    					    		
		if(this.offsetWidth < this.scrollWidth && !$this.attr('title')){		    							    		
			$this.attr('title', $this.text());
		}
	});
}