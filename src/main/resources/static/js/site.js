document.addEventListener("DOMContentLoaded", function () {
    var dateInput = document.getElementById("reservation-date");
    if (!dateInput) {
        return;
    }

    if (!dateInput.value) {
        dateInput.value = new Date().toISOString().slice(0, 10);
    }

    dateInput.addEventListener("change", function () {
        if (dateInput.value) {
            dateInput.form.submit();
        }
    });
});
