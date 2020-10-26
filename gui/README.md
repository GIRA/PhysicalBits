# Physical Bits IDE

The Physical Bits IDE is built entirely out of plain HTML/CSS/Javascript. You can host these files in any way you want. However, the entire state of the UI is dependent on the middleware server. So the IDE won't start unless the server is accessible and running.

![IDE](/docs/img/ide.png)

## Quick start

The easiest way to work on this project is to just start the server as explained [here](https://github.com/GIRA/PhysicalBits/tree/master/middleware/server#quick-start). Once the server is started you can edit any of the IDE files and see the effects of the changes in the browser.

## Libraries

We used a lot of cool libraries to make this project.

* [Blockly](https://developers.google.com/blockly) to build the block editor.
* [Ace](https://ace.c9.io/) for the code editor.
* [GoldenLayout](https://golden-layout.com/) to manage the panel layout.
* [FileSaver](https://github.com/eligrey/FileSaver.js/) to implement the project download behavior.
* [jQuery](https://jquery.com/) to build parts of the GUI.
* [Bootstrap](https://getbootstrap.com/) to make the GUI a little nicer.
* [loading.io](https://loading.io/) for the robot icon in the loading screen.
* [Font Awesome](https://fontawesome.com/) for most of the icons in the UI.
