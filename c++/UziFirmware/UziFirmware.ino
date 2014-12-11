#include "Interpreter.h"
#include "SerialStream.h"

/* REQUEST COMMANDS */
#define RQ_SET_PROGRAM									0
#define RQ_SET_VALUE									1
#define RQ_SET_MODE										2
#define RQ_START_REPORTING								3
#define RQ_STOP_REPORTING								4

/* RESPONSE COMMANDS */
#define RS_ERROR										0
#define RS_PIN_VALUE									1

/* MACROS */
#define IS_COMMAND(x)						((x) >> 7 == 0)
#define IS_ARGUMENT(x)						((x) >> 7 == 1)
#define GET_COMMAND(x)									(x)
#define GET_ARGUMENT(x)							((x) & 127)
#define AS_COMMAND(x)									(x)
#define AS_ARGUMENT(x)							((x) | 128)

Program * program = new Program();
Interpreter * interpreter = new Interpreter();
PE * pe = new PE();
ReadStream * stream = new SerialStream(&Serial);

unsigned char reporting = 0;
unsigned long lastTimeReport = 0;

void executeCommand(unsigned char);
void executeSetProgram(void);
void executeSetValue(void);
void executeSetMode(void);
void executeStartReporting(void);
void executeStopReporting(void);
void sendPinValues(void);
void sendError(unsigned char);

void setup() {
	Serial.begin(57600);
	// Temporary hack because the Interpreter doesn't have primitives to set pin modes yet:
	/*pe->setMode(9, OUTPUT);
	pe->setMode(8, OUTPUT);
	pe->setMode(10, INPUT);
	pe->setMode(15, INPUT);*/
}

void loop() {
	if (Serial.available()) {
		unsigned char inByte = stream->nextChar();
		executeCommand(inByte);
	}
	
	interpreter->executeProgram(program, pe);
	
	if (!reporting) return;
	unsigned long now = millis();
	if (now - lastTimeReport > 50) {
		sendPinValues();
		lastTimeReport = now;
	}
}

void sendError(unsigned char errorCode) {
	Serial.write(AS_COMMAND(RS_ERROR));
	Serial.write(AS_ARGUMENT(errorCode));
}

void sendPinValues(void) {
	for (int i = 0; i < TOTAL_PINS; i++) {
		int pin = PIN_NUMBER(i);
		if (pe->getReport(pin)) {
			Serial.write(AS_COMMAND(RS_PIN_VALUE));
			Serial.write(AS_ARGUMENT(pin));
			
			// PE.getValue(..) returns a float between 0 and 1
			// but we send back a value between 0 and 1023.
			unsigned short val = pe->getValue(pin) * 1023;
			unsigned char val1 = val >> 7; 	// MSB
			unsigned char val2 = val & 127;	// LSB
			Serial.write(AS_ARGUMENT(val1));            
			Serial.write(AS_ARGUMENT(val2));
		}
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
		case RQ_START_REPORTING:
			executeStartReporting();
			break;
		case RQ_STOP_REPORTING:
			executeStopReporting();
			break;
	}
}

void executeSetProgram(void) {
	delete program;
	program = new Program(stream);
	program->configurePins(pe);
}

void executeSetValue(void) {
	unsigned char pin = stream->nextChar();
	// We receive a value between 0 and 255 but PE.setValue(..) expects 0..1
	float value = (float)stream->nextChar() / 255;
	
	pe->setValue(pin, value);
}

void executeSetMode(void) {
	unsigned char pin = stream->nextChar();
	unsigned char mode = stream->nextChar();
	
	pe->setMode(pin, mode);
}

void executeStartReporting(void) {
	reporting = 1;
}

void executeStopReporting(void) {
	reporting = 0;
}
