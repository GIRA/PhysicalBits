
#include "EEPROMWriter.h"

void EEPROMWriter::setPosition(int16 value)
{
	position = value;
}

int16 EEPROMWriter::getPosition()
{
	return position;
}

void EEPROMWriter::incrementPosition(void)
{
	position = positive_modulo(position + 1, EEPROM_SIZE);
}

void EEPROMWriter::nextPut(uint8 value)
{
	EEPROM.write(position, value);
	incrementPosition();
}