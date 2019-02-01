$(document).ready(function(){
    $('body').on('mousedown', '.ui-autocomplete-panel', function(event) { event.stopImmediatePropagation();});
});