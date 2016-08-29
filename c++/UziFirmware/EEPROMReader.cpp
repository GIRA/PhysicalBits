
#include "EEPROMReader.h"


EEPROMReader::EEPROMReader(void)
{
	position = 0;
}

EEPROMReader::EEPROMReader(int16 position)
{
	EEPROMReader::position = position;
}

int16 EEPROMReader::getPosition(void)
{
	return position;
}

void EEPROMReader::incrementPosition(void)
{
	position = positive_modulo(position + 1, EEPROM_SIZE);
}

void EEPROMReader::decrementPosition(void)
{
	position = positive_modulo(position - 1, EEPROM_SIZE);
}

uint8 EEPROMReader::next(bool& timeout)
{
	timeout = false;
	uint8 result = EEPROM.read(position);
	incrementPosition();
	return result;
}

uint8 EEPROMReader::next(void)
{
	bool timeout;
	return next(timeout);
}

uint8 EEPROMReader::peek(void)
{
	return EEPROM.read(position);
}

uint8 EEPROMReader::back(void)
{
	decrementPosition();
	return EEPROM.read(position);
}

uint8 EEPROMReader::peekBack(void)
{
	uint8 result = back();
	incrementPosition();
	return result;
}