#include "StackVM.h"
#include "SerialStream.h"

/* REQUEST COMMANDS */
#define RQ_SET_PROGRAM	                         0

// Two examples of compiled programs
unsigned char blinkLed9[] = {// LENGTH: 24
	// Stepping and steptime (true, 1000)
	0x80, 0x00, 0x03, 0xE8,
	
	// Literals (9, 255, 0, 1000)
	0x04, 0x0C, 0x09, 0xFF, 0x00, 0x05, 0x03, 0xE8, 

	// Variables
	0x00,

	// Bytecodes
	0x00, 0x50, 0xA4, 0x00, 0x01, 0x51, 0x83, 0x00,
	0x02, 0x51, 0xFF
};
unsigned char button10Led9[] = {// LENGTH: 14
	// Stepping and steptime (true, 0)
	0x80, 0x00, 0x00, 0x00,

	// Literals (9, 10)
	0x02, 0x08, 0x09, 0x0A,

	// Variables
	0x00,

	// Bytecodes
	0x00, 0x01, 0x50, 0x51, 0xFF 
};


StackProgram * program = new StackProgram();
StackVM * vm = new StackVM();
PE * pe = new PE();

void executeCommand(byte);
void executeSetProgram(void);

void setup() {
	Serial.begin(57600);
	// Temporary hack because the StackVM doesn't have primitives to set pin modes yet:
	pe->setMode(9, OUTPUT);
	pe->setMode(10, INPUT);
}

void loop() {
	if (Serial.available()) {
		byte inByte = Serial.read();
		Serial.write(inByte);
		executeCommand(inByte);		
	} else {	
		vm->executeProgram(program, pe);
	}

  /*int aval = Serial.available();
  if(aval > 0) {
    ReadStream * rs = new SerialStream(&Serial);
    for (int i = 0; i < aval; i++) {
      unsigned char next = rs->nextChar();
      Serial.write(next);
    }
    delete rs;
  }*/
	
        /*long n = rs->nextLong(4);
	bool _stepping = (n >> 31) & 1;
	long _stepTime = n & 0x7FFFFFFF;

        Serial.println("###");
        Serial.println(n);
        Serial.println(_stepping ? 1 : 0);
        Serial.println(_stepTime);
        Serial.println("###");*/
        
        /*
        if (Serial.available()) {
          Serial.read();
          long result = 0;
          result |= (0x00L << (3 * 8));
          result |= (0x01L << (2 * 8));
          result |= (0x00L << (1 * 8));
          result |= (0x01L << (0 * 8));
          Serial.println(result);
        }
        */
        
        /*
        long n = rs->nextLong(4);
        Serial.println(n);
        */
}

void executeCommand(byte cmd) {
	switch(cmd) {
		case RQ_SET_PROGRAM:
			executeSetProgram();
			break;
	}
}

void executeSetProgram(void) {
        ReadStream * rs = new SerialStream(&Serial);
        delete program;
        program = new StackProgram(rs);        
        delete rs;
}
