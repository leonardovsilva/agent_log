const sqlite3 = require('sqlite3').verbose();
const path = require('path');

const isPkg = typeof process.pkg !== 'undefined';
// Check if running in Electron (using userAgent or process.versions.electron)
const isElectron = process.versions.electron !== undefined;

let dbPath;
if (isPkg) {
    dbPath = path.join(path.dirname(process.execPath), 'history.db');
} else if (isElectron) {
    dbPath = path.join(__dirname, '../history.db');
    if (__dirname.includes('app.asar')) {
        dbPath = path.join(process.resourcesPath, '..', 'history.db');
    }
} else {
    dbPath = path.join(__dirname, '../history.db');
}

console.log('Database path:', dbPath);
const db = new sqlite3.Database(dbPath);

function initDb() {
    db.serialize(() => {
        db.run(`CREATE TABLE IF NOT EXISTS analysis_history (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            prompt TEXT,
            response TEXT,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )`);
    });
}

function getHistory(prompt) {
    return new Promise((resolve, reject) => {
        db.get("SELECT response FROM analysis_history WHERE prompt = ?", [prompt], (err, row) => {
            if (err) reject(err);
            else resolve(row ? JSON.parse(row.response) : null);
        });
    });
}

function saveHistory(prompt, response) {
    return new Promise((resolve, reject) => {
        db.run("INSERT INTO analysis_history (prompt, response) VALUES (?, ?)", [prompt, JSON.stringify(response)], (err) => {
            if (err) reject(err);
            else resolve();
        });
    });
}

module.exports = {
    db,
    initDb,
    getHistory,
    saveHistory
};
