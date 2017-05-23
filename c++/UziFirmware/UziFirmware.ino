#include "VM.h"
#include "SerialReader.h"
#include "EEPROMWearLevelingWriter.h"
#include "EEPROMWearLevelingReader.h"
#include "Errors.h"

#define MAJOR_VERSION		0
#define MINOR_VERSION		3

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
#define RQ_SET_GLOBAL_REPORT						   11
#define RQ_SET_BREAK_COUNT							   12

/* RESPONSE COMMANDS */
#define RS_ERROR										0
#define RS_PIN_VALUE									1
#define RS_PROFILE										2
#define RS_GLOBAL_VALUE									3
#define RS_TRACE										4
#define RS_COROUTINE_STATE								5
#define RS_TICKING_SCRIPTS								6

/* OTHER CONSTANTS */
#define PROGRAM_START 					(uint8)0xC3
#define REPORT_INTERVAL									100
#define KEEP_ALIVE_INTERVAL							   2000

Program * program = new Program();
VM * vm = new VM();
GPIO * io = new GPIO();
Reader * stream = new SerialReader();

uint8 reporting = 0;
uint32 lastTimeReport = 0;
uint32 lastTimeKeepAlive = 0;

uint8 profiling = 0;
uint32 lastTimeProfile = 0;
uint16 tickCount = 0;

inline void executeCommand(uint8);
inline void executeSetProgram(void);
inline void executeSetValue(void);
inline void executeSetMode(void);
inline void executeStartReporting(void);
inline void executeStopReporting(void);
inline void executeSetReport(void);
inline void executeSaveProgram(void);
inline void executeKeepAlive(void);
inline void executeSetBreakCount(void);
inline void sendPinValues(void);
inline void sendGlobalValues(void);
inline void sendTickingScripts(void);
inline void sendError(uint8, uint8);
inline void loadProgramFromReader(Reader*);
inline void loadInstalledProgram(void);
inline void initSerial(void);
inline void checkForIncomingMessages(void);
inline void sendProfile(void);
inline void sendReport(void);
inline void sendVMState(void);
inline void executeProfile(void);
inline void executeRunProgram(void);
inline void executeSetGlobal(void);
inline void executeSetGlobalReport(void);
inline void trace(const char*);

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
	sendVMState();
}

void loadInstalledProgram(void)
{
	EEPROMWearLevelingReader eeprom;
	if (eeprom.next() == PROGRAM_START
		&& eeprom.next() == MAJOR_VERSION
		&& eeprom.next() == MINOR_VERSION)
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
		uint8 in = stream->next(timeout);
		if (!timeout) { executeCommand(in); }
	}
}

void sendProfile()
{
	if (!profiling) return;
	uint32 now = millis();
	tickCount++;
	if (now - lastTimeProfile > 100)
	{
		Serial.write(RS_PROFILE);

		uint16 val = tickCount;
		uint8 val1 = val >> 7;  // MSB
		uint8 val2 = val & 127; // LSB
		Serial.write(val1);
		Serial.write(val2);

		tickCount = 0;
		lastTimeProfile = now;
	}
}

void sendReport(void)
{
	if (!reporting) return;
	uint32 now = millis();
	if (now - lastTimeReport > REPORT_INTERVAL)
	{
		sendPinValues();
		sendGlobalValues();
		sendTickingScripts();
		lastTimeReport = now;
	}
}

void sendVMState(void)
{
	uint8 count = program->getCoroutineCount();
	Coroutine* coroutine = program->getCoroutine();
	for (uint8 i = 0; i < count; i++)
	{
		if (coroutine->getError() != NO_ERROR)
		{
			sendError(i, coroutine->getError());
			coroutine->reset();
		}
		if (coroutine->getDumpState())
		{
			coroutine->clearDumpState();
			Serial.write(RS_COROUTINE_STATE);
			Serial.write(i);
			int16 pc = coroutine->getPC();
			uint8 val1 = pc >> 8 & 0xFF; // MSB
			uint8 val2 = pc & 0xFF;	// LSB
			Serial.write(val1);
			Serial.write(val2);
			uint8 fp = coroutine->getFramePointer();
			Serial.write(fp);
			uint16 stackSize = coroutine->getStackSize();
			Serial.write(stackSize);
			for (uint16 j = 0; j < stackSize; j++)
			{
				uint32 value = float_to_uint32(coroutine->getStackElementAt(j));
				Serial.write((value >> 24) & 0xFF);
				Serial.write((value >> 16) & 0xFF);
				Serial.write((value >> 8) & 0xFF);
				Serial.write(value & 0xFF);
			}
		}
		coroutine = coroutine->getNext();
	}
}

void checkKeepAlive(void)
{
	if (!reporting) return;
	uint32 now = millis();
	if (now - lastTimeKeepAlive > KEEP_ALIVE_INTERVAL)
	{
		executeStopReporting();
	}
}

void sendError(uint8 coroutineIndex, uint8 errorCode)
{
	Serial.write(RS_ERROR);
	Serial.write(coroutineIndex);
	Serial.write(errorCode);
}

