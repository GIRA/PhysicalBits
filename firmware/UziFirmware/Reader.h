#pragma once

#include <string.h>
#include "types.h"

class Reader
{

public:
	uint16 counter;

	virtual uint8 next(bool&) = 0;

	virtual int32 nextLong(int16, bool&);
	virtual float nextFloat(bool&);

	void resetCounter();
};

