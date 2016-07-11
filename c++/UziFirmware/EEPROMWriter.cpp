
#include "EEPROMWriter.h"

EEPROMWriter::EEPROMWriter()
{
	_position = 0;
	_nextPointer = 0;
	_offset = findOffset();
	jumpOverLastProgram();
}

int EEPROMWriter::findOffset(void)
{
	// The 0 position is always a pointer
	int index = 0;
	do
	{
		int val = EEPROM.read(index);
		if (val == 0)
		{
			// We found the offset
			return index;
		}
		index = index + val;
	}
	while (index < EEPROM_SIZE);
	return 0;
}

void EEPROMWriter::incrementPosition(void)
{
	_position++;
	if (actualIndex() == _nextPointer)
	{
		_nextPointer += EEPROM.read(actualIndex());
		incrementPosition();
	}
}

int EEPROMWriter::actualIndex(void)
{
	return (_offset + _position) % EEPROM_SIZE;
}

/*
 * We did a full round trip, so now we need to fix the pointer chain.
 */
void EEPROMWriter::fixPointers(void)
{
	_offset = (_offset + 1) % EEPROM_SIZE;
	if (_offset > 255)
	{
		// Special case. 
		// For now we just set it as the max posible value.
		// ACAACA Richo: Think about this more! You lazy bastard...
		_offset = 255;
	}
	// The first byte is always a pointer
	EEPROM.write(0, _offset);
	// The next pointer is the offset itself
	EEPROM.write(_offset, 0);
}

void EEPROMWriter::nextPut(unsigned char value)
{
	if (EEPROM.read(_position) != value)
	{
		EEPROM.write(_position, value);
	}
	_position++;
}

void EEPROMWriter::jumpOverLastProgram(void)
{}
