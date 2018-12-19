function fixInputHours(changes, source) {
    for (var i = changes.length - 1; i >= 0; i--) {
        var value = changes[i][3];
        value = value.replace(/[^\d\.]/g, '');
        if (value == '.') {
            value = '';
        }
        var valueParts = value.split('.');
        if (valueParts.length > 2) {
            value = valueParts[0] + '.' + valueParts.slice(1, valueParts.length).join('');
        }
        changes[i][3] = value;
    }
}

Handsontable.hooks.add('beforeChange', fixInputHours);
$(document).on('mouseover.htCommment', function () {
    $('.htCommentTextArea').attr('maxlength', 255).on('keyup', function () {
        var lineBreaksCount = this.value.split(/\n/).length;
        if (lineBreaksCount > 1) {
            var maxlength = $(this).attr('maxlength') - lineBreaksCount;
            if (this.value.length > maxlength) {
                this.value = this.value.substring(0, maxlength);
            }
        }
    });
});
