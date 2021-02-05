#pragma once

#include "types.h"
#include "Arduino.h"
#include "UziSerial.h"
#include "SerialReader.h"
#include "VM.h"
#include "Program.h"
#include "Script.h"
#include "EEPROMWearLevelingWriter.h"
#include "EEPROMWearLevelingReader.h"

enum MonitorState
{
	DISCONNECTED,
	CONNECTION_REQUESTED,
	CONNECTED
};

class Monitor 
{
public:
	void loadInstalledProgram(Program** program);
	void initSerial(UziSerial*);
	void checkForIncomingMessages(Program** program, GPIO* io, VM* vm);
	void sendOutgoingMessages(Program* program, GPIO* io, VM* vm);
	void sendError(uint8 errorCode);

	void serialWrite(uint8 value);

private:
	UziSerial* serial;
	SerialReader stream;

	uint8 state = DISCONNECTED;
	uint8 handshake = 0;

	uint8 reportingStep = 0;
	uint32 lastTimeReport = 0;

	int8 keepAliveCounter = 0;
	uint32 lastTimeKeepAlive = 0;

	uint32 lastTimeProfile = 0;
	uint16 tickCount = 0;

	bool sent : 1; // TODO(Richo)
	bool profiling : 1;
	bool fixedReportInterval : 1;
	uint16 reportInterval : 13;
	// TODO(Richo): Add a separate middlewareReportInterval variable that sets the min value for 
	// the reportInterval if fixedReportInterval is set. This way, if the middleware is slow it
	// can specify a report interval, but if this value is still too much for the VM and performance
	// degrades it can still increase it to reach its goal. So actually, fixedReportInterval 
	// wouldn't even be necessary. The vm is free to inc/dec the reportInterval but without going
	// below the limit set by the middleware. If the middleware is fast, then this min would be 0.


	void connectionRequest();
	void acceptConnection();

	void executeCommand(Program** program, GPIO* io, VM* vm);
	void executeSetProgram(Program** program, GPIO* io);
	void executeSetValue(GPIO* io);
	void executeSetMode(GPIO* io);
	void executeStartReporting();
	void executeStopReporting();
	void executeSetReport(GPIO* io);
	void executeSaveProgram(Program** program, GPIO* io);
	void executeKeepAlive();
	void executeProfile();
	void executeSetReportInterval();
	void executeSetGlobal(Program* program);
	void executeSetGlobalReport(Program* program);
	void executeDebugContinue(VM* vm);
	void executeDebugSetBreakpoints(Program* program);
	void executeDebugSetBreakpointsAll(Program* program);

	void sendReport(GPIO* io, Program* program);
	void checkKeepAlive();
	void sendProfile();
	void sendVMState(Program* program, VM* vm);
	void sendCoroutineState(Program* program, Script* script);
	void sendTimestamp();
	void sendPinValues(GPIO* io);
	void sendGlobalValues(Program* program);
	void sendRunningTasks(Program* program);
	void sendFreeRAM();

	void loadProgramFromReader(Reader* reader, Program** program);
};

void trace(const char*);

