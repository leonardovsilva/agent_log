const { app, BrowserWindow } = require('electron');
const path = require('path');

// Start the Express server
require('./server.js');

function createWindow() {
    const win = new BrowserWindow({
        width: 1200,
        height: 800,
        title: "Assistente de Erros", // Set custom title
        autoHideMenuBar: true, // Auto hide menu bar instead of removing it completely if preferred, or keep setMenuBarVisibility(false)
        webPreferences: {
            nodeIntegration: false,
            contextIsolation: true
        },
        icon: path.join(__dirname, '../public/favicon.ico'),
        backgroundColor: '#000000' // Set background to black
    });

    // Load the local server URL
    // Wait a bit for the server to start (simple approach)
    setTimeout(() => {
        win.loadURL('http://localhost:3000');
    }, 1000);

    // Remove menu bar for a cleaner look
    win.setMenuBarVisibility(false);
}

app.whenReady().then(() => {
    createWindow();

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) {
            createWindow();
        }
    });
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit();
    }
});
