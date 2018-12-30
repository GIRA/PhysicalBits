#include "Monitor.h"

/* VERSION NUMBER */
#define MAJOR_VERSION                                       0
#define MINOR_VERSION                                       6

/* INCOMING */
#define MSG_IN_CONNECTION_REQUEST                         255
#define MSG_IN_SET_PROGRAM                                  0
#define MSG_IN_SET_VALUE                                    1
#define MSG_IN_SET_MODE                                     2
#define MSG_IN_START_REPORTING                              3
#define MSG_IN_STOP_REPORTING                               4
#define MSG_IN_SET_REPORT                                   5
#define MSG_IN_SAVE_PROGRAM                                 6
#define MSG_IN_KEEP_ALIVE                                   7
#define MSG_IN_PROFILE                                      8
// TODO(Richo): Available spot here!
#define MSG_IN_SET_GLOBAL                                  10
#define MSG_IN_SET_GLOBAL_REPORT                           11
#define MSG_IN_DEBUG_CONTINUE							   12
#define MSG_IN_DEBUG_SET_BREAKPOINTS					   13
#define MSG_IN_DEBUG_SET_BREAKPOINTS_ALL				   14

/* OUTGOING */
#define MSG_OUT_ERROR                                       0
#define MSG_OUT_PIN_VALUE                                   1
#define MSG_OUT_PROFILE                                     2
#define MSG_OUT_GLOBAL_VALUE                                3
#define MSG_OUT_TRACE                                       4
#define MSG_OUT_COROUTINE_STATE                             5
#define MSG_OUT_TICKING_SCRIPTS                             6
#define MSG_OUT_FREE_RAM                                    7
#define MSG_OUT_SERIAL_TUNNEL                               8

/* OTHER CONSTANTS */
#define PROGRAM_START                             (uint8)0xC3
#define REPORT_INTERVAL                                   100
#define KEEP_ALIVE_INTERVAL                              1000

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


void Monitor::checkForIncomingMessages(Program** program, GPIO* io, VM* vm)
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
		executeCommand(program, io, vm);
	}
}

