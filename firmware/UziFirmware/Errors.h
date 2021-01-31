#pragma once


enum Error
{
	NO_ERROR					= 0,

	STACK_OVERFLOW				= 1,
	STACK_UNDERFLOW				= 2,
	ACCESS_VIOLATION		= 4,
	OUT_OF_MEMORY				= 8,
	READER_TIMEOUT				= 16,
	DISCONNECT_ERROR			= 32,
	READER_CHECKSUM_FAIL		= 64,
};

inline Error& operator|=(Error& a, Error b)
{
	return (Error&)((int&)a |= (int)b);
}
