$(document).ready(function() {
    $('.date').each(function() {
        var d = $(this).text()
        $(this).text(new Date(d))
    })
})