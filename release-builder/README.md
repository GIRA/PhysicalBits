# Release Builder

## Quick start

This project allows you to build a Physical Bits release by running:

    $ node . [version]

After running, check the `/out` folder for the generated files.

## Dependencies

First of all, you'll obviously need [Node.js](https://nodejs.org/) installed.

Before you run make sure to install the npm dependencies:

    $ npm install

You will also need the following:

* [Leiningen](https://leiningen.org/) to be able to build the server jar. This also needs [Java](https://openjdk.java.net/) installed.
* [electron-packager](https://github.com/electron/electron-packager) to build the desktop versions. This needs to be installed globally (`npm install electron-packager -g`).
* [Wine](https://wiki.winehq.org/MacOS) in macOS to be able to build windows packages (`brew cask install xquartz` and then `brew cask install --no-quarantine wine-stable`).
