#pragma once

#define E2END 1023

class EEPROMClass
{

public:
	unsigned char read(int);
	void write(int, unsigned char);

private:
	unsigned char bytes[E2END + 1] = { 255 };

};

extern EEPROMClass EEPROM;