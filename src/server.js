const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const path = require('path');
const multer = require('multer');
const xlsx = require('xlsx');
const { initDb } = require('./database');
const datadogService = require('./services/datadogService');
const aiService = require('./services/aiService');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(bodyParser.json());
app.use(express.static(path.join(__dirname, '../public')));

// Upload config
const upload = multer({ storage: multer.memoryStorage() });

// Initialize DB
initDb();

// Routes
app.post('/api/generate-datadog', async (req, res) => {
    try {
        const { prompt } = req.body;
        if (!prompt) return res.status(400).json({ error: 'Prompt is required' });

        const { getHistory, saveHistory } = require('./database');

        // Check cache
        const cachedResult = await getHistory(prompt);
        if (cachedResult) {
            console.log('Cache hit for prompt:', prompt);
            return res.json({ result: cachedResult, cached: true });
        }

        const result = await aiService.generateDatadogQuery(prompt);

        // Save to cache
        await saveHistory(prompt, result);

        res.json({ result, cached: false });
    } catch (error) {
        console.error('Error generating Datadog query:', error);
        res.status(500).json({ error: 'Internal Server Error' });
    }
});

app.post('/api/generate-report', async (req, res) => {
    try {
        const { type, data } = req.body;
        // Mock report generation for now
        const report = `Report for ${type} generated at ${new Date().toISOString()}`;
        res.json({ report });
    } catch (error) {
        console.error('Error generating report:', error);
        res.status(500).json({ error: 'Internal Server Error' });
    }
});

app.post('/api/analyze-file', upload.single('file'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ error: 'No file uploaded' });
        }

        const { customPrompt } = req.body;
        let data;

        if (req.file.originalname.endsWith('.txt') || req.file.mimetype === 'text/plain') {
            data = req.file.buffer.toString('utf-8');
        } else {
            const workbook = xlsx.read(req.file.buffer, { type: 'buffer' });
            const sheetName = workbook.SheetNames[0];
            const sheet = workbook.Sheets[sheetName];
            data = xlsx.utils.sheet_to_json(sheet);
        }

        if (!data || (Array.isArray(data) && data.length === 0) || (typeof data === 'string' && data.trim().length === 0)) {
            return res.status(400).json({ error: 'File is empty or invalid' });
        }

        const analysis = await aiService.analyzeSpreadsheet(data, customPrompt);
        res.json({ report: analysis });

    } catch (error) {
        console.error('Error analyzing file:', error);
        res.status(500).json({ error: 'Internal Server Error' });
    }
});

app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});
