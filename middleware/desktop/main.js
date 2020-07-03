const { app, BrowserWindow } = require('electron')
const { spawn } = require('child_process');

/*
const bat = spawn('java', ['-jar', 'middleware\\middleware-0.3.0-SNAPSHOT-standalone.jar',
                           '-o', '-w', 'middleware\\web', '-u', 'middleware\\uzi']);
*/

function createWindow () {
  // Create the browser window.
  const win = new BrowserWindow({
    show: false,
    autoHideMenuBar: true
  });

  // TODO(Richo): Allow to config path
  win.loadFile('../../gui/ide/index.html');
  win.maximize();

  // win.webContents.openDevTools();
  win.show();
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(createWindow)

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    //bat.kill();
    app.quit()
  }
})

app.on('activate', () => {
  // On macOS it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow()
  }
})

// In this file you can include the rest of your app's specific main process
// code. You can also put them in separate files and require them here.
