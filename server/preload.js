const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('api', {
  selectFolder: () => ipcRenderer.invoke('select-folder'),
  getDefaultFolder: () => ipcRenderer.invoke('get-default-folder'),
  readStoredPath: () => ipcRenderer.invoke('read-stored-path')
});
