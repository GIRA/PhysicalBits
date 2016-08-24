#include <EEPROM.h>
#include "VM.h"
#include "SerialReader.h"
#include "EEPROMWearLevelingWriter.h"
#include "EEPROMWearLevelingReader.h"

#define MAJOR_VERSION		0
#define MINOR_VERSION		0

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
#define RQ_RUN_PROGRAM									9
#define RQ_SET_GLOBAL								   10

/* RESPONSE COMMANDS */
#define RS_ERROR										0
#define RS_PIN_VALUE									1
#define RS_PROFILE										2

/* OTHER CONSTANTS */
#define PROGRAM_START 					(unsigned char)0xC3
#define REPORT_INTERVAL									100
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
inline void loadProgramFromReader(Reader*);
inline void loadInstalledProgram(void);
inline void initSerial(void);
inline void checkForIncomingMessages(void);
inline void sendProfile(void);
inline void sendReport(void);
inline void executeProfile(void);
inline void executeRunProgram(void);
inline void executeSetGlobal(void);

void setup()
{
	loadInstalledProgram();
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

void loadInstalledProgram(void)
{
	EEPROMWearLevelingReader eeprom;
	if (eeprom.next() == PROGRAM_START
		&& eeprom.next() == MAJOR_VERSION)
	{
		loadProgramFromReader(&eeprom);
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
		bool timeout;
		unsigned char in = stream->next(timeout);
		if (!timeout) { executeCommand(in); }
	}
}

void sendProfile()
{
	if (!profiling) return;
	unsigned long now = millis();
	tickCount++;
	if (now - lastTimeProfile > 100)
	{
		Serial.write(RS_PROFILE);

		unsigned short val = tickCount;
		unsigned char val1 = val >> 7;  // MSB
		unsigned char val2 = val & 127; // LSB
		Serial.write(val1);
		Serial.write(val2);

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
	Serial.write(RS_ERROR);
	Serial.write(errorCode);
}

void sendPinValues(void)
{
	int count = 0;
	for (int i = 0; i < TOTAL_PINS; i++)
	{
		int pin = PIN_NUMBER(i);
		if (io->getReport(pin))
		{
			count++;
		}
	}
	if (count == 0) return;
	
	Serial.write(RS_PIN_VALUE);
	Serial.write(count);
	for (int i = 0; i < TOTAL_PINS; i++)
	{
		int pin = PIN_NUMBER(i);
		if (io->getReport(pin))
		{
			// GPIO.getValue(..) returns a float between 0 and 1
			// but we send back a value between 0 and 1023.
			unsigned short val = (unsigned short)(io->getValue(pin) * 1023);
			unsigned char val1 = (pin << 2) | (val >> 8); 	// MSB
			unsigned char val2 = val & 0xFF;	// LSB
			Serial.write(val1);
			Serial.write(val2);
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
		case RQ_RUN_PROGRAM:
			executeRunProgram();
			break;
		case RQ_SET_GLOBAL:
			executeSetGlobal();
			break;
	}
}

void executeSetProgram(void)
{
	loadProgramFromReader(stream);
}

void executeSetValue(void)
{
	bool timeout;

	unsigned char pin = stream->next(timeout);
	if (timeout) return;

	// We receive a value between 0 and 255 but GPIO.setValue(..) expects 0..1
	float value = (float)stream->next(timeout) / 255;
	if (timeout) return;

	io->setValue(pin, value);
}

void executeSetMode(void)
{
	bool timeout;

	unsigned char pin = stream->next(timeout);
	if (timeout) return;
	unsigned char mode = stream->next(timeout);
	if (timeout) return;

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
	bool timeout;
	unsigned char pin = stream->next(timeout);
	if (timeout) return;
	unsigned char report = stream->next(timeout);
	if (timeout) return;

	io->setReport(pin, report != 0);
}

void executeSaveProgram(void)
{
	bool timeout;
	long size = stream->nextLong(2, timeout);
	if (timeout) return;

	EEPROMWearLevelingWriter writer;
	writer.nextPut(PROGRAM_START);
	writer.nextPut(MAJOR_VERSION);
	for (int i = 0; i < size; i++)
	{
		writer.nextPut(stream->next(timeout));
		if (timeout) return; // TODO(Richo): What happens if we don't close the writer?
							 // The program we write should be invalid, how do we enforce that?
	}
	writer.close();
}

void executeKeepAlive(void)
{
	lastTimeKeepAlive = millis();
}

void executeProfile(void)
{
	bool timeout;
	profiling = stream->next(timeout);
	if (timeout) return;

	tickCount = 0;
	lastTimeProfile = millis();
}

void executeRunProgram(void)
{
	bool timeout;
	Program program(stream, timeout);
	if (timeout) return;
	vm->executeProgram(&program, io);
}

void loadProgramFromReader(Reader* reader)
{
	bool timeout;
	Program* p = new Program(reader, timeout);
	if (!timeout)
	{
		delete program;
		program = p;
		io->reset();
	}
}

void executeSetGlobal(void) 
{
	bool timeout;
	unsigned char index = stream->next(timeout);
	if (timeout) return;
	float value = stream->nextFloat(timeout);
	if (timeout) return;

	program->setGlobal(index, value);
}