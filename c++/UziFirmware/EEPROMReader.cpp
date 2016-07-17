
#include "EEPROMReader.h"


EEPROMReader::EEPROMReader(void)
{
	position = 0;
}

EEPROMReader::EEPROMReader(int position)
{
	EEPROMReader::position = position;
}

int EEPROMReader::getPosition(void)
{
	return position;
}

void EEPROMReader::incrementPosition(void)
{
	position = (position + 1) % EEPROM_SIZE;
}

void EEPROMReader::decrementPosition(void)
{
	position = (position - 1) % EEPROM_SIZE;
}

unsigned char EEPROMReader::next(void)
{
	unsigned char result = EEPROM.read(position);
	incrementPosition();
	return result;
}

unsigned char EEPROMReader::peek(void)
{
	return EEPROM.read(position);
}

unsigned char EEPROMReader::back(void)
{
	decrementPosition();
	return EEPROM.read(position);
}

unsigned char EEPROMReader::peekBack(void)
{
	unsigned char result = back();
	incrementPosition();
	return result;
}