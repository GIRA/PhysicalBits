const fs = require('fs').promises;
const ncp = require('ncp').ncp;
const { exec } = require('child_process');
const archiver = require('archiver');
const rimraf = require('rimraf');

function nop() { /* Do nothing */ }
function log() { console.log(arguments); }

Promise.each = function(arr, fn) {
  if(!Array.isArray(arr)) return Promise.reject(new Error("Non array passed to each"));
  if(arr.length === 0) return Promise.resolve();
  return arr.reduce(function(prev, cur) {
    return prev.then(() => fn(cur))
  }, Promise.resolve());
}

const appName = "PhysicalBits";
const releasesFolder = "out";
const platform = process.platform;
const version = process.argv[2];
if (!version) {
  console.log("ERROR! Version required");
  return;
}

// HACK(Richo): Special build for the Orange Pi Zero
const opi = process.argv[3] == "opi";

if (opi) {
  removeDir(releasesFolder)
    .then(createServerJAR)
    .then(opiRelease);
} else {
  removeDir(releasesFolder)
    .then(createJRE)
    .then(createServerJAR)
    .then(webRelease)
    .then(desktopRelease);
}

function opiRelease() {
  let outFolder = releasesFolder + "/" + appName + "." + version + "-OPI";
  console.log("\nBuilding " + appName + "-OPI");
  return createOutFolder(outFolder)
    .then(() => copyGUI(outFolder))
    .then(() => copyUziLibraries(outFolder))
    .then(() => copyServerJAR(outFolder))
    .then(() => copyConfigEDN(outFolder))
    .then(() => createStartScripts(outFolder, false))
    .then(() => createZip(outFolder));
}

function webRelease() {
  let outFolder = releasesFolder + "/" + appName + "." + version + "-web-" + platform;
  console.log("\nBuilding " + appName + "-web");
  return createOutFolder(outFolder)
    .then(() => copyFirmware(outFolder))
    .then(() => copyGUI(outFolder))
    .then(() => copyUziLibraries(outFolder))
    .then(() => copyJRE(outFolder))
    .then(() => copyServerJAR(outFolder))
    .then(() => copyConfigEDN(outFolder))
    .then(() => createStartScripts(outFolder, true))
    .then(() => createZip(outFolder));
}

function desktopRelease() {
  let outFolder = releasesFolder + "/" + appName + "." + version + "-desktop-" + platform;
  return createElectronPackage()
    .then(() => copyElectronPackage(outFolder))
    .then(() => {
      let appFolder;
      if (platform == "darwin") {
        appFolder = outFolder + "/" + appName + ".app/Contents/Resources/app/";
      } else {
        appFolder = outFolder + "/resources/app";
      }

      return copyFirmware(outFolder)
        .then(() => copyGUI(appFolder))
        .then(() => copyUziLibraries(appFolder))
        .then(() => copyJRE(appFolder))
        .then(() => copyServerJAR(appFolder))
        .then(() => copyConfigEDN(appFolder))
        .then(() => createStartScripts(appFolder, false))
        .then(() => createConfigJSON(appFolder))
        .then(() => createZip(outFolder));
    });
}

function createJRE() {
  console.log("\nCreating JRE");
  let outputFolder = "out/jre";
  return removeDir(outputFolder).then(findJava).then(java_home => {
    let modules = "java.base,java.sql,java.xml,java.naming,java.desktop";
    let cmd = java_home + "jlink --add-modules " + modules + " --output "  + outputFolder + " --strip-debug --no-man-pages --no-header-files --compress=2";
    return executeCmd(cmd).catch(log);
  });
}

function findJava() {
  if (platform == "darwin") {
    /*
    For some reason, in macOS the jlink program is not found, so we need to go to the actual java
    folder and find it. To do that, it seems the easier way is to execute a command that will
    return the folder. Then, we find the binaries inside the bin folder.
    */
    return executeCmd("/usr/libexec/java_home").then(java_home => java_home.trim() + "/bin/");
  } else {
    // Other platforms should find jlink in the path already, so we don't need to find the java home
    return Promise.resolve("");
  }
}

function createElectronPackage() {
  console.log("\nCreating electron package");
  return removeDir("../middleware/desktop/out")
    .then(() => executeCmd("npm install", "../middleware/desktop").catch(log))
    .then(() => {
      let cmd = "electron-packager . " + appName + " --out=out --overwrite";
      return executeCmd(cmd, "../middleware/desktop").catch(log);
    });
}

function copyElectronPackage(path) {
  console.log("Copying electron package");
  return fs.readdir("../middleware/desktop/out").then(entries => {
    if (entries.length == 0) throw "No electron package!";
    else if (entries.length > 1) throw "More than 1 electron package!";

    return copyDir("../middleware/desktop/out/" + entries[0], path).catch(log);
  });
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

function copyJRE(path) {
  console.log("Copying JRE");
  return copyDir("out/jre", path + "/middleware/jre").catch(log);
}

function copyServerJAR(path) {
  console.log("Copying server jar");
  let uberjarFolder = "../middleware/server/target/uberjar";
  return fs.readdir(uberjarFolder).then(files => {
    let jar = files.find(file => file.endsWith("-standalone.jar"));
    if (jar == undefined) { throw "NO UBERJAR FOUND!!"; }
    return fs.copyFile(uberjarFolder + "/" + jar, path + "/middleware/server.jar").catch(log);
  });
}

function copyConfigEDN(path) {
  console.log("Copying config.edn file");
  let configPath = "../middleware/server/config.edn";
  return fs.copyFile(configPath, path + "/config.edn").catch(log);
}

function createStartScripts(path, openBrowser) {
  console.log("Creating start scripts");
  let cmd = "java -showversion -jar middleware/server.jar -w middleware/gui -u middleware/uzi";
  if (opi) {
    let zulu_path = "../zulu13/bin/";
    return Promise.all([
      fs.writeFile(path + "/start.sh", "#!/bin/bash" + "\n" + cmd).then(() => {
        return fs.chmod(path + "/start.sh", 0o777);
      }),
      fs.writeFile(path + "/startzulu.sh", "#!/bin/bash" + "\n" + zulu_path + cmd).then(() => {
        return fs.chmod(path + "/startzulu.sh", 0o777);
      })
    ]);
  } else {
    let jre_path = "middleware/jre/bin/";
    if (platform == "win32") {
      jre_path = jre_path.replace(/\//g, "\\");
    }
    cmd = jre_path + cmd;
    if (openBrowser) { cmd += " -o"; }
    if (platform == "win32") {
      return fs.writeFile(path + "/start.bat", cmd);
    } else {
      return fs.writeFile(path + "/start.sh", "#!/bin/bash" + "\n" + cmd).then(() => {
        return fs.chmod(path + "/start.sh", 0o777);
      })
    }
  }
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
    let options = {};
    if (cwd) {
      options.cwd = cwd;
    }
    let p = exec(cmd, options, (error, stdout, stderr) => {
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
    console.log("--- Started process (PID: " + p.pid + ") $ " + (cwd ? cwd : ".") + " > " + cmd);
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

function removeDir(path) {
  return new Promise((resolve, reject) => {
    rimraf(path, resolve);
  });
}
