import express from 'express';
import cors from 'cors';
import { v4 as uuid } from 'uuid';
import path from 'path';
import { fileURLToPath } from 'url';

const app = express();
app.use(cors());
app.use(express.json());

const medicines = [];
const schedules = [];
const adherence = [];

app.get('/health', (_, res) => res.json({ ok: true }));

app.get('/patients/:patientId/medicines', (req, res) => {
  const { patientId } = req.params;
  const today = new Date().toISOString().slice(0, 10);
  const activeOnly = req.query.active === 'true';

  const data = medicines.filter((m) => {
    if (m.patientId !== patientId) return false;
    if (!activeOnly) return true;
    return m.startDate <= today && m.endDate >= today;
  });

  res.json(data);
});

app.post('/patients/:patientId/medicines', (req, res) => {
  const { patientId } = req.params;
  const payload = req.body;

  if (!payload.brandName || !payload.genericName || !payload.dose || !payload.startDate || !payload.endDate) {
    return res.status(400).json({ error: 'brandName, genericName, dose, startDate, endDate are required' });
  }

  const med = { id: uuid(), patientId, ...payload };
  medicines.push(med);
  res.status(201).json(med);
});

app.delete('/medicines/:medicineId', (req, res) => {
  const idx = medicines.findIndex((m) => m.id === req.params.medicineId);
  if (idx < 0) return res.status(404).json({ error: 'Not found' });

  const [removed] = medicines.splice(idx, 1);
  for (let i = schedules.length - 1; i >= 0; i--) {
    if (schedules[i].medicineId === removed.id) schedules.splice(i, 1);
  }
  res.status(204).send();
});

app.post('/patients/:patientId/schedules', (req, res) => {
  const { medicineId, period } = req.body;
  if (!medicineId || !period) {
    return res.status(400).json({ error: 'medicineId and period are required' });
  }

  const schedule = { id: uuid(), patientId: req.params.patientId, medicineId, period };
  schedules.push(schedule);
  res.status(201).json(schedule);
});

app.get('/patients/:patientId/periods/:period/medicines', (req, res) => {
  const { patientId, period } = req.params;
  const date = req.query.date || new Date().toISOString().slice(0, 10);

  const medIdsForPeriod = schedules
    .filter((s) => s.patientId === patientId && s.period === period)
    .map((s) => s.medicineId);

  const data = medicines.filter((m) =>
    m.patientId === patientId &&
    medIdsForPeriod.includes(m.id) &&
    m.startDate <= date &&
    m.endDate >= date
  );

  res.json(data);
});

app.post('/patients/:patientId/adherence/complete-period', (req, res) => {
  const { date, period } = req.body;
  if (!date || !period) return res.status(400).json({ error: 'date and period are required' });

  const log = {
    id: uuid(),
    patientId: req.params.patientId,
    date,
    period,
    status: 'TAKEN',
    completedAt: new Date().toISOString()
  };
  adherence.push(log);
  res.status(201).json(log);
});

app.get('/patients/:patientId/adherence/daily', (req, res) => {
  const { patientId } = req.params;
  const { date } = req.query;
  res.json(adherence.filter((a) => a.patientId === patientId && a.date === date));
});

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const webDir = path.resolve(__dirname, '../../web');
app.use(express.static(webDir));
app.get('*', (_, res) => res.sendFile(path.join(webDir, 'index.html')));

const port = process.env.PORT || 8080;
app.listen(port, () => {
  console.log(`Server running on port ${port}`);
});
