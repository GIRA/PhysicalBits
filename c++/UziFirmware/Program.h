#pragma once

#include "ReadStream.h"
#include "Script.h"

class Program {

public:
	Program(ReadStream*);
	Program(void);
	~Program(void);

private:
		
	unsigned char _pinModes[3];
	Script * _scripts;
};

