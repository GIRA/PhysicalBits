#include "Program.h"
#include "Monitor.h"
#include "VM.h"
#include "GPIO.h"
#include "Memory.h"

Program * program;
Monitor monitor;
VM vm;
GPIO io;

void setup()
{
	monitor.initSerial();
	monitor.loadInstalledProgram(&program);
}

void loop()
{
#ifdef __SIMULATOR__
	Stats.availableMemory = Stats.coroutineResizeCounter = 0;
#endif // __SIMULATOR__

	monitor.checkForIncomingMessages(&program, &io);
	Error result = vm.executeProgram(program, &io, &monitor);
	if (result != NO_ERROR)
	{
		monitor.sendError(result);
		uzi_memreset();
		program = uzi_create(Program);
	}
	monitor.sendOutgoingMessages(program, &io);

#ifdef __SIMULATOR__
	Stats.availableMemory = uzi_available();
#endif // __SIMULATOR__
}