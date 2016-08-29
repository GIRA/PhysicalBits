#pragma once

#include <string.h>
#include "types.h"

class Reader
{

public:

	virtual uint8 next(bool&) = 0;

	virtual uint8 * next(int16, bool&);
	virtual uint8 * upTo(uint8, bool, bool&);
	virtual int32 nextLong(int16, bool&);
	virtual float nextFloat(bool&);
};

