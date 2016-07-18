
#include "EEPROMWriter.h"

EEPROMWriter::EEPROMWriter()
{
	position = 0;
}

EEPROMWriter::EEPROMWriter(int position)
{
	EEPROMWriter::position = position;
}

int EEPROMWriter::getPosition()
{
	return position;
}

void EEPROMWriter::incrementPosition(void)
{
	position = positive_modulo(position + 1, EEPROM_SIZE);
}

void EEPROMWriter::nextPut(unsigned char value)
{
	EEPROM.write(position, value);
	incrementPosition();
}