
#include "EEPROMWriter.h"

EEPROMWriter::EEPROMWriter()
{
	position = 0;
}

EEPROMWriter::EEPROMWriter(int16 position)
{
	EEPROMWriter::position = position;
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