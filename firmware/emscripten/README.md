# UziFirmware (emscripten)

This scripts allow to compile the firmware to wasm using emscripten. Obviously, you'll need [emscripten](https://emscripten.org/) installed.

I'm currently using the following version (on Windows):

```
λ em++ -v
emcc (Emscripten gcc/clang-like replacement + linker emulating GNU ld) 2.0.30 (f782b50a7f8dded7cd0e2c7ee4fed41ab743f5c0)
clang version 14.0.0 (https://github.com/llvm/llvm-project c4048d8f50aaf2c4c13b8d3e138abc34a22da754)
Target: wasm32-unknown-emscripten
Thread model: posix
InstalledDir: D:/emsdk/upstream/bin
```

**IMPORTANT**: In case `em++` is not found, run the `emsdk_env.bat` (that comes with the emscripten install) to set the env variables for the session.


## Build instructions

I made a batch script that invokes the emscripten compiler. It's not perfect but it works ok. It outputs two files (simulator.js and simulator.wasm) to the `../../gui/ide/` folder.

To build, just run:

    build.bat

If you want to recompile automatically on changes, run (this uses [nodemon](https://www.npmjs.com/package/nodemon) to watch for changes):

    watch.bat

## Running the compiled program

The build.bat script will generate files in the `../../gui/ide` folder, so that testing with the actual physical bits environment is easier. However, if you want to test the compilation separately you can modify the `out` variable so that it points to a local `out/` folder and then you can use the `index.html` file for testing.

To run this file is not enough to open it in the browser, you'll need a local webserver to host the files. I'm using [http-server](https://www.npmjs.com/package/http-server), which works great for this stuff.

    http-server.cmd . -c-1 --mimetypes mime.types -o
