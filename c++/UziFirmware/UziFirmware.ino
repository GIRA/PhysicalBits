#include <EEPROM.h>
#include "VM.h"
#include "SerialReader.h"
#include "EEPROMWearLevelingWriter.h"
#include "EEPROMWearLevelingReader.h"

/* REQUEST COMMANDS */
#define RQ_SET_PROGRAM									0
#define RQ_SET_VALUE									1
#define RQ_SET_MODE										2
#define RQ_START_REPORTING								3
#define RQ_STOP_REPORTING								4
#define RQ_SET_REPORT									5
#define RQ_SAVE_PROGRAM									6
#define RQ_KEEP_ALIVE									7
#define RQ_PROFILE										8

/* RESPONSE COMMANDS */
#define RS_ERROR										0
#define RS_PIN_VALUE									1
#define RS_PROFILE                    2

/* MACROS */
#define IS_COMMAND(x)						((x) >> 7 == 0)
#define IS_ARGUMENT(x)						((x) >> 7 == 1)
#define GET_COMMAND(x)									(x)
#define GET_ARGUMENT(x)							((x) & 127)
#define AS_COMMAND(x)									(x)
#define AS_ARGUMENT(x)							((x) | 128)

/* OTHER CONSTANTS */
#define PROGRAM_START 					(unsigned char)0xC3
#define REPORT_INTERVAL									 50
#define KEEP_ALIVE_INTERVAL							   2000

Program * program = new Program();
VM * vm = new VM();
GPIO * io = new GPIO();
Reader * stream = new SerialReader();

unsigned char reporting = 0;
unsigned long lastTimeReport = 0;
unsigned long lastTimeKeepAlive = 0;

unsigned char profiling = 0;
unsigned long lastTimeProfile = 0;
unsigned short tickCount = 0;

inline void executeCommand(unsigned char);
inline void executeSetProgram(void);
inline void executeSetValue(void);
inline void executeSetMode(void);
inline void executeStartReporting(void);
inline void executeStopReporting(void);
inline void executeSetReport(void);
inline void executeSaveProgram(void);
inline void executeKeepAlive(void);
inline void sendPinValues(void);
inline void sendError(unsigned char);
inline void installSavedProgram(void);
inline void initSerial(void);
inline void checkForIncomingMessages(void);
inline void sendProfile(void);
inline void sendReport(void);
inline void executeProfile(void);

void setup()
{
	installSavedProgram();
	initSerial();
}

void loop()
{
	checkForIncomingMessages();
	vm->executeProgram(program, io);
	sendReport();
	//checkKeepAlive();
	sendProfile();
}

void installSavedProgram(void)
{
	EEPROMWearLevelingReader eeprom;
	if (eeprom.next() == PROGRAM_START)
	{
		delete program;
		program = new Program(&eeprom);
		program->configurePins(io);
	}
}

void initSerial(void)
{
	Serial.begin(57600);
}

void checkForIncomingMessages(void)
{
	if (Serial.available())
	{
		unsigned char in = stream->next();
		executeCommand(in);
	}
}

void sendProfile()
{
	if (!profiling) return;
	unsigned long now = millis();
	tickCount++;
	if (now - lastTimeProfile > 100)
	{
		Serial.write(AS_COMMAND(RS_PROFILE));

		unsigned short val = tickCount;
		unsigned char val1 = val >> 7;  // MSB
		unsigned char val2 = val & 127; // LSB
		Serial.write(AS_ARGUMENT(val1));
		Serial.write(AS_ARGUMENT(val2));

		tickCount = 0;
		lastTimeProfile = now;
	}
}

void sendReport(void)
{
	if (!reporting) return;
	unsigned long now = millis();
	if (now - lastTimeReport > REPORT_INTERVAL)
	{
		sendPinValues();
		lastTimeReport = now;
	}
}

void checkKeepAlive(void)
{
	if (!reporting) return;
	unsigned long now = millis();
	if (now - lastTimeKeepAlive > KEEP_ALIVE_INTERVAL)
	{
		executeStopReporting();
	}
}

void sendError(unsigned char errorCode)
{
	Serial.write(AS_COMMAND(RS_ERROR));
	Serial.write(AS_ARGUMENT(errorCode));
}

void sendPinValues(void)
{
	for (int i = 0; i < TOTAL_PINS; i++)
	{
		int pin = PIN_NUMBER(i);
		if (io->getReport(pin))
		{
			Serial.write(AS_COMMAND(RS_PIN_VALUE));
			Serial.write(AS_ARGUMENT(pin));

			// GPIO.getValue(..) returns a float between 0 and 1
			// but we send back a value between 0 and 1023.
			unsigned short val = (unsigned short)(io->getValue(pin) * 1023);
			unsigned char val1 = val >> 7; 	// MSB
			unsigned char val2 = val & 127;	// LSB
			Serial.write(AS_ARGUMENT(val1));
			Serial.write(AS_ARGUMENT(val2));
		}
	}
}

void executeCommand(unsigned char cmd)
{
	switch (cmd)
	{
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
		case RQ_SET_REPORT:
			executeSetReport();
			break;
		case RQ_SAVE_PROGRAM:
			executeSaveProgram();
			break;
		case RQ_KEEP_ALIVE:
			executeKeepAlive();
			break;
		case RQ_PROFILE:
			executeProfile();
			break;
	}
}

void executeSetProgram(void)
{
	delete program;
	program = new Program(stream);
	program->configurePins(io);
}

void executeSetValue(void)
{
	unsigned char pin = stream->next();
	// We receive a value between 0 and 255 but GPIO.setValue(..) expects 0..1
	float value = (float)stream->next() / 255;

	io->setValue(pin, value);
}

void executeSetMode(void)
{
	unsigned char pin = stream->next();
	unsigned char mode = stream->next();

	io->setMode(pin, mode);
}

void executeStartReporting(void)
{
	reporting = 1;
}

void executeStopReporting(void)
{
	reporting = 0;
}

void executeSetReport(void)
{
	unsigned char pin = stream->next();
	unsigned char report = stream->next();

	io->setReport(pin, report != 0);
}

void executeSaveProgram(void)
{
	long size = stream->nextLong(2);

	EEPROMWearLevelingWriter writer;
	writer.nextPut(PROGRAM_START);
	for (int i = 0; i < size; i++)
	{
		writer.nextPut(stream->next());
	}
	writer.close();
}

void executeKeepAlive(void)
{
	lastTimeKeepAlive = millis();
}

void executeProfile(void)
{
	profiling = stream->next();
	tickCount = 0;
	lastTimeProfile = millis();
}