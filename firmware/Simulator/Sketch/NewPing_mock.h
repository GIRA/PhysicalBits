#pragma once

#include <stdint.h>

class NewPing
{
	public:
		NewPing(uint8_t trigger_pin, uint8_t echo_pin, unsigned int max_cm_distance = 500) {}
		unsigned long ping_cm(unsigned int max_cm_distance = 0) { return 0; }
};

