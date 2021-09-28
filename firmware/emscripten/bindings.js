/*
EXTERN unsigned short GPIO_getPinValue(unsigned int pin);
EXTERN void GPIO_setPinValue(unsigned int pin, unsigned short value);
EXTERN void Sketch_setup(void);
EXTERN void Sketch_loop(void);
EXTERN size_t Serial_readInto(char* buffer, size_t len);
EXTERN void Serial_write(char* str, size_t len);
EXTERN long Sketch_getMillis(void);
EXTERN void Sketch_setMillis(long millis);
EXTERN unsigned char EEPROM_read(int address);
EXTERN void EEPROM_write(int address, unsigned char value);
EXTERN int EEPROM_size(void);
EXTERN uint16_t Stats_usedMemory();
EXTERN uint32_t Stats_coroutineResizeCounter();
*/
var GPIO = {
  getPinValue: Module.cwrap("GPIO_getPinValue", "number", ["number"]),
  setPinValue: Module.cwrap("GPIO_setPinValue", "number", ["number", "number"]),
};
var Sketch = {
  setup: Module.cwrap("Sketch_setup", null, []),
  loop: Module.cwrap("Sketch_loop", null, []),
  getMillis: Module.cwrap("Sketch_getMillis", "number", []),
  setMillis: Module.cwrap("Sketch_setMillis", null, ["number"]),
};
var Serial = {
  readInto: Module.cwrap("Serial_readInto", "number", ["number", "number"]),
  write: Module.cwrap("Serial_write", null, ["array", "number"]),
  listeners: [],
  addListener: function (listener) {
    Serial.listeners.push(listener);
  },
  removeListener: function (listener) {
    Serial.listeners = Serial.listeners.filter(l => l != listener);
  },
  notifyListeners: function (data) {
    Serial.listeners.forEach(listener => {
      try {
        listener(data);
      } catch (err) {
        console.error("ERROR notifying listeners", err);
      }
    });
  },
  readAvailable: function () {
    let buffer;
    try {
      buffer = Module._malloc(1024);
      let bytesRead = Serial.readInto(buffer, 1024);
      let result = [];
      for (var i = 0; i < bytesRead; i++) {
        result.push(Module.HEAPU8[buffer + i]);
      }
      return result;
    } finally {
      Module._free(buffer);
    }
  }
};
var EEPROM = {
  write: Module.cwrap("EEPROM_write", null, ["number", "number"]),
  read: Module.cwrap("EEPROM_read", "number", ["number"]),
  size: Module.cwrap("EEPROM_size", "number", []),
};
var Simulator = {
  interval: null,
  start: function (interval) {
    Simulator.stop();

    let begin = Date.now();
    Sketch.setMillis(Date.now() - begin);
    Sketch.setup();
    Simulator.interval = setInterval(() => {
      Sketch.setMillis(Date.now() - begin);
      Sketch.loop();
      let data = Serial.readAvailable();
      if (data.length > 0) {
        Serial.notifyListeners(data);
      }
    }, interval);
  },
  stop: function () {
    clearInterval(Simulator.interval);
  }
};
Simulator.ready = new Promise(res => {
  Simulator.init = res;
});

function handshake() {
  Sketch.setMillis(0);
  Sketch.setup();
  Serial.write([255, 0, 8], 3);
  Sketch.loop();
  var handshake = Serial.readAvailable();
  console.log(handshake);

  var send = (0 + 8 + handshake[0]) % 256;
  Serial.write([send], 1);
  Sketch.loop();

  var ack = Serial.readAvailable();
  console.log(ack);
}

function test_blink() {
  handshake();

  var program = [0, 0, 12, 1, 2, 4, 13, 5, 3, 232, 160, 4, 2, 131, 162];
  Serial.write(program, program.length);
  var begin = Date.now();
  setInterval(() => {
    var now = Date.now() - begin;
    Sketch.setMillis(now);
    Sketch.loop();
    console.log(GPIO.getPinValue(13));
  }, 100);
}
