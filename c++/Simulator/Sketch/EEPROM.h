#pragma once

#define E2END 1023

class EEPROMClass {

public:
	unsigned char read(int);
	void write(int, unsigned char);

};

extern EEPROMClass EEPROM;