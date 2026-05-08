document.addEventListener("DOMContentLoaded", function () {
    var themeToggle = document.getElementById("theme-toggle");
    var savedTheme = localStorage.getItem("reservation-theme");
    var dateInput = document.getElementById("reservation-date");
    var checkInInput = document.getElementById("check-in");
    var checkOutInput = document.getElementById("check-out");
    var minCapacityInput = document.getElementById("min-capacity");
    var availabilityForm = document.getElementById("availability-form");
    var availableRooms = document.getElementById("available-rooms");
    var availabilityCount = document.getElementById("availability-count");
    var reservationForm = document.getElementById("reservation-form");
    var selectedRoomPanel = document.getElementById("selected-room");
    var roomIdInput = document.getElementById("room-id");
    var guestCountInput = document.getElementById("guest-count");
    var bookButton = document.getElementById("book-button");
    var bookingAlert = document.getElementById("booking-alert");
    var selectedRoom = null;

    if (savedTheme === "dark") {
        document.documentElement.dataset.theme = "dark";
    }

    updateThemeToggle();

    if (themeToggle) {
        themeToggle.addEventListener("click", function () {
            var isDark = document.documentElement.dataset.theme === "dark";
            document.documentElement.dataset.theme = isDark ? "light" : "dark";
            localStorage.setItem("reservation-theme", isDark ? "light" : "dark");
            updateThemeToggle();
        });
    }

    if (dateInput && !dateInput.value) {
        dateInput.value = today();
    }

    if (dateInput) {
        dateInput.addEventListener("change", function () {
            if (dateInput.value) {
                dateInput.form.submit();
            }
        });
    }

    if (!availabilityForm) {
        return;
    }

    checkInInput.value = checkInInput.value || (dateInput && dateInput.value ? dateInput.value : today());
    checkOutInput.value = checkOutInput.value || addDays(checkInInput.value, 1);
    guestCountInput.value = minCapacityInput.value;

    checkInInput.addEventListener("change", function () {
        if (!checkOutInput.value || checkOutInput.value <= checkInInput.value) {
            checkOutInput.value = addDays(checkInInput.value, 1);
        }
    });

    minCapacityInput.addEventListener("change", function () {
        guestCountInput.value = minCapacityInput.value;
    });

    availabilityForm.addEventListener("submit", function (event) {
        event.preventDefault();
        searchAvailability(true);
    });

    reservationForm.addEventListener("submit", function (event) {
        event.preventDefault();
        createReservation();
    });

    searchAvailability();

    function searchAvailability(keepAlert) {
        if (!keepAlert) {
            clearAlert();
        }
        clearSelection();

        if (!checkInInput.value || !checkOutInput.value || checkOutInput.value <= checkInInput.value) {
            renderEmpty("Choose a valid date range.");
            return;
        }

        setLoading(true);
        fetch("/api/rooms/available?" + new URLSearchParams({
            checkIn: checkInInput.value,
            checkOut: checkOutInput.value,
            minCapacity: minCapacityInput.value || "1"
        }))
            .then(parseResponse)
            .then(function (rooms) {
                renderRooms(rooms);
            })
            .catch(function (error) {
                renderEmpty(error.message || "Availability search failed.");
            })
            .finally(function () {
                setLoading(false);
            });
    }

    function createReservation() {
        if (!selectedRoom) {
            showAlert("Select a room first.", "error");
            return;
        }

        bookButton.disabled = true;
        showAlert("Booking room...", "info");

        fetch("/api/reservations", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify({
                roomId: Number(roomIdInput.value),
                guestId: Number(document.getElementById("guest-id").value),
                guestCount: Number(guestCountInput.value),
                checkInDate: checkInInput.value,
                checkOutDate: checkOutInput.value
            })
        })
            .then(parseResponse)
            .then(function (reservation) {
                showAlert("Reservation #" + reservation.id + " created for room " + reservation.roomNumber + ".", "success");
                searchAvailability();
                if (dateInput) {
                    dateInput.value = checkInInput.value;
                }
            })
            .catch(function (error) {
                showAlert(error.message || "Reservation failed.", "error");
                bookButton.disabled = false;
            });
    }

    function renderRooms(rooms) {
        availabilityCount.textContent = rooms.length + (rooms.length === 1 ? " room" : " rooms");
        if (!rooms.length) {
            renderEmpty("No rooms match this stay.");
            return;
        }

        availableRooms.innerHTML = "";
        rooms.forEach(function (room) {
            var card = document.createElement("article");
            card.className = "room-card";
            card.innerHTML =
                "<div>" +
                "<h3>" + escapeHtml(room.name) + " " + escapeHtml(room.number) + "</h3>" +
                "<p>" + escapeHtml(room.bedInfo) + " · up to " + room.maxCapacity + " guests</p>" +
                "</div>" +
                "<div class=\"room-rate\">" +
                "<strong>$" + Number(room.pricePerNight).toFixed(2) + "</strong>" +
                "<span>per night</span>" +
                "</div>" +
                "<button type=\"button\">Select</button>";
            card.querySelector("button").addEventListener("click", function () {
                selectRoom(room, card);
            });
            availableRooms.appendChild(card);
        });
    }

    function selectRoom(room, card) {
        selectedRoom = room;
        roomIdInput.value = room.id;
        guestCountInput.max = room.maxCapacity;
        guestCountInput.value = Math.min(Number(minCapacityInput.value || 1), room.maxCapacity);
        bookButton.disabled = false;
        clearAlert();

        document.querySelectorAll(".room-card-selected").forEach(function (node) {
            node.classList.remove("room-card-selected");
        });
        card.classList.add("room-card-selected");

        selectedRoomPanel.innerHTML =
            "<p class=\"selected-title\">" + escapeHtml(room.name) + " " + escapeHtml(room.number) + "</p>" +
            "<p>" + escapeHtml(room.bedInfo) + " · $" + Number(room.pricePerNight).toFixed(2) + " per night</p>" +
            "<p>" + nights(checkInInput.value, checkOutInput.value) + " nights · up to " + room.maxCapacity + " guests</p>";
    }

    function clearSelection() {
        selectedRoom = null;
        roomIdInput.value = "";
        bookButton.disabled = true;
        selectedRoomPanel.innerHTML = "<p class=\"empty-state\">Select an available room.</p>";
    }

    function renderEmpty(message) {
        availabilityCount.textContent = "0 rooms";
        availableRooms.innerHTML = "<p class=\"empty-state\">" + escapeHtml(message) + "</p>";
    }

    function setLoading(isLoading) {
        availabilityForm.querySelector("button").disabled = isLoading;
        if (isLoading) {
            availableRooms.innerHTML = "<p class=\"empty-state\">Searching rooms...</p>";
        }
    }

    function parseResponse(response) {
        return response.json().catch(function () {
            return {};
        }).then(function (body) {
            if (!response.ok) {
                throw new Error(body.message || "Request failed.");
            }
            return body;
        });
    }

    function showAlert(message, type) {
        bookingAlert.textContent = message;
        bookingAlert.className = "form-alert form-alert-" + type;
    }

    function clearAlert() {
        bookingAlert.textContent = "";
        bookingAlert.className = "form-alert";
    }

    function updateThemeToggle() {
        if (!themeToggle) {
            return;
        }

        var isDark = document.documentElement.dataset.theme === "dark";
        themeToggle.setAttribute("aria-pressed", String(isDark));
        themeToggle.querySelector(".theme-icon").textContent = isDark ? "☀" : "☾";
        themeToggle.querySelector(".theme-label").textContent = isDark ? "Light" : "Dark";
    }

    function today() {
        return new Date().toISOString().slice(0, 10);
    }

    function addDays(value, days) {
        var date = new Date(value + "T00:00:00");
        date.setDate(date.getDate() + days);
        return date.toISOString().slice(0, 10);
    }

    function nights(checkIn, checkOut) {
        return Math.max(1, Math.round((new Date(checkOut) - new Date(checkIn)) / 86400000));
    }

    function escapeHtml(value) {
        return String(value == null ? "" : value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
});
