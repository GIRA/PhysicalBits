#pragma once

#define E2END 1023

class EEPROMClass
{

public:
	EEPROMClass(void)
	{
		for (int i = 0; i < E2END + 1; i++) { bytes[i] = 255; }
	}

	unsigned char read(int);
	void write(int, unsigned char);

private:
	unsigned char bytes[E2END + 1];

};

extern EEPROMClass EEPROM;