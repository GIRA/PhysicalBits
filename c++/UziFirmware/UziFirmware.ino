#include "StackVM.h"
#include "SerialStream.h"

/* REQUEST COMMANDS */
#define RQ_SET_PROGRAM	                         0
#define RQ_SET_VALUE                             1
#define RQ_SET_MODE                              2

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
ReadStream * stream = new SerialStream(&Serial);

void executeCommand(byte);
void executeSetProgram(void);
void executeSetValue(void);
void executeSetMode(void);

void setup() {
	Serial.begin(57600);
	// Temporary hack because the StackVM doesn't have primitives to set pin modes yet:
	pe->setMode(9, OUTPUT);
	pe->setMode(10, INPUT);
}

void loop() {
	if (Serial.available()) {
		unsigned char inByte = stream->nextChar();
		Serial.write(inByte);
		executeCommand(inByte);		
	} else {	
		vm->executeProgram(program, pe);
	}
}

void executeCommand(unsigned char cmd) {
	switch(cmd) {
		case RQ_SET_PROGRAM:
			executeSetProgram();
			break;
                case RQ_SET_VALUE:
                        executeSetValue();
                        break;
                case RQ_SET_MODE:
                        executeSetMode();
                        break;
	}
}

void executeSetProgram(void) {
        delete program;
        program = new StackProgram(stream);
}

void executeSetValue(void) {
        unsigned char pin = stream->nextChar();
        unsigned char value = stream->nextChar();
        
        pe->setValue(pin, value);
}

void executeSetMode(void) {
        unsigned char pin = stream->nextChar();
        unsigned char mode = stream->nextChar();
        
        pe->setMode(pin, mode);
}
