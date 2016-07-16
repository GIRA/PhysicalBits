
#include "EEPROMWriter.h"

EEPROMWriter::EEPROMWriter()
{
	position = 0;
}

EEPROMWriter::EEPROMWriter(int position)
{
	EEPROMWriter::position = position;
}

void EEPROMWriter::incrementPosition(void)
{
	position = (position + 1) % EEPROM_SIZE;
}

void EEPROMWriter::nextPut(unsigned char value)
{
	EEPROM.write(position, value);
	incrementPosition();
}