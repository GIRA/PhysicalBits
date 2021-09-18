First run the `emsdk_env.bat` to set the env variables for emscripten. Then:

    nodemon -e c,h,cpp,hpp,ino,bat --watch .. --exec build.bat
