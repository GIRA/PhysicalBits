#include "Monitor.h"

/* VERSION NUMBER */
#define MAJOR_VERSION                                       0
#define MINOR_VERSION                                       7

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
#define KEEP_ALIVE_INTERVAL                                10
#define KEEP_ALIVE_COUNTER								  100

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

void Monitor::initSerial(UziSerial* s)
{
	serial = s;
	serial->begin();
	stream.init(serial);
}


void Monitor::checkForIncomingMessages(Program** program, GPIO* io, VM* vm)
{
	if (!serial->available()) return;
	
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
	serial->write(handshake);
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
	serial->write(expected);
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

void Monitor::sendError(uint8 errorCode)
{
	serial->write(MSG_OUT_ERROR);
	serial->write(errorCode);
}

void Monitor::sendProfile()
{
	if (!profiling) return;
	uint32 now = millis();
	tickCount++;
	if (now - lastTimeProfile > 100)
	{
		serial->write(MSG_OUT_PROFILE);

		uint16 val = tickCount;
		uint8 val1 = val >> 7;  // MSB
		uint8 val2 = val & 127; // LSB
		serial->write(val1);
		serial->write(val2);

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
		sendRunningTasks(program);
		sendFreeRAM();
		lastTimeReport = now;
	}
}

void Monitor::sendVMState(Program* program, VM* vm)
{
	if (vm->halted && !sent) 
	{
		sent = true;
		sendCoroutineState(program, vm->haltedScript);

		/*
		TODO(Richo): Send the state of all the coroutines?
		If we do that we need to make sure that all the coroutines are updated!
		We know that the halted script is updated because we make sure to save its
		state when we halt. But the others? I don't know...
		*/
	}
}

void Monitor::sendCoroutineState(Program* program, Script* script)
{
	if (script != NULL && script->hasCoroutine())
	{    
		uint8 scriptIndex = script->getIndex();
		Coroutine* coroutine = script->getCoroutine();
		if (coroutine->getError() != NO_ERROR)
		{
			// TODO(Richo): Is this really necessary?
			sendError(coroutine->getError());
			program->resetCoroutine(coroutine); // TODO(Richo): Why?
		}
		serial->write(MSG_OUT_COROUTINE_STATE);
		serial->write(scriptIndex);
		int16 pc = coroutine->getPC();
		uint8 val1 = pc >> 8 & 0xFF; // MSB
		uint8 val2 = pc & 0xFF;	// LSB
		serial->write(val1);
		serial->write(val2);
		uint8 fp = (uint8)coroutine->getFramePointer();
		serial->write(fp);
		uint8 stackSize = (uint8)coroutine->getStackSize();
		serial->write(stackSize);
		for (uint8 j = 0; j < stackSize; j++)
		{
			uint32 value = float_to_uint32(coroutine->getStackElementAt(j));
			serial->write((value >> 24) & 0xFF);
			serial->write((value >> 16) & 0xFF);
			serial->write((value >> 8) & 0xFF);
			serial->write(value & 0xFF);
		}
	}
}

void Monitor::checkKeepAlive()
{
	if (state != CONNECTED) return;
	int32 now = millis();
	if (now - lastTimeKeepAlive > KEEP_ALIVE_INTERVAL)
	{
		if (keepAliveCounter <= 0) 
		{
			state = DISCONNECTED;
			sendError(DISCONNECT_ERROR);
		}
		else 
		{
			keepAliveCounter--;
		}
		lastTimeKeepAlive = now;
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

	serial->write(MSG_OUT_PIN_VALUE);

	uint32 time = millis();
	serial->write((time >> 24) & 0xFF);
	serial->write((time >> 16) & 0xFF);
	serial->write((time >> 8) & 0xFF);
	serial->write(time & 0xFF);

	serial->write(count);
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
			serial->write(val1);
			serial->write(val2);
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

	serial->write(MSG_OUT_GLOBAL_VALUE);
	
	uint32 time = millis();
	serial->write((time >> 24) & 0xFF);
	serial->write((time >> 16) & 0xFF);
	serial->write((time >> 8) & 0xFF);
	serial->write(time & 0xFF);

	serial->write(count);
	for (uint8 i = 0; i < program->getGlobalCount(); i++)
	{
		if (program->getReport(i))
		{
			serial->write(i);
			uint32 value = float_to_uint32(program->getGlobal(i));
			serial->write((value >> 24) & 0xFF);
			serial->write((value >> 16) & 0xFF);
			serial->write((value >> 8) & 0xFF);
			serial->write(value & 0xFF);
		}
	}
}

void Monitor::sendRunningTasks(Program* program)
{
	serial->write(MSG_OUT_TICKING_SCRIPTS);
	uint8 scriptCount = program->getScriptCount();
	serial->write(scriptCount);
	for (int16 i = 0; i < scriptCount; i++)
	{
		Script* script = program->getScript(i);
		uint8 val = NO_ERROR;
		if (script->isRunning()) 
		{
			val |= 0b10000000;
		}
		if (script->hasCoroutine()) 
		{
			val |= (script->getCoroutine()->getError() & 0b01111111);
		}
		serial->write(val);
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
	serial->write(MSG_OUT_FREE_RAM);
	{
		uint32 value = freeRam();
		serial->write((value >> 24) & 0xFF);
		serial->write((value >> 16) & 0xFF);
		serial->write((value >> 8) & 0xFF);
		serial->write(value & 0xFF);
	}
	{
		uint32 value = uzi_available();
		serial->write((value >> 24) & 0xFF);
		serial->write((value >> 16) & 0xFF);
		serial->write((value >> 8) & 0xFF);
		serial->write(value & 0xFF);
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
		vm->reset();
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
		vm->reset();
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
	keepAliveCounter = KEEP_ALIVE_COUNTER;
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
	bool val = stream.next(timeout);
	if (timeout) return;
	uint8 count = stream.next(timeout);
	if (timeout) return;

	for (uint16 i = 0; i < count; i++) 
	{
		int16 pc = stream.nextLong(2, timeout);
		if (timeout) return;

		Script* script = program->getScriptForPC(pc);
		if (script != NULL)
		{
			script->setBreakpointAt(pc, val);
		}
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
	uint8 size = (uint8)strlen(str);
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
		serial->write(MSG_OUT_SERIAL_TUNNEL);
	}
	serial->write(value);
}
