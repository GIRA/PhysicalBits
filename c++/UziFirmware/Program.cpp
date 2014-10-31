#include "Program.h"

Program::Program(ReadStream * rs) {
	/*
    long n = rs->nextLong(4);
	_stepping = (n >> 31) & 1;
	_stepTime = n & 0x7FFFFFFF;
	_lastStepTime = 0;
	
	_literals = parseSection(rs);
	_locals = parseSection(rs);
	_bytecodes = parseBytecodes(rs);*/
}

Program::Program() {
	// Returns a NOOP program.
	/*_stepping = false;
	_stepTime = _lastStepTime = 0;
	_literals = new long[0];
	_locals = new long[0];
	_bytecodes = new unsigned char[1];
	_bytecodes[0] = 0xFF;*/
}

Program::~Program(void) {
	/*delete[] _literals;
	delete[] _locals;
	delete[] _bytecodes;*/
}