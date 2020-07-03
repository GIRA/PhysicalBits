const { app, BrowserWindow } = require('electron')
const { spawn } = require('child_process');

// TODO(Richo): Allow to configure paths
const server = spawn('java', ['-jar', '../server/target/uberjar/middleware-0.3.0-SNAPSHOT-standalone.jar',
                              '-w', '../../gui',
                              '-u', '../../uzi/libraries']);

function createWindow () {
  // Create the browser window.
  const win = new BrowserWindow({
    show: false,
    autoHideMenuBar: true
  });

  // TODO(Richo): Allow to config path
  win.loadFile('../../gui/ide/index.html');
  win.maximize();

  win.webContents.openDevTools();
  win.show();
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(createWindow);

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    server.kill();
    app.quit();
  }
});

app.on('activate', () => {
  // On macOS it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});
