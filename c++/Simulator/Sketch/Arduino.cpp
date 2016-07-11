//--------------------------------------------------------------------
// Arduino Console Stub
//--------------------------------------------------------------------

#if _MSC_VER 

#include "Arduino.h"
#include <conio.h>
#include <time.h>


CSerial Serial;

//--------------------------------------------------------------------
// Timers
//--------------------------------------------------------------------

unsigned long millis()
{
	return (clock() * 1000) / CLOCKS_PER_SEC;
}

void delay(unsigned long delayms)
{
	unsigned long u = millis() + delayms;
	while (u > millis())
		;
}

//--------------------------------------------------------------------
// I/O
//--------------------------------------------------------------------

void pinMode(int, int)
{}

extern unsigned short analogRead(unsigned int pin)
{
	return __getPinValue(pin);
}

void analogWrite(unsigned int pin, unsigned short value)
{
	__setPinValue(pin, (unsigned short)round(value * 4.01176470588235));
}

void digitalWrite(int pin, int value)
{
	analogWrite(pin, value == 0 ? LOW : 255);
}

bool digitalRead(int pin)
{
	return analogRead(pin) == 0 ? LOW : HIGH;
}

//--------------------------------------------------------------------
// Serial
//--------------------------------------------------------------------

void CSerial::begin(long)
{
	in_buffer[0] = 0;
	in_buflen = 0;
}

void CSerial::print(char *pString)
{
	printf("%s", pString);
}

void CSerial::print(int value, int)
{
	printf("%d", value);
}

void CSerial::println()
{
	printf("\r\n");
}

void CSerial::println(char *pString)
{
	printf("%s\r\n", pString);
}

void CSerial::println(int value, int)
{
	printf("%d\r\n", value);
}

void CSerial::println(unsigned int value, int)
{
	printf("%u\r\n", value);
}

void CSerial::println(unsigned long value, int)
{
	printf("%lu\r\n", value);
}


int CSerial::available()
{
	return in_buflen;
}

char CSerial::read()
{
	char c = 0;
	if (in_buflen > 0)
	{
		c = in_buffer[0];
		memcpy(&in_buffer[0], &in_buffer[1], --in_buflen);
	}
	return c;
}

void CSerial::write(unsigned char c)
{
	printf("%c", c);
}

void CSerial::_append(char c)
{
	CSerial::in_buffer[in_buflen] = c;
	if (++in_buflen >= 1024)
	{
		in_buflen--;
	}
}

//--------------------------------------------------------------------
// Main
//--------------------------------------------------------------------

int main(int, char**)
{
	setup();
	for (;;)
	{
		if (_kbhit())
		{
			Serial._append((char)_getch());
		}
		loop();
	}
}

#endif
