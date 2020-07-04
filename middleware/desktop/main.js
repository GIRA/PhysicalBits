const { app, BrowserWindow } = require('electron')
const { spawn } = require('child_process');
const fs = require('fs');

if (!app.requestSingleInstanceLock()) { return app.quit(); }

const config = JSON.parse(fs.readFileSync('config.json', 'utf-8'));

let win;

app.on('second-instance', (event, commandLine, workingDirectory) => {
  // Someone tried to run a second instance, we should focus our window.
  if (win) {
    if (win.isMinimized()) win.restore();
    win.focus();
  }
});

const server = spawn('java', ['-jar', config.jar, '-w', config.web, '-u', config.uzi]);

function createWindow () {
  // Create the browser window.
  win = new BrowserWindow({
    show: false,
    autoHideMenuBar: true
  });

  // TODO(Richo): Allow to config path
  win.loadFile(config.index);
  win.maximize();

  if (config.devTools) {
    win.webContents.openDevTools();
  }
  win.show();
}

app.whenReady().then(createWindow);

app.on('will-quit', () => {
  server.kill();
});