void sendPinValues(void)
{
	uint8 count = 0;
	for (uint8 i = 0; i < TOTAL_PINS; i++)
	{
		uint8 pin = PIN_NUMBER(i);
		if (io->getReport(pin))
		{
			count++;
		}
	}
	if (count == 0) return;
	
	Serial.write(RS_PIN_VALUE);
	Serial.write(count);
	for (uint8 i = 0; i < TOTAL_PINS; i++)
	{
		uint8 pin = PIN_NUMBER(i);
		if (io->getReport(pin))
		{
			// GPIO.getValue(..) returns a float between 0 and 1
			// but we send back a value between 0 and 1023.
			uint16 val = (uint16)(io->getValue(pin) * 1023);
			uint8 val1 = (pin << 2) | (val >> 8); 	// MSB
			uint8 val2 = val & 0xFF;	// LSB
			Serial.write(val1);
			Serial.write(val2);
		}
	}
}

void sendGlobalValues(void)
{
	uint8 count = 0;
	for (uint8 i = 0; i < program->getGlobalCount(); i++)
	{
		if (program->getReport(i))
		{
			count++;
		}
	}
	if (count == 0) return;

	Serial.write(RS_GLOBAL_VALUE);
	Serial.write(count);
	for (uint8 i = 0; i < program->getGlobalCount(); i++)
	{
		if (program->getReport(i))
		{
			Serial.write(i);
			uint32 value = float_to_uint32(program->getGlobal(i));
			Serial.write((value >> 24) & 0xFF);
			Serial.write((value >> 16) & 0xFF);
			Serial.write((value >> 8) & 0xFF);
			Serial.write(value & 0xFF);
		}
	}
}

void sendTickingScripts(void)
{
	Serial.write(RS_TICKING_SCRIPTS);
	uint8 scriptCount = program->getScriptCount();
	Serial.write(scriptCount);
	Script* script = program->getScript();
	uint8 result = 0;
	int8 bit = -1;
	for (int16 i = 0; i < scriptCount; i++)
	{
		uint8 isStepping = script->isStepping() ? 1 : 0;
		result |= isStepping << ++bit;
		if (bit == 7)
		{
			Serial.write(result);
			bit = -1;
			result = 0;
		}
		script = script->getNext();
	}
	if (bit != -1)
	{
		Serial.write(result);
	}
}

void executeCommand(uint8 cmd)
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
		case RQ_SET_GLOBAL_REPORT:
			executeSetGlobalReport();
			break;
		case RQ_SET_BREAK_COUNT:
			executeSetBreakCount();
			break;
		default:
			// TODO(Richo): Return RS_ERROR
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

	uint8 pin = stream->next(timeout);
	if (timeout) return;

	// We receive a value between 0 and 255 but GPIO.setValue(..) expects 0..1
	float value = (float)stream->next(timeout) / 255;
	if (timeout) return;

	io->setValue(pin, value);
}

void executeSetMode(void)
{
	bool timeout;

	uint8 pin = stream->next(timeout);
	if (timeout) return;
	uint8 mode = stream->next(timeout);
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
	uint8 pin = stream->next(timeout);
	if (timeout) return;
	uint8 report = stream->next(timeout);
	if (timeout) return;

	io->setReport(pin, report != 0);
}

void executeSaveProgram(void)
{
	bool timeout;
	int32 size = stream->nextLong(2, timeout);
	if (timeout) return;
	uint8* buf = new uint8[size];
	for (int i = 0; i < size; i++)
	{
		buf[i] = stream->next(timeout);
		if (timeout)
		{
			delete[] buf;
			return;
		}
	}

	EEPROMWearLevelingWriter writer;
	writer.nextPut(PROGRAM_START);
	writer.nextPut(MAJOR_VERSION);
	writer.nextPut(MINOR_VERSION);
	for (int i = 0; i < size; i++)
	{
		writer.nextPut(buf[i]);
	}
	writer.close();
	delete[] buf;
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
	uint8 index = stream->next(timeout);
	if (timeout) return;
	float value = stream->nextFloat(timeout);
	if (timeout) return;

	program->setGlobal(index, value);
}

void executeSetGlobalReport(void)
{
	bool timeout;
	uint8 index = stream->next(timeout);
	if (timeout) return;
	uint8 report = stream->next(timeout);
	if (timeout) return;

	program->setReport(index, report != 0);
}

void executeSetBreakCount(void)
{
	bool timeout;
	uint8 index = stream->next(timeout);
	if (timeout) return;
	int8 value = stream->next(timeout) - 127;
	if (timeout) return;

	Coroutine* coroutine = program->getCoroutine(index);
	if (coroutine != 0)
	{
		coroutine->setBreakCount(value);
	}
}

// TODO(Richo): Move this to some other place so that I can access it from anywhere
void trace(const char* str)
{
	Serial.write(RS_TRACE);
	uint8 size = strlen(str);
	Serial.write(size);
	for (int i = 0; i < size; i++)
	{
		Serial.write(str[i]);
	}
}