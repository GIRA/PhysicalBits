const { app, BrowserWindow } = require('electron')
const { spawn } = require('child_process');
const kill = require('tree-kill');
const fs = require('fs');
const path = require('path');
const log = require('electron-log');


log.transports.console.level = true;

if (!app.requestSingleInstanceLock()) { return app.quit(); }

const config = JSON.parse(fs.readFileSync(path.resolve(app.getAppPath(), 'config.json'), 'utf-8'));
log.info(config);
let win;

app.on('second-instance', (event, commandLine, workingDirectory) => {
  // Someone tried to run a second instance, we should focus our window.
  if (win) {
    if (win.isMinimized()) win.restore();
    win.focus();
  }
});

let server;
if (config.startServer) {
  /*
  NOTE(Richo): VERY IMPORTANT! The stdio 'ignore' option prevents the child process from
  blocking if the main process doesn't read from it.
  See https://nodejs.org/api/child_process.html#child_process_child_process
  */
  let options = { cwd: app.getAppPath(), stdio: 'ignore' };
  if (process.platform === 'win32') {
    server = spawn('cmd.exe', ['/c', 'start.bat'], options);
  } else {
    server = spawn('./start.sh', options);
  }
}

function executeJs(js) {
  return win.webContents.executeJavaScript(js);
}

function elog(event) {
  return executeJs("Uzi.elog('" + event.toString() + "')");
}

function killServer() {
  return new Promise((resolve, reject) => {
    if (server) {
      kill(server.pid, 'SIGTERM', function () {
        server = null;
        resolve();
      });
    } else {
      resolve();
    }
  });
}

function createWindow () {
  win = new BrowserWindow({
    show: false,
    autoHideMenuBar: true,
    webPreferences: {
      enableRemoteModule: true,
      nodeIntegration: true
    }
  });

  win.on('closed', function () {
      win = null;
  });

  win.on('close', function (e) {
      if (server) {
          e.preventDefault();
          // HACK(Richo): For some reason the SIGTERM signal is not detected
          // from the java side so I log the system shutdown from here before
          // killing the server.
          elog("SYSTEM/SHUTDOWN")
            .then(killServer)
            .then(() => win.close());
      }
  });

  win.on('minimize', function () {
    elog("WINDOW/MINIMIZE");
  });

  win.on('restore', function () {
    elog("WINDOW/RESTORE");
  });  

  win.on('blur', function () {
    elog("WINDOW/BLUR");
  });

  win.on('focus', function () {
    elog("WINDOW/FOCUS");
  });

  win.loadFile(path.resolve(app.getAppPath(), config.index));
  win.maximize();

  if (config.devTools) {
    win.webContents.openDevTools();
  }
  win.show();
}

app.whenReady().then(createWindow);
