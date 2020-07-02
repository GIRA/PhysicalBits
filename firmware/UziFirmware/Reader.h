#pragma once

#include <string.h>
#include "types.h"

class Reader
{

public:

	virtual uint8 next(bool&) = 0;

	virtual int32 nextLong(int16, bool&);
	virtual float nextFloat(bool&);
};

