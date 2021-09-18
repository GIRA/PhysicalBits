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
  readInto: Module.cwrap("Serial_readInto", "number", ["array", "number"]),
  write: Module.cwrap("Serial_write", null, ["array", "number"]),

  readAvailable: function () {
    var buf = new Uint8Array(1024);
    var bytesRead = Serial.readInto(buf, buf.length);
    return buf.subarray(0, bytesRead);
  }
};
var EEPROM = {
  write: Module.cwrap("EEPROM_write", null, ["number", "number"]),
  read: Module.cwrap("EEPROM_read", "number", ["number"])
};

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
  }, 100);
}
