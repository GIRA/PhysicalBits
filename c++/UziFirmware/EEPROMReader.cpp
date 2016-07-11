
#include "EEPROMReader.h"


EEPROMReader::EEPROMReader(void)
{
	_position = 0;
	_nextPointer = 0;
	_offset = findOffset();
}

int EEPROMReader::findOffset(void)
{
	// The 0 position is always a pointer
	int index = 0;
	{
		int val = EEPROM.read(index);
		if (val == 0)
		{
			// We found the start marker. The next byte is the
			// size of the block, the offset starts afterwards.
			return index + 2;
		}
		index += val;
	} while (index < EEPROM_SIZE);
	// No offset, what do we do?
	return -1;
}

void EEPROMReader::incrementPosition(void)
{
	_position++;
	if (actualIndex() == _nextPointer)
	{
		_nextPointer += EEPROM.read(actualIndex());
		incrementPosition();
	}
}

int EEPROMReader::actualIndex(void)
{
	return (_offset + _position) % EEPROM_SIZE;
}

unsigned char EEPROMReader::nextChar(void)
{
	unsigned char result = EEPROM.read(actualIndex());
	incrementPosition();
	return result;
}