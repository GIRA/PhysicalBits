//--------------------------------------------------------------------
// Arduino Console Stub
//--------------------------------------------------------------------

#if _MSC_VER 

#include "Arduino.h"
#include <conio.h>
#include <time.h>

unsigned char __pinModes__[__TOTAL_PINS__];
unsigned short __pinValues__[__TOTAL_PINS__];

extern void setup();
extern void loop();

CSerial Serial;

//--------------------------------------------------------------------
// Timers
//--------------------------------------------------------------------

unsigned long millis()
{
	return (clock() * 1000) /  CLOCKS_PER_SEC;
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

void pinMode(int,int)
{
}

extern unsigned short analogRead(unsigned int pin) {
	return __getPinValue(pin);
}

void analogWrite(unsigned int pin, unsigned short value) {
	__setPinValue(pin, round(value * 4.01176470588235));
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
	buffer[0] = 0;
	buflen = 0;
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
	return buflen;
}

char CSerial::read() 
{ 
	char c = 0;
	if (buflen > 0)
	{
		c = buffer[0];
		memcpy(&buffer[0], &buffer[1], --buflen);
	}
	return c;
}

void CSerial::write(unsigned char c) 
{
	_append(c);
}

void CSerial::_append(char c)
{
	CSerial::buffer[buflen] = c;
	if (++buflen >= 1024)
	{
		buflen--;
	}
}

//--------------------------------------------------------------------
// Main
//--------------------------------------------------------------------

int main(int, char**)
{
	setup();
	for(;;)
	{
		if (_kbhit())
		{
			Serial._append((char)_getch());
		}
		loop();
	}
}


unsigned short __getPinValue(unsigned int pin) {
	int index = pin;
	if (index < 0 || index >= __TOTAL_PINS__) {
		return 0;
	}
	return __pinValues__[index];
}

void __setPinValue(unsigned int pin, unsigned short value) {
	int index = pin;
	if (index < 0 || index >= __TOTAL_PINS__) {
		return;
	}
	__pinValues__[index] = value % 1024;
}

EXTERN long foo(void) {
	return 42;
}


EXTERN unsigned short getPinValue(unsigned int pin) {
	return __getPinValue(pin);
}

EXTERN void setPinValue(unsigned int pin, unsigned short value) {
	__setPinValue(pin, value);
}

EXTERN void start(void) {
	setup();
	for (;;)
		loop();
}
#endif