void Monitor::connectionRequest()
{
	bool timeout;
	uint8 in;
		
	in = stream.next(timeout);
	if (timeout || in != MSG_IN_CONNECTION_REQUEST) return;
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

void Monitor::sendOutgoingMessages(Program* program, GPIO* io, VM* vm)
{
	checkKeepAlive();
	if (state == CONNECTED) 
	{
		sendReport(io, program);
		sendProfile();
		sendVMState(program, vm);
	}
}

void Monitor::sendError(uint8 coroutineIndex, uint8 errorCode)
{
	Serial.write(MSG_OUT_ERROR);
	Serial.write(coroutineIndex);
	Serial.write(errorCode);
}

void Monitor::sendError(uint8 errorCode)
{
	/*
	INFO(Richo): Since this is a standalone error we send 255 as coroutine index.
	*/
	sendError(255, errorCode);
}

void Monitor::sendProfile()
{
	if (!profiling) return;
	uint32 now = millis();
	tickCount++;
	if (now - lastTimeProfile > 100)
	{
		Serial.write(MSG_OUT_PROFILE);

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

void Monitor::sendVMState(Program* program, VM* vm)
{
	if (vm->halted && !sent) 
	{
		sent = true;

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
				Serial.write(MSG_OUT_COROUTINE_STATE);
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
		sendError(DISCONNECT_ERROR);
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

	Serial.write(MSG_OUT_PIN_VALUE);

	uint32 time = millis();
	Serial.write((time >> 24) & 0xFF);
	Serial.write((time >> 16) & 0xFF);
	Serial.write((time >> 8) & 0xFF);
	Serial.write(time & 0xFF);

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

	Serial.write(MSG_OUT_GLOBAL_VALUE);
	
	uint32 time = millis();
	Serial.write((time >> 24) & 0xFF);
	Serial.write((time >> 16) & 0xFF);
	Serial.write((time >> 8) & 0xFF);
	Serial.write(time & 0xFF);

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
	Serial.write(MSG_OUT_TICKING_SCRIPTS);
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
	Serial.write(MSG_OUT_FREE_RAM);
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


void Monitor::executeCommand(Program** program, GPIO* io, VM* vm)
{
	bool timeout;
	uint8 cmd = stream.next(timeout);
	if (timeout) return;

	switch (cmd)
	{
	case MSG_IN_SET_PROGRAM:
		// TODO(Richo): Refactor this. I added it because the VM state must be reset if the program changes!
		vm->halted = false;
		vm->breakpointPC = -1;
		executeSetProgram(program, io);
		break;
	case MSG_IN_SET_VALUE:
		executeSetValue(io);
		break;
	case MSG_IN_SET_MODE:
		executeSetMode(io);
		break;
	case MSG_IN_START_REPORTING:
		executeStartReporting();
		break;
	case MSG_IN_STOP_REPORTING:
		executeStopReporting();
		break;
	case MSG_IN_SET_REPORT:
		executeSetReport(io);
		break;
	case MSG_IN_SAVE_PROGRAM:
		// TODO(Richo): Refactor this. I added it because the VM state must be reset if the program changes!
		vm->halted = false;
		vm->breakpointPC = -1;
		executeSaveProgram(program, io);
		break;
	case MSG_IN_KEEP_ALIVE:
		executeKeepAlive();
		break;
	case MSG_IN_PROFILE:
		executeProfile();
		break;
	case MSG_IN_SET_GLOBAL:
		executeSetGlobal(*program);
		break;
	case MSG_IN_SET_GLOBAL_REPORT:
		executeSetGlobalReport(*program);
		break;
	case MSG_IN_DEBUG_CONTINUE:
		executeDebugContinue(vm);
		break;
	case MSG_IN_DEBUG_SET_BREAKPOINTS:
		executeDebugSetBreakpoints(*program);
		break;
	case MSG_IN_DEBUG_SET_BREAKPOINTS_ALL:
		executeDebugSetBreakpointsAll(*program);
		break;
	default:
		// TODO(Richo): Return MSG_OUT_ERROR
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

void Monitor::executeSaveProgram(Program** program, GPIO* io)
{
	bool timeout;
	int32 size = stream.nextLong(2, timeout);
	if (timeout) return;

	// Load the data into a buffer
	uzi_memreset();
	uint8* buf = uzi_createArray(uint8, size);
	for (int i = 0; i < size; i++)
	{
		buf[i] = stream.next(timeout);
		if (timeout)
		{
			// If we can't read expected number of bytes, reset program
			uzi_memreset();
			*program = uzi_create(Program);
			return;
		}
	}

	// Write the buffer into the EEPROM
	EEPROMWearLevelingWriter writer;
	writer.nextPut(PROGRAM_START);
	writer.nextPut(MAJOR_VERSION);
	writer.nextPut(MINOR_VERSION);
	for (int i = 0; i < size; i++)
	{
		writer.nextPut(buf[i]);
	}
	writer.close();

	// Read the program from the EEPROM
	io->reset();
	uzi_memreset();
	loadInstalledProgram(program);
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

void Monitor::executeDebugContinue(VM* vm) 
{
	vm->halted = false;
	sent = false;
}

void Monitor::executeDebugSetBreakpoints(Program* program) 
{
	bool timeout;
	uint8 count = stream.next(timeout);
	if (timeout) return;

	for (uint16 i = 0; i < count; i++) 
	{
		int16 pc = stream.nextLong(2, timeout);
		if (timeout) return;
		bool val = stream.next(timeout);
		if (timeout) return;

		program->getScriptForPC(pc)->setBreakpointAt(pc, val);
	}
}

void Monitor::executeDebugSetBreakpointsAll(Program* program)
{
	bool timeout;
	bool val = stream.next(timeout);
	if (timeout) return;

	int16 count = program->getScriptCount();
	for (int16 i = 0; i < count; i++) 
	{
		Script* script = program->getScript(i);
		for (int16 pc = script->getInstructionStart(); pc <= script->getInstructionStop(); pc++) 
		{
			script->setBreakpointAt(pc, val);
		}
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
	Serial.write(MSG_OUT_TRACE);
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
		Serial.write(MSG_OUT_SERIAL_TUNNEL);
	}
	Serial.write(value);
}