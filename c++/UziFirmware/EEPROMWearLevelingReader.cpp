#include "EEPROMWearLevelingReader.h"


EEPROMWearLevelingReader::EEPROMWearLevelingReader()
{
	reader = new EEPROMReader(findPosition());
	reader->next(); // We know the next byte is the begin mark, so just skip it
}

EEPROMWearLevelingReader::~EEPROMWearLevelingReader()
{
	delete reader;
}

unsigned char EEPROMWearLevelingReader::next()
{
	if (atEnd())
	{
		return 0;
	}
	else
	{
		return escapeIfNecessary(reader->next());
	}
}

bool EEPROMWearLevelingReader::atEnd()
{
	return reader->getPosition() == endPosition;
}

unsigned char EEPROMWearLevelingReader::escapeIfNecessary(unsigned char byte)
{
	if ((byte == EEPROM_BEGIN_MARK && reader->peek() == EEPROM_BEGIN_MARK)
		|| (byte == EEPROM_END_MARK && reader->peek() == EEPROM_END_MARK))
	{
		reader->next();
	}
	return byte;
}

int EEPROMWearLevelingReader::findPosition()
{
	EEPROMReader reader;
	int count, position;

	// Skip beginning end marks
	count = 0;
	while (count < EEPROM_SIZE && reader.peek() == EEPROM_END_MARK)
	{
		// Skip this byte
		reader.next();
		count++;
	}

	// Now, look for the end mark on the entire memory
	count = 0;
	while (count <= EEPROM_SIZE)
	{
		count++;
		if (reader.next() == EEPROM_END_MARK)
		{
			if (reader.peek() == EEPROM_END_MARK)
			{
				// It was escaped. Skip next
				reader.next();
				count++;
			}
			else
			{
				// We found it! Break out of the loop
				break;
			}
		}
	}

	// Skip end mark and take note of its position
	reader.back();
	endPosition = reader.getPosition();

	// Finally, go back until you find the begin mark
	position = count = 0;
	while (count <= EEPROM_SIZE)
	{
		count++;
		if (reader.back() == EEPROM_BEGIN_MARK)
		{
			if (reader.peekBack() == EEPROM_BEGIN_MARK)
			{
				// It was escaped. Skip back
				reader.back();
				count++;
			}
			else
			{
				// We found it! Break out of the loop
				position = reader.getPosition();
				break;
			}
		}
	}

	return position;
}