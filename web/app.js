const patientId = 'parent-001';
const apiBase = window.location.origin;

const periods = ['MORNING', 'NOON', 'NIGHT'];
const periodLabels = { MORNING: 'Morning', NOON: 'Noon', NIGHT: 'Night' };
const periodClass = { MORNING: 'morning', NOON: 'noon', NIGHT: 'night' };

const parentTab = document.getElementById('parentTab');
const adminTab = document.getElementById('adminTab');
const parentSection = document.getElementById('parentSection');
const adminSection = document.getElementById('adminSection');
const periodsEl = document.getElementById('periods');
const periodDetailsEl = document.getElementById('periodDetails');
const medicineListEl = document.getElementById('medicineList');
const medicineForm = document.getElementById('medicineForm');

let periodCompletion = JSON.parse(localStorage.getItem('periodCompletion') || '{}');

function today() {
  return new Date().toISOString().slice(0, 10);
}

function switchTab(isParent) {
  parentTab.classList.toggle('active', isParent);
  adminTab.classList.toggle('active', !isParent);
  parentSection.classList.toggle('hidden', !isParent);
  adminSection.classList.toggle('hidden', isParent);
}

parentTab.addEventListener('click', () => switchTab(true));
adminTab.addEventListener('click', () => {
  switchTab(false);
  renderMedicineList();
});

function renderPeriods() {
  const dayKey = today();
  periodsEl.innerHTML = periods.map((p) => {
    const done = periodCompletion[`${dayKey}-${p}`] ? 'Completed' : 'Pending';
    return `
      <article class="period-card ${periodClass[p]}">
        <strong>${periodLabels[p]}</strong>
        <span>${done}</span>
        <button data-period="${p}">Open</button>
      </article>
    `;
  }).join('');

  periodsEl.querySelectorAll('button').forEach((btn) => {
    btn.addEventListener('click', () => openPeriod(btn.dataset.period));
  });
}

async function api(path, options = {}) {
  const res = await fetch(`${apiBase}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) throw new Error(await res.text());
  if (res.status === 204) return null;
  return res.json();
}

async function openPeriod(period) {
  const meds = await api(`/patients/${patientId}/periods/${period}/medicines?date=${today()}`);
  if (!meds.length) {
    periodDetailsEl.classList.remove('hidden');
    periodDetailsEl.innerHTML = `<h3>${periodLabels[period]}</h3><p>No active medicines for this period.</p>`;
    return;
  }

  let idx = 0;

  const renderCard = () => {
    const med = meds[idx];
    periodDetailsEl.classList.remove('hidden');
    periodDetailsEl.innerHTML = `
      <h3>${periodLabels[period]} medicine (${idx + 1}/${meds.length})</h3>
      <div class="medicine-item">
        <div>
          <div><strong>${med.brandName}</strong></div>
          <div class="small">${med.genericName}</div>
          <div>Dose: ${med.dose}</div>
          <div class="small">${med.startDate} to ${med.endDate}</div>
        </div>
        ${med.imageUrl ? `<img src="${med.imageUrl}" width="64" height="64" alt="${med.brandName}"/>` : ''}
      </div>
      <div class="nav-row">
        <button id="prevBtn" ${idx === 0 ? 'disabled' : ''}>Previous</button>
        <button id="nextBtn" ${idx === meds.length - 1 ? 'disabled' : ''}>Next</button>
      </div>
      <button id="completeBtn">I have completed the medicine for the selected period</button>
    `;

    document.getElementById('prevBtn').onclick = () => { idx--; renderCard(); };
    document.getElementById('nextBtn').onclick = () => { idx++; renderCard(); };
    document.getElementById('completeBtn').onclick = async () => {
      await api(`/patients/${patientId}/adherence/complete-period`, {
        method: 'POST',
        body: JSON.stringify({ date: today(), period }),
      });
      periodCompletion[`${today()}-${period}`] = true;
      localStorage.setItem('periodCompletion', JSON.stringify(periodCompletion));
      renderPeriods();
      periodDetailsEl.innerHTML = `<p>✅ Marked ${periodLabels[period]} as completed.</p>`;
    };
  };

  renderCard();
}

medicineForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const formData = new FormData(medicineForm);
  const selectedPeriods = [...document.querySelectorAll('input[name="period"]:checked')].map((el) => el.value);

  if (selectedPeriods.length === 0) {
    alert('Please select at least one period.');
    return;
  }

  const body = {
    brandName: formData.get('brandName') || document.getElementById('brandName').value,
    genericName: formData.get('genericName') || document.getElementById('genericName').value,
    dose: formData.get('dose') || document.getElementById('dose').value,
    imageUrl: formData.get('imageUrl') || document.getElementById('imageUrl').value,
    startDate: document.getElementById('startDate').value,
    endDate: document.getElementById('endDate').value,
  };

  const med = await api(`/patients/${patientId}/medicines`, {
    method: 'POST',
    body: JSON.stringify(body),
  });

  await Promise.all(selectedPeriods.map((period) =>
    api(`/patients/${patientId}/schedules`, {
      method: 'POST',
      body: JSON.stringify({ medicineId: med.id, period }),
    })
  ));

  medicineForm.reset();
  document.getElementById('startDate').value = today();
  document.getElementById('endDate').value = today();
  renderMedicineList();
});

async function renderMedicineList() {
  const meds = await api(`/patients/${patientId}/medicines?active=false`);
  medicineListEl.innerHTML = meds.map((m) => `
    <div class="medicine-item">
      <div>
        <strong>${m.brandName}</strong>
        <div class="small">${m.genericName}</div>
        <div class="small">${m.startDate} to ${m.endDate}</div>
      </div>
      <button class="danger" data-id="${m.id}">Delete</button>
    </div>
  `).join('') || '<p>No medicines yet.</p>';

  medicineListEl.querySelectorAll('button').forEach((btn) => {
    btn.onclick = async () => {
      await api(`/medicines/${btn.dataset.id}`, { method: 'DELETE' });
      renderMedicineList();
      renderPeriods();
    };
  });
}

function setDefaultDates() {
  document.getElementById('startDate').value = today();
  const d = new Date();
  d.setDate(d.getDate() + 15);
  document.getElementById('endDate').value = d.toISOString().slice(0, 10);
}

if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => navigator.serviceWorker.register('/sw.js'));
}

setDefaultDates();
renderPeriods();
