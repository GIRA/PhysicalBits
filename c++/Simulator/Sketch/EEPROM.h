#pragma once

#define E2END 1023

class EEPROMClass
{

public:
	EEPROMClass(void)
	{
		for (int i = 0; i < E2END + 1; i++) { bytes[i] = 255; }
		// HACK(Richo): To test if the EEPROMReader is working, I initialize the EEPROM with the default program
		unsigned char defaultProgram[] = { 
			0, 
			206, // BEGIN
			195, // PROGRAM_START
			2, 
			128, 0, 0, 0, 2, 8, 11, 14, 0, 0, 1, 80, 81, 255, 
			128, 0, 3, 232, 3, 12, 0, 1, 13, 0, 2, 80, 164, 2, 1, 81, 131, 2, 0, 81, 255, 
			207 // END
		};
		for (int i = 0; i < 40; i++) { bytes[i] = defaultProgram[i]; }
	}

	unsigned char read(int);
	void write(int, unsigned char);

private:
	unsigned char bytes[E2END + 1];

};

extern EEPROMClass EEPROM;