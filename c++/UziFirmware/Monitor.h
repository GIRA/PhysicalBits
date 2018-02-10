#pragma once

#include "types.h"
#include "Arduino.h"
#include "SerialReader.h"
#include "Program.h"
#include "Script.h"
#include "EEPROMWearLevelingWriter.h"
#include "EEPROMWearLevelingReader.h"
#include "VM.h"

class Monitor 
{
public: 
	void loadInstalledProgram(Program** program);
	void initSerial();
	void checkForIncomingMessages(Program** program, GPIO* io, VM* vm);
	void sendError(uint8 coroutineIndex, uint8 errorCode);
	void sendError(uint8 errorCode);
	void sendReport(GPIO* io, Program* program);
	void checkKeepAlive();
	void sendProfile();
	void sendVMState(Program* program);

private:
	SerialReader stream;

	uint8 reporting = 0;
	uint32 lastTimeReport = 0;
	uint32 lastTimeKeepAlive = 0;

	uint8 profiling = 0;
	uint32 lastTimeProfile = 0;
	uint16 tickCount = 0;

	void executeCommand(uint8 cmd, Program** program, GPIO* io, VM* vm);
	void executeSetProgram(Program** program, GPIO* io);
	void executeSetValue(GPIO* io);
	void executeSetMode(GPIO* io);
	void executeStartReporting();
	void executeStopReporting();
	void executeSetReport(GPIO* io);
	void executeSaveProgram();
	void executeKeepAlive();
	void executeProfile();
	void executeRunProgram(VM* vm, GPIO* io);
	void executeSetGlobal(Program* program);
	void executeSetGlobalReport(Program* program);
	void executeSetBreakCount(Program* program);

	void sendPinValues(GPIO* io);
	void sendGlobalValues(Program* program);
	void sendTickingScripts(Program* program);
	void sendFreeRAM();

	void loadProgramFromReader(Reader* reader, Program** program);
};

void trace(const char*);