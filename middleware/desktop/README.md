# Physical Bits desktop

This project is a simple [electron](https://www.electronjs.org/) wrapper around the Physical Bits server in order to provide a desktop experience.

## Quick start

__IMPORTANT__: In order for this app to work you'll first need to compile and build the server jar as explained [here](https://github.com/GIRA/PhysicalBits/tree/master/middleware/server#compilation).

Once the server jar is generated you can start the electron app by running:

    $ npm start

![electron](/docs/img/electron.png)

### Configuration

You *probably* won't need to change this but the app uses a small configuration file called `config.json` with just three parameters:

* `"startServer"`: If true, it will start the server by running either `start.bat` (on Windows) or `start.sh` (on linux and macOS).
* `"index"`: The path to the IDE html file that will be used for the main window.
* `"devTools"`: If true, the main window will open chrome developer tools automatically.

## Compilation

To package the application as an executable you should probably use the [release-builder](https://github.com/GIRA/PhysicalBits/tree/master/release-builder) that takes care of everything automatically.

## Dependencies

* [Node.js](https://nodejs.org/), obviously.
* [Java](https://openjdk.java.net/) to run the server jar.
* [Leiningen](https://leiningen.org/) to build the server jar.
