#include "Monitor.h"

/* VERSION NUMBER */
#define MAJOR_VERSION                                   0
#define MINOR_VERSION                                   4

/* REQUEST COMMANDS */
#define RQ_CONNECTION_REQUEST                         255
#define RQ_SET_PROGRAM                                  0
#define RQ_SET_VALUE                                    1
#define RQ_SET_MODE                                     2
#define RQ_START_REPORTING                              3
#define RQ_STOP_REPORTING                               4
#define RQ_SET_REPORT                                   5
#define RQ_SAVE_PROGRAM                                 6
#define RQ_KEEP_ALIVE                                   7
#define RQ_PROFILE                                      8
#define RQ_RUN_PROGRAM                                  9
#define RQ_SET_GLOBAL                                  10
#define RQ_SET_GLOBAL_REPORT                           11
#define RQ_SET_BREAK_COUNT                             12

/* RESPONSE COMMANDS */
#define RS_ERROR                                        0
#define RS_PIN_VALUE                                    1
#define RS_PROFILE                                      2
#define RS_GLOBAL_VALUE                                 3
#define RS_TRACE                                        4
#define RS_COROUTINE_STATE                              5
#define RS_TICKING_SCRIPTS                              6
#define RS_FREE_RAM                                     7
#define RS_SERIAL_TUNNEL                                8

/* OTHER CONSTANTS */
#define PROGRAM_START                         (uint8)0xC3
#define REPORT_INTERVAL                               100
#define KEEP_ALIVE_INTERVAL                           150

void Monitor::loadInstalledProgram(Program** program)
{
	EEPROMWearLevelingReader eeprom;
	if (eeprom.next() == PROGRAM_START
		&& eeprom.next() == MAJOR_VERSION
		&& eeprom.next() == MINOR_VERSION)
	{
		loadProgramFromReader(&eeprom, program);
	}
	else
	{
		uzi_memreset();
		*program = uzi_create(Program);
	}
}

void Monitor::initSerial()
{
	Serial.begin(57600);
}


void Monitor::checkForIncomingMessages(Program** program, GPIO* io)
{
	if (!Serial.available()) return;
	
	if (state == DISCONNECTED)
	{
		connectionRequest();
	}
	else if (state == CONNECTION_REQUESTED) 
	{
		acceptConnection();
	}
	else if (state == CONNECTED)
	{
		executeCommand(program, io);
	}
}

void Monitor::connectionRequest()
{
	bool timeout;
	uint8 in;
		
	in = stream.next(timeout);
	if (timeout || in != RQ_CONNECTION_REQUEST) return;
	in = stream.next(timeout);
	if (timeout || in != MAJOR_VERSION) return;
	in = stream.next(timeout);
	if (timeout || in != MINOR_VERSION) return;

	handshake = millis() % 256;
	Serial.write(handshake);
	state = CONNECTION_REQUESTED;
}

void Monitor::acceptConnection()
{
	bool timeout;
	uint8 in = stream.next(timeout);
	if (timeout) return;

	uint8 expected = (MAJOR_VERSION + MINOR_VERSION + handshake) % 256;
	if (in != expected) return;

	state = CONNECTED; 
	executeKeepAlive();
	Serial.write(expected);
}

void Monitor::sendOutgoingMessages(Program* program, GPIO* io) 
{
	checkKeepAlive();
	if (state == CONNECTED) 
	{
		sendReport(io, program);
		sendProfile();
		sendVMState(program);
	}
}

void Monitor::sendError(uint8 coroutineIndex, uint8 errorCode)
{
	Serial.write(RS_ERROR);
	Serial.write(coroutineIndex);
	Serial.write(errorCode);
}

void Monitor::sendError(uint8 errorCode)
{
	sendError(255, errorCode);
}

void Monitor::sendProfile()
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

void Monitor::sendReport(GPIO* io, Program* program)
{
	if (!reporting) return;
	uint32 now = millis();
	if (now - lastTimeReport > REPORT_INTERVAL)
	{
		sendPinValues(io);
		sendGlobalValues(program);
		sendTickingScripts(program);
		sendFreeRAM();
		lastTimeReport = now;
	}
}

void Monitor::sendVMState(Program* program)
{
	uint8 count = program->getScriptCount();
	for (uint8 i = 0; i < count; i++)
	{
		Script* script = program->getScript(i);
		if (script->hasCoroutine())
		{
			Coroutine* coroutine = script->getCoroutine();
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
		}
	}
}

void Monitor::checkKeepAlive()
{
	if (state == CONNECTED && 
		millis() - lastTimeKeepAlive > KEEP_ALIVE_INTERVAL)
	{
		state = DISCONNECTED;
	}
}

void Monitor::sendPinValues(GPIO* io)
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

void Monitor::sendGlobalValues(Program* program)
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

void Monitor::sendTickingScripts(Program* program)
{
	Serial.write(RS_TICKING_SCRIPTS);
	uint8 scriptCount = program->getScriptCount();
	Serial.write(scriptCount);
	uint8 result = 0;
	int8 bit = -1;
	for (int16 i = 0; i < scriptCount; i++)
	{
		Script* script = program->getScript(i);
		uint8 isStepping = script->isStepping() ? 1 : 0;
		result |= isStepping << ++bit;
		if (bit == 7)
		{
			Serial.write(result);
			bit = -1;
			result = 0;
		}
	}
	if (bit != -1)
	{
		Serial.write(result);
	}
}

