#pragma once

// HACK(Richo): This shouldn't be redefined...
#define E2END 255


class EEPROMClass {

public:
	unsigned char read(int);
	void write(int, unsigned char);

};

extern EEPROMClass EEPROM;