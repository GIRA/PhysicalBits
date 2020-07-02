#pragma once

#include <stdint.h>

typedef int8_t int8;
typedef int16_t int16;
typedef int32_t int32;

typedef uint8_t uint8;
typedef uint16_t uint16;
typedef uint32_t uint32;

inline float uint32_to_float(uint32 value)
{
	union
	{
		float f;
		uint32 ul;
	} u;
	u.ul = value;
	return u.f;
}

inline uint32 float_to_uint32(float value)
{
	union
	{
		float f;
		uint32 ul;
	} u;
	u.f = value;
	return u.ul;
}