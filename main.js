const { app, BrowserWindow, ipcMain, dialog } = require('electron');
const path = require('path');
const fs = require('fs');
const os = require('os');

function createWindow () {
  const win = new BrowserWindow({
    width: 1200,
    height: 800,
    autoHideMenuBar: true, 
    frame: true,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false
    }
  });
  win.loadURL('http://127.0.0.1:8082');}

app.whenReady().then(createWindow);

ipcMain.handle('get-default-folder', () => {
  const downloadsPath = path.join(os.homedir(), 'Downloads');
  return downloadsPath;
});

ipcMain.handle('select-folder', async () => {
  const result = await dialog.showOpenDialog({
    properties: ['openDirectory']
  });

  if (!result.canceled && result.filePaths.length > 0) {
    const selectedPath = result.filePaths[0];

    const filePath = path.join(__dirname, 'path.dat');
    fs.writeFileSync(filePath, selectedPath, 'utf-8');

    return selectedPath;
  } else {
    return null;
  }
});

ipcMain.handle('read-stored-path', () => {
  const filePath = path.resolve(__dirname, 'path.dat');

  if (fs.existsSync(filePath)) {
    const content = fs.readFileSync(filePath, 'utf-8').trim();

    if (content) {
      try {
        if (fs.existsSync(content) && fs.lstatSync(content).isDirectory()) {
          return content;
        }

        fs.mkdirSync(content, { recursive: true });
        return content;
      } catch (error) {
        console.error("Error accessing or creating path from path.dat:", error);
      }
    } 
  }

  const fallback = path.join(os.homedir(), 'Downloads');

  if (!fs.existsSync(fallback)) {
    fs.mkdirSync(fallback, { recursive: true });
  }

  return fallback;
});

app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit(); });
