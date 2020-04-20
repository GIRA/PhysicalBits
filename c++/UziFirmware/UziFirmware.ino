#include "Program.h"
#include "Monitor.h"
#include "VM.h"
#include "GPIO.h"
#include "Memory.h"
#include "UziSerial.h"

UziSerial serial;
Monitor monitor;
VM vm;
GPIO io;
Program * program;

void setup()
{
	monitor.initSerial(&serial);
	monitor.loadInstalledProgram(&program);
}

void loop()
{
#ifdef __SIMULATOR__
	Stats.usedMemory = Stats.coroutineResizeCounter = 0;
#endif // __SIMULATOR__

	monitor.checkForIncomingMessages(&program, &io, &vm);
	Error result = vm.executeProgram(program, &io, &monitor);
	if (result != NO_ERROR)
	{
		monitor.sendError(result);
		uzi_memreset();
		program = uzi_create(Program);
	}
	monitor.sendOutgoingMessages(program, &io, &vm);

#ifdef __SIMULATOR__
	Stats.usedMemory = uzi_used();
#endif // __SIMULATOR__
}
