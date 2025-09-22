const { app, BrowserWindow, ipcMain, dialog, Menu } = require('electron');
const path = require('path');
const fs = require('fs');
const os = require('os');
const { spawn } = require('child_process');

let javaProcess;
let isQuitting = false;

const isDev = !app.isPackaged;

function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }
async function waitFor(url, { timeoutMs = 30000, intervalMs = 400 } = {}) {
  const start = Date.now();
  while (true) {
    try {
      const res = await fetch(url, { cache: 'no-store' });
      if (res.ok) return;
    } catch {}
    if (Date.now() - start > timeoutMs) {
      throw new Error(`Timeout waiting for ${url}`);
    }
    await sleep(intervalMs);
  }
}

function baseServerDir() {
  return isDev ? path.join(__dirname, 'server') : path.join(process.resourcesPath, 'server');
}


async function pickPort() {
  const { default: getPort } = await import('get-port'); // <— key line
  return getPort({ port: [8082, 8083, 8084, 8085, 0] });
}

async function startBackend() {
  const base = baseServerDir();
  const javaBin = path.join(base, 'jre', 'bin', 'java.exe');
  const jar = path.join(base, 'XBRL_Lite.jar');

  const port = await pickPort();

  javaProcess = spawn(javaBin, ['-jar', jar, `--server.port=${port}`], {
    cwd: base,
    stdio: 'inherit',
    windowsHide: true,
  });

  javaProcess.on('exit', () => { if (!isQuitting) app.quit(); });

  return `http://127.0.0.1:${port}`;
}

async function createWindow() {
  try {
    const url = await startBackend();  // <-- await here

    if (typeof url !== 'string' || !/^https?:\/\//i.test(url)) {
      throw new Error(`Bad URL from startBackend: ${String(url)}`);
    }

    try {
      await waitFor(`${url}/actuator/health`, { timeoutMs: 30000 });
    } catch (e) {
      console.warn('[MAIN] health check timed out, loading UI anyway');
    }

    const win = new BrowserWindow({
      width: 1200,
      height: 800,
      autoHideMenuBar: true,
      icon: path.join(__dirname, 'icons', 'output.ico'),
      webPreferences: {
        preload: path.join(__dirname, 'preload.js'),
        contextIsolation: true,
        nodeIntegration: false,
      },
    });

    win.setMenuBarVisibility(false);

    Menu.setApplicationMenu(null);

    if (isDev) win.webContents.openDevTools({ mode: 'detach' });

    await win.loadURL(url);  
  } catch (err) {
    console.error('[MAIN] createWindow failed:', err);
    dialog.showErrorBox('Startup failed', String(err?.message || err));
    app.quit();
  }
}


app.whenReady().then(createWindow);

// keep your ipc handlers...
// IMPORTANT: read & write path.dat in the SAME place as you write it
ipcMain.handle('read-stored-path', () => {
  const base = baseServerDir();
  const filePath = path.join(base, 'path.dat');  // <-- was __dirname before
  if (fs.existsSync(filePath)) {
    const content = fs.readFileSync(filePath, 'utf-8').trim();
    if (content) {
      try {
        if (fs.existsSync(content) && fs.lstatSync(content).isDirectory()) return content;
        fs.mkdirSync(content, { recursive: true });
        return content;
      } catch (e) { console.error('Error with stored path:', e); }
    }
  }
  const fallback = path.join(os.homedir(), 'Downloads');
  if (!fs.existsSync(fallback)) fs.mkdirSync(fallback, { recursive: true });
  return fallback;
});

app.on('before-quit', () => {
  isQuitting = true;
  if (javaProcess && !javaProcess.killed) { try { javaProcess.kill(); } catch {} }
});
app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit(); });
