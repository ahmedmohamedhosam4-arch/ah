const form = document.getElementById("doctorForm");
const message = document.getElementById("message");
const locationStatus = document.getElementById("locationStatus");
const popup = document.getElementById("popup");
const popupCode = document.getElementById("popupCode");
const closePopup = document.getElementById("closePopup");

let doctorLocation = null;

function showMessage(type, text) {
  message.className = type;
  message.textContent = text;
  message.style.display = "block";
}

function generateCode() {
  return Math.floor(100000 + Math.random() * 900000);
}

function getLocation() {
  locationStatus.textContent = "Requesting location...";

  if (!navigator.geolocation) {
    locationStatus.textContent = "❌ Geolocation not supported";
    return;
  }

  navigator.geolocation.getCurrentPosition(
    (pos) => {
      doctorLocation = pos.coords;
      locationStatus.textContent =
        `Location: ${doctorLocation.latitude.toFixed(6)}, ${doctorLocation.longitude.toFixed(6)}`;
    },
    (err) => {
      locationStatus.textContent = "❌ Could not get location. Allow access & enable GPS.";
      console.error(err);
    },
    { enableHighAccuracy: true, timeout: 10000 }
  );
}

form.addEventListener("submit", (e) => {
  e.preventDefault();

  const lectureName = document.getElementById("lectureName").value.trim();
  const hours = document.getElementById("hours").value || 0;
  const minutes = document.getElementById("minutes").value || 0;

  if (!lectureName) {
    showMessage("error", "⚠️ Enter lecture name");
    return;
  }

  if (!doctorLocation) {
    showMessage("error", "⚠️ Location not detected yet");
    return;
  }

  const code = generateCode();
  document.getElementById("lecturePass").value = code;

  const lectureData = {
    lectureName,
    code,
    duration: { hours, minutes },
    location: {
      lat: doctorLocation.latitude,
      lng: doctorLocation.longitude,
    },
    createdAt: new Date().toISOString(),
  };

  localStorage.setItem("currentLecture", JSON.stringify(lectureData));

  popupCode.textContent = code;
  popup.classList.remove("hidden");

  showMessage("success", "Lecture Created Successfully ✔️");
});

closePopup.addEventListener("click", () => {
  popup.classList.add("hidden");
});

document.getElementById("logoutBtn").addEventListener("click", () => {
  localStorage.clear();
  window.location.href = "home.html";
});

/* ============================
   AUTO REQUEST LOCATION
============================ */
window.addEventListener("load", () => {
  getLocation();

  // Apply saved Dark Mode
  if (localStorage.getItem("darkMode") === "on") {
    document.documentElement.classList.add("dark-mode");
  }
});

/* ============================
       SEATING MENU LOGIC
============================ */

const seatingBtn = document.getElementById("seatingBtn");
const seatingMenu = document.getElementById("seatingMenu");

// Open/Close dropdown
seatingBtn.addEventListener("click", (e) => {
  e.stopPropagation();
  seatingMenu.classList.toggle("hidden");
});

// Close if clicking outside
document.addEventListener("click", (e) => {
  if (!seatingMenu.classList.contains("hidden")) {
    if (!seatingMenu.contains(e.target) && e.target !== seatingBtn) {
      seatingMenu.classList.add("hidden");
    }
  }
});

/* ============================
          MODAL MAKER
============================ */
function createModal(html) {
  const backdrop = document.createElement("div");
  backdrop.className = "modal-backdrop";

  backdrop.innerHTML = `
    <div class="modal">${html}</div>
  `;

  document.body.appendChild(backdrop);

  // Click outside = close modal
  backdrop.addEventListener("click", (e) => {
    if (e.target === backdrop) backdrop.remove();
  });

  return backdrop;
}

/* ============================
   EDIT LECTURE DURATION
============================ */
document.getElementById("editDurationBtn").addEventListener("click", () => {
  seatingMenu.classList.add("hidden");

  const data = JSON.parse(localStorage.getItem("currentLecture")) || null;
  const currH = data?.duration?.hours || 0;
  const currM = data?.duration?.minutes || 0;

  const html = `
    <h3>Edit Lecture Duration</h3>
    <div class="row">
      <label>Hours</label>
      <input type="number" id="modalHours" min="0" max="5" value="${currH}">
    </div>
    <div class="row">
      <label>Minutes</label>
      <input type="number" id="modalMinutes" min="0" max="59" value="${currM}">
    </div>
    <div class="actions">
      <button class="btn-secondary" id="cancelDur">Cancel</button>
      <button class="btn-primary" id="saveDur">Save</button>
    </div>
  `;

  const modal = createModal(html);

  modal.querySelector("#cancelDur").onclick = () => modal.remove();

  modal.querySelector("#saveDur").onclick = () => {
    const h = Number(modal.querySelector("#modalHours").value);
    const m = Number(modal.querySelector("#modalMinutes").value);

    const lecture = JSON.parse(localStorage.getItem("currentLecture"));
    lecture.duration = { hours: h, minutes: m };

    localStorage.setItem("currentLecture", JSON.stringify(lecture));

    modal.remove();
    alert("Lecture duration updated ✔ ");
  };
});

/* ============================
           DARK MODE
============================ */
document.getElementById("toggleDarkBtn").addEventListener("click", () => {
  seatingMenu.classList.add("hidden");

  const html = `
    <h3>Toggle Dark Mode</h3>
    <p>Switch between light and dark mode.</p>
    <div class="actions">
      <button class="btn-secondary" id="cancelDM">Cancel</button>
      <button class="btn-primary" id="applyDM">Apply</button>
    </div>
  `;

  const modal = createModal(html);

  modal.querySelector("#cancelDM").onclick = () => modal.remove();

  modal.querySelector("#applyDM").onclick = () => {
    const root = document.documentElement;

    if (root.classList.contains("dark-mode")) {
      root.classList.remove("dark-mode");
      localStorage.setItem("darkMode", "off");
    } else {
      root.classList.add("dark-mode");
      localStorage.setItem("darkMode", "on");
    }

    modal.remove();
  };
});

/* ============================
      CHANGE PASSWORD
============================ */
document.getElementById("changePassBtn").addEventListener("click", () => {
  seatingMenu.classList.add("hidden");

  const html = `
    <h3>Change Doctor Password</h3>
    <div class="row">
      <label>New Password</label>
      <input type="password" id="newPass">
    </div>
    <div class="actions">
      <button class="btn-secondary" id="cancelP">Cancel</button>
      <button class="btn-primary" id="saveP">Save</button>
    </div>
  `;

  const modal = createModal(html);

  modal.querySelector("#cancelP").onclick = () => modal.remove();

  modal.querySelector("#saveP").onclick = () => {
    const newPass = modal.querySelector("#newPass").value.trim();

    if (!newPass) {
      alert("Password cannot be empty");
      return;
    }

    localStorage.setItem("doctorPassword", newPass);
    modal.remove();
    alert("Password changed ✔");
  };
});
