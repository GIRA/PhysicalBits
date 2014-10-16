#include "StackProgram.h"

StackProgram::StackProgram(ReadStream * rs) {
    long n = rs->nextLong(4);
	_stepping = (n >> 31) & 1;
	_stepTime = n & 0x7FFFFFFF;
	_lastStepTime = 0;
	
	_literals = parseSection(rs);
	_locals = parseSection(rs);
	_bytecodes = parseBytecodes(rs);
}

StackProgram::StackProgram() {
	// Returns a NOOP program.
	_stepping = false;
	_stepTime = _lastStepTime = 0;
	_literals = new long[0];
	_locals = new long[0];
	_bytecodes = new unsigned char[1];
	_bytecodes[0] = 0xFF;
}

StackProgram::~StackProgram(void) {
	delete[] _literals;
	delete[] _locals;
	delete[] _bytecodes;
}

long StackProgram::literalAt(int index) {
	return _literals[index];
}

long StackProgram::localAt(int index) {
	return _locals[index];
}

unsigned char StackProgram::bytecodeAt(int index) {
	return _bytecodes[index];
}

void StackProgram::rememberLastStepTime(long now) {
	_lastStepTime = now;
}

bool StackProgram::shouldStepNow(long now) {
	return (now - _lastStepTime) > _stepTime;
}

bool StackProgram::isStepping(void) {
	return _stepping;
}

void StackProgram::setStepping(bool val) {
	_stepping = val;
}

long StackProgram::stepTime(void) {
	return _stepTime;
}

long * StackProgram::parseSection(ReadStream * rs) {
	unsigned char size = rs->nextChar();
	long * result = new long[size];
	int i = 0;
	while (i < size) {
		unsigned char sec = rs->nextChar();
		int count = (sec >> 2) & 0x3F;
		int size = (sec & 0x03) + 1;
		while (count > 0) {
			result[i] = rs->nextLong(size);
			count--;
			i++;
		}
	}
	return result;
}

unsigned char * StackProgram::parseBytecodes(ReadStream * rs) {
	return rs->upTo(0xFF, true);
}
