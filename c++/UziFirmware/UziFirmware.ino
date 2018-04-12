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
	monitor.checkForIncomingMessages(&program, &io, &vm);
	Error result = vm.executeProgram(program, &io);
	if (result != NO_ERROR)
	{
		monitor.sendError(result);
		uzi_memreset();
		program = uzi_create(Program);
	}
	monitor.sendOutgoingMessages(program, &io, &vm);
}