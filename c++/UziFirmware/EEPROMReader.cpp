
#include "EEPROMReader.h"


EEPROMReader::EEPROMReader(void)
{
	position = 0;
}

EEPROMReader::EEPROMReader(int position)
{
	EEPROMReader::position = position;
}

void EEPROMReader::incrementPosition(void)
{
	position = (position + 1) % EEPROM_SIZE;
}

unsigned char EEPROMReader::nextChar(void)
{
	unsigned char result = EEPROM.read(position);
	incrementPosition();
	return result;
}