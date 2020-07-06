const fs = require('fs').promises;
const ncp = require('ncp').ncp;
const { exec } = require('child_process');

function nop() { /* Do nothing */ }
function log() { console.log(arguments); }

//const releasesFolder = "../releases";
const releasesFolder = "out";
const version = process.argv[2];
if (!version) {
  console.log("ERROR! Version required");
  return;
}

let outFolder;

createServerJAR()
  .then(webRelease)
  .then(desktopRelease);


function webRelease() {
  outFolder = releasesFolder + "/UziScript." + version + "-web";
  console.log("\nBuilding " + outFolder);
  return createOutFolder()
    .then(copyFirmware)
    .then(copyGUI)
    .then(copyUziLibraries)
    .then(copyServerJAR)
    .then(createStartScripts);
}

function desktopRelease() {
  outFolder = releasesFolder + "/UziScript." + version + "-desktop";
  console.log("\nBuilding " + outFolder);
  console.log("TODO");
  return;
  return createOutFolder()
    .then(copyFirmware)
    .then(copyGUI)
    .then(copyUziLibraries)
    .then(copyServerJAR)
    .then(createStartScripts);
}

function createServerJAR() {
  console.log("Creating server JAR");
  return executeCmd("lein uberjar", "../middleware/server").catch(log);
}

function createOutFolder() {
  console.log("Creating out folder");
  return fs.mkdir(outFolder).catch(nop);
}

function copyFirmware() {
  console.log("Copying firmware");
  return copyDir("../firmware/UziFirmware", outFolder + "/firmware/UziFirmware").catch(log);
}

function copyGUI() {
  console.log("Copying GUI files");
  return copyDir("../gui/ide", outFolder + "/middleware/gui/ide").catch(log);
}

function copyUziLibraries() {
  console.log("Copying uzi libraries");
  return copyDir("../uzi/libraries", outFolder + "/middleware/uzi").catch(log);
}

function copyServerJAR() {
  console.log("Copying server jar");
  // TODO(Richo): Find jar automatically...
  return fs.copyFile("../middleware/server/target/uberjar/middleware-0.3.0-SNAPSHOT-standalone.jar",
              outFolder + "/middleware/server.jar").catch(log);
}

function createStartScripts() {
  console.log("Creating start scripts");
  const cmd = "java -jar middleware/server.jar -o -w middleware/gui -u middleware/uzi";
  return Promise.all([
    fs.writeFile(outFolder + "/start.bat", cmd),
    fs.writeFile(outFolder + "/start.sh", "#!/bin/bash" + "\n" + cmd).then(() => {
      return fs.chmod(outFolder + "/start.sh", 0o777);
    })
  ]);
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
