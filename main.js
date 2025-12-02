const { app, BrowserWindow, ipcMain, dialog, shell } = require('electron');
const path = require('path');
const fs = require('fs');
const os = require('os');

const { spawn } = require("child_process");

let javaProcess;
let isQuitting = false;

function startBackend() {
  const isDev = !app.isPackaged;

  const base = isDev
    ? path.join(__dirname, "server")
    : path.join(process.resourcesPath, "server");

  const javaBin = path.join(base, "jre", "bin", "java.exe");
  const jar = path.join(base, "XBRL_Lite.jar");

  const port = 8082;

  javaProcess = spawn(javaBin, ["-jar", jar, `--server.port=${port}`], {
    cwd: base,
    stdio: "inherit",
    windowsHide: true,
  });

  javaProcess.on("exit", (code) => {
    if (!isQuitting) {
      app.quit();
    }
  });

  return `http://127.0.0.1:${port}`;
}

async function createWindow() {
  const win = new BrowserWindow({
    width: 1200,
    height: 800,
    icon: path.join(__dirname, "icons", "favico.ico"),
    webPreferences: { preload: path.join(__dirname, 'preload.js'),contextIsolation: true, nodeIntegration: false },
    autoHideMenuBar: true
  });

  win.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url);
    return { action: 'deny' };
  });


  win.webContents.openDevTools({ mode: 'detach' });
  //await win.loadURL(url);
  await win.loadURL(`http://127.0.0.1:8082`);
}

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

    const isDev = !app.isPackaged;
    const base = isDev
      ? path.join(__dirname, "server")
      : path.join(process.resourcesPath, "server");

    const pathDirectory = isDev
      ? __dirname
      : process.resourcesPath;

    const filePath = path.join(pathDirectory, 'path.dat');
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

app.on("before-quit", () => {
  isQuitting = true;
  if (javaProcess && !javaProcess.killed) {
    try { javaProcess.kill(); } catch {}
  }
});

app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit(); });