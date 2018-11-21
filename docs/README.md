# UziScript Documentation

## Description of the language

UziScript syntax is based on C, which is familiar to most programmers including Arduino developers. The `task` keyword has been added to represent behavior that can be executed periodically at a configurable rate. For example, the following code will declare a task that will toggle the LED on pin 13 every second.

```
task blink() running 1/s { toggle(D13); }
```

UziScript does not require any type declarations, so to distinguish a function from a procedure two new keywords are introduced: `func` and `proc`.

```
func isOn(pin) { return read(pin) > 0.5; }
 
proc toggle(pin) {
  if isOn(pin) { turnOff(pin); }
  else { turnOn(pin); }
}
```

A program can have any number of tasks, and each task can be defined with a different interval as well as a different starting state, which can be either `running` or `stopped`. Additionally, special tasks that need to be run just once can be defined using the keyword `once` after the task name. This is especially useful to initialize variables and can be used as a substitute to the Arduino setup() function. 

The execution of each task at the correct time is performed automatically by the virtual machine but the user can invoke certain primitives to start, stop, pause, or resume a given task. Each task execution is independent, it has its own stack, and it shares memory with other tasks through specially defined global variables. This design allows users to write sequential programs in Arduino’s usual style and make them run concurrently without being concerned about the processor scheduling. 

Primitive instructions like `delay()` are provided to allow the user to block the executing task for a given amount of time without affecting the rest. Arduino related primitives are also included but in some cases their names and behavior were modified to offer a simplified interface with the hardware. For example, the Arduino `digitalRead()` and `analogRead()` functions are merged into a single UziScript function called `read()`, which accepts a pin number and returns a floating-point value. If the pin is digital the resulting value can either be 0 or 1 but if the pin is analog the function will normalize its value between 0 and 1. An equivalent implementation of the `write()` procedure is also provided. We believe these small design details make the language more accessible to beginners by providing a concise (and consistent) interface to the hardware.

Finally, UziScript also supports external libraries that can extend the primitive functionality of the language.
