const fs = require('fs').promises;
const ncp = require('ncp').ncp;
const { exec } = require('child_process');
const archiver = require('archiver');

function nop() { /* Do nothing */ }
function log() { console.log(arguments); }

Promise.each = function(arr, fn) {
  if(!Array.isArray(arr)) return Promise.reject(new Error("Non array passed to each"));
  if(arr.length === 0) return Promise.resolve();
  return arr.reduce(function(prev, cur) {
    return prev.then(() => fn(cur))
  }, Promise.resolve());
}

const appName = "PhysicalBITS";
const releasesFolder = "out";
const version = process.argv[2];
if (!version) {
  console.log("ERROR! Version required");
  return;
}

createServerJAR()
  .then(webRelease)
  .then(desktopRelease);

function webRelease() {
  let outFolder = releasesFolder + "/" + appName + "." + version + "-web";
  console.log("\nBuilding " + appName + "-web");
  return createOutFolder(outFolder)
    .then(() => copyFirmware(outFolder))
    .then(() => copyGUI(outFolder))
    .then(() => copyUziLibraries(outFolder))
    .then(() => copyServerJAR(outFolder))
    .then(() => createStartScripts(outFolder, true))
    .then(() => createZip(outFolder));
}

function desktopRelease() {
  let tempFolder = releasesFolder + "/" + appName + "." + version + "-desktop";
  return createElectronPackages()
    .then(() => copyElectronPackages(tempFolder))
    .then(() => fs.readdir(tempFolder)
      .then(folders => Promise.each(folders, folder => {
        console.log("\nBuilding " + folder);
        let packageFolder = tempFolder + "/" + folder;
        let appFolder = packageFolder + "/resources/app";
        let finalFolder = releasesFolder + "/" + folder.replace(appName, appName + "." + version);
        return copyFirmware(packageFolder)
          .then(() => copyGUI(appFolder))
          .then(() => copyUziLibraries(appFolder))
          .then(() => copyServerJAR(appFolder))
          .then(() => createStartScripts(appFolder, false))
          .then(() => createConfigJSON(appFolder))
          .then(() => fs.rename(packageFolder, finalFolder))
          .then(() => createZip(finalFolder));
      })))
    .then(() => fs.rmdir(tempFolder));
}

function createElectronPackages() {
  let cmd = "electron-packager . " + appName + " --platform=win32 --arch=all --out=out --overwrite";
  console.log("\nCreating electron packages");
  return executeCmd(cmd, "../middleware/desktop").catch(log);
}

function copyElectronPackages(path) {
  console.log("Copying electron packages");
  return copyDir("../middleware/desktop/out", path).catch(log);
}

function createServerJAR() {
  console.log("\nCreating server JAR");
  return executeCmd("lein uberjar", "../middleware/server").catch(log);
}

function createOutFolder(path) {
  console.log("Creating out folder");
  return fs.mkdir(path).catch(nop);
}

function copyFirmware(path) {
  console.log("Copying firmware");
  return copyDir("../firmware/UziFirmware", path + "/firmware/UziFirmware").catch(log);
}

function copyGUI(path) {
  console.log("Copying GUI files");
  return copyDir("../gui/ide", path + "/middleware/gui/ide").catch(log);
}

function copyUziLibraries(path) {
  console.log("Copying uzi libraries");
  return copyDir("../uzi/libraries", path + "/middleware/uzi").catch(log);
}

function copyServerJAR(path) {
  console.log("Copying server jar");
  // TODO(Richo): Find jar automatically...
  return fs.copyFile("../middleware/server/target/uberjar/middleware-0.3.0-SNAPSHOT-standalone.jar",
                    path + "/middleware/server.jar").catch(log);
}

function createStartScripts(path, openBrowser) {
  console.log("Creating start scripts");
  let cmd = "java -jar middleware/server.jar -w middleware/gui -u middleware/uzi";
  if (openBrowser) { cmd += " -o"; }
  return Promise.all([
    fs.writeFile(path + "/start.bat", cmd),
    fs.writeFile(path + "/start.sh", "#!/bin/bash" + "\n" + cmd).then(() => {
      return fs.chmod(path + "/start.sh", 0o777);
    })
  ]);
}

function createConfigJSON(path) {
  console.log("Creating config.json");
  let config = {
    "startServer": true,
    "index": "middleware/gui/ide/index.html",
    "devTools": false
  };
  return fs.writeFile(path + "/config.json", JSON.stringify(config));
}

function createZip(path) {
  console.log("Creating zip file");
  return zipDirectory(path).catch(log);
}

function copyDir(source, destination) {
  return new Promise((resolve, reject) => {
    fs.mkdir(destination, { recursive: true }).catch(nop).then(() => {
      ncp(source, destination, function (err) {
       if (err) { reject(err); }
       else { resolve(); }
      });
    });
  });
}

function executeCmd(cmd, cwd) {
  return new Promise((resolve, reject) => {
    let p = exec(cmd, { cwd: cwd }, (error, stdout, stderr) => {
      if (error) {
        reject({ error: error, stdout: stdout, stderr: stderr });
      } else {
        p.kill();
        resolve(stdout);
      }
    });
    p.stdout.on("data", (data) => { console.log(data.toString().trim()); });
    p.stderr.on("data", (data) => { console.log(data.toString().trim()); });
    p.on('exit', (code) => { console.log("--- Process (PID: " + p.pid + ") exited with code " + code) });
    console.log("--- Started process (PID: " + p.pid + ") $ " + cwd + " > " + cmd);
  });
}

function zipDirectory(path) {
  return new Promise((resolve, reject) => {

    var output = require('fs').createWriteStream(path + ".zip");
    var archive = archiver('zip', { zlib: { level: 9 }});

    output.on("close", resolve);
    archive.on("error", reject);
    archive.pipe(output);

    archive.directory(path, false);
    archive.finalize();
  });
}
