# Physical Bits server

The middleware contains a small set of tools that allow to compile, debug, and transmit the programs to the Arduino board through a serial connection.

In order for the web-based IDE to interact with these tools the middleware exposes a REST API that allows to: connect and disconnect from the robot; compile, run, and install programs; and a websocket endpoint that notifies the clients of state changes in the robot, allowing them to access sensor and global data (among other things).

These tools were originally prototyped using [Squeak](https://squeak.org/), an open source version of [Smalltalk](https://en.wikipedia.org/wiki/Smalltalk). We used Smalltalk to build the first prototypes mainly due to our familiarity with the language. But we eventually had to face some of its limitations and so we decided to port the entire Smalltalk codebase to [Clojure](https://clojure.org/). This is the result of that port.

## Quick start (Clojure)

### Development

We are using the [leiningen](https://leiningen.org/) for development. Open a REPL by running:

    $ lein repl

Then start the server by evaluating the `start` function.

    $ user=> (start)

You can then open a browser pointing to the IDE by running the `open-browser` function.

    $ user=> (open-browser)

If everything goes well you should see something like this:

![repl](/docs/img/repl.png)

### Testing

To run tests I find is better to use the [test-refresh](https://github.com/jakemcc/lein-test-refresh) plugin. Execute the following in a separate terminal:

    $ lein test-refresh :changes-only

Now you should have a process running that automatically runs the tests whenever a source file changes. By using the `:changes-only` option it will only run the test namespaces where the code has changed, if you want to run *all* the tests, just press `Enter` in the console.

I also modified the report function to play a short sound as a notification if the tests pass or fail.

### Compilation

To build a jar file you can close the REPL and execute:

    $ lein uberjar

This command will generate a jar file in the `/target/uberjar/` directory. You can then start the server by running the jar file:

    $ java -jar target/uberjar/middleware-x.y.z-standalone.jar [args]

Make sure to specify valid arguments so that the server knows where to locate the uzi libraries as well as the web resources.

The options are:

    -u, --uzi             PATH      Uzi libraries folder (default: "uzi")  
    -w, --web             PATH      Web resources folder (default: "web")
    -s, --server-port     PORT      Arduino port name (optional)
    -o, --open-browser              Open a browser automatically (optional)
    -h, --help

## Quick start (ClojureScript)

### Development

In ClojureScript, we are currently using [shadow-cljs](https://github.com/thheller/shadow-cljs) for development.

There are several builds configured for different targets, you can look at the `shadow-cljs.edn` configuration file to see the current available builds. Some builds target the browser, others target node, others are just for testing. In each case, the way of running the code differs slightly. However, I think the best is to start by running a shadow-cljs server:

    $ shadow-cljs server

This command will start a process that shadow-cljs will use to speed up the startup of future commands. You can run the existing browser builds by opening the browser in the url http://localhost:8081. Also, you can connect the REPL to the server on the port 9000.

To compile the code you must run (in a different terminal) the `shadow-cljs watch <build>` command. This will trigger the compilation process automatically after any changes to the code. For example, the following command will compile the `simulator` build:

    $ shadow-cljs watch simulator

Wait until compilation finishes and then you can open the browser on the resulting build.

To connect the REPL you use the port 9000. However, once you connected the REPL to the shadow-cljs server you need to select the appropiate build if you want to run code in the actual runtime:

    $ (shadow.cljs.devtools.api/nrepl-select :simulator)

Now you should be able to evaluate code in the context of the browser.

If the build target is node, the same steps apply but instead of opening the browser you need to run the generated code using `node`.

### Testing

In order to run the tests on every change you should start the shadow-cljs server as instructed above and then, in a separate terminal:

    $ shadow-cljs watch compiler-test

After the build is completed you should be able to run the tests in the browser (the url should be http://localhost:8081/). While the browser window is open any change in the source code should run the tests automatically.

If the build target is node, instead of opening the browser you need to run the code using `node`.

### Compilation

In order to compile for production you should execute the `shadow-cljs release <build>` command:

    $ shadow-cljs release simulator

The compiled output should be a single javascript file (output folder depends on the build configuration, see `shadow-cljs.edn`) that you can load in the browser or run using node depending on the target.

## Dependencies

For Clojure:
* [Java](https://openjdk.java.net/)
* [Leiningen](https://leiningen.org/)

For ClojureScript:
* [Node.js](https://nodejs.org/)
* [shadow-cljs](https://github.com/thheller/shadow-cljs)