/*
INFO(Richo): This function returns the space between the heap and the stack.
Taken from: https://learn.adafruit.com/memories-of-an-arduino/measuring-free-memory#sram
*/
int freeRam()
{
	extern unsigned int __heap_start;
	extern void*__brkval;
	int v;
	return (int)&v - (__brkval == 0 ? (int)&__heap_start : (int)__brkval);
}

void Monitor::sendFreeRAM()
{
	Serial.write(RS_FREE_RAM);
	{
		uint32 value = freeRam();
		Serial.write((value >> 24) & 0xFF);
		Serial.write((value >> 16) & 0xFF);
		Serial.write((value >> 8) & 0xFF);
		Serial.write(value & 0xFF);
	}
	{
		uint32 value = uzi_available();
		Serial.write((value >> 24) & 0xFF);
		Serial.write((value >> 16) & 0xFF);
		Serial.write((value >> 8) & 0xFF);
		Serial.write(value & 0xFF);
	}
}


void Monitor::executeCommand(Program** program, GPIO* io)
{
	bool timeout;
	uint8 cmd = stream.next(timeout);
	if (timeout) return;

	switch (cmd)
	{
	case RQ_SET_PROGRAM:
		executeSetProgram(program, io);
		break;
	case RQ_SET_VALUE:
		executeSetValue(io);
		break;
	case RQ_SET_MODE:
		executeSetMode(io);
		break;
	case RQ_START_REPORTING:
		executeStartReporting();
		break;
	case RQ_STOP_REPORTING:
		executeStopReporting();
		break;
	case RQ_SET_REPORT:
		executeSetReport(io);
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
	case RQ_SET_GLOBAL:
		executeSetGlobal(*program);
		break;
	case RQ_SET_GLOBAL_REPORT:
		executeSetGlobalReport(*program);
		break;
	case RQ_SET_BREAK_COUNT:
		executeSetBreakCount(*program);
		break;
	default:
		// TODO(Richo): Return RS_ERROR
		break;
	}
}


void Monitor::executeSetProgram(Program** program, GPIO* io)
{
	io->reset();
	loadProgramFromReader(&stream, program);
}

void Monitor::executeSetValue(GPIO* io)
{
	bool timeout;

	uint8 pin = stream.next(timeout);
	if (timeout) return;

	// We receive a value between 0 and 255 but GPIO.setValue(..) expects 0..1
	float value = (float)stream.next(timeout) / 255;
	if (timeout) return;

	io->setValue(pin, value);
}

void Monitor::executeSetMode(GPIO* io)
{
	bool timeout;

	uint8 pin = stream.next(timeout);
	if (timeout) return;
	uint8 mode = stream.next(timeout);
	if (timeout) return;

	io->setMode(pin, mode);
}

void Monitor::executeStartReporting()
{
	reporting = 1;
}

void Monitor::executeStopReporting()
{
	reporting = 0;
}

void Monitor::executeSetReport(GPIO* io)
{
	bool timeout;
	uint8 pin = stream.next(timeout);
	if (timeout) return;
	uint8 report = stream.next(timeout);
	if (timeout) return;

	io->setReport(pin, report != 0);
}

void Monitor::executeSaveProgram(void)
{
	bool timeout;
	int32 size = stream.nextLong(2, timeout);
	if (timeout) return;
	// TODO(Richo): Remove this allocation!
	uint8* buf = new uint8[size];
	for (int i = 0; i < size; i++)
	{
		buf[i] = stream.next(timeout);
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

void Monitor::executeKeepAlive()
{
	lastTimeKeepAlive = millis();
}

void Monitor::executeProfile()
{
	bool timeout;
	profiling = stream.next(timeout);
	if (timeout) return;

	tickCount = 0;
	lastTimeProfile = millis();
}

void Monitor::executeSetGlobal(Program* program)
{
	bool timeout;
	uint8 index = stream.next(timeout);
	if (timeout) return;
	float value = stream.nextFloat(timeout);
	if (timeout) return;

	program->setGlobal(index, value);
}

void Monitor::executeSetGlobalReport(Program* program)
{
	bool timeout;
	uint8 index = stream.next(timeout);
	if (timeout) return;
	uint8 report = stream.next(timeout);
	if (timeout) return;

	program->setReport(index, report != 0);
}

void Monitor::executeSetBreakCount(Program* program)
{
	bool timeout;
	uint8 index = stream.next(timeout);
	if (timeout) return;
	int8 value = stream.next(timeout) - 127;
	if (timeout) return;

	Script* script = program->getScript(index);
	if (script != 0)
	{
		// TODO(Richo): Should I check that the script has a coroutine here?
		Coroutine* coroutine = script->getCoroutine();
		coroutine->setBreakCount(value);
	}
}

void Monitor::loadProgramFromReader(Reader* reader, Program** program)
{
	Error result = NO_ERROR;
	uzi_memreset();
	Program * p = uzi_create(Program);
	if (p == 0)
	{
		result = OUT_OF_MEMORY;
	}
	else
	{
		result = readProgram(reader, p);
	}

	if (result == NO_ERROR)
	{
		*program = p;
	}
	else
	{
		uzi_memreset();
		*program = uzi_create(Program);
		sendError(result);
	}
}

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

void Monitor::serialWrite(uint8 value) 
{
	if (state == CONNECTED) 
	{
		Serial.write(RS_SERIAL_TUNNEL);
	}
	Serial.write(value);
}