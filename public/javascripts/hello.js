$(function() {
    $("#showItemForm").click(function(e) {
        e.preventDefault();
        $("#itemFormContainer").toggle('slow');
    });

    var hasError = $("#itemFormContainer .form-group").hasClass("has-error")
    if (hasError) {
        $("#itemFormContainer").show();
    }
});