#include "EEPROMWearLevelingWriter.h"


EEPROMWearLevelingWriter::EEPROMWearLevelingWriter()
{
	writer.setPosition(findPosition());
	beginPosition = writer.getPosition();
	// Erase previous end mark and then write the begin mark"
	writer.nextPut(0);
	writer.nextPut(EEPROM_BEGIN_MARK);
}

EEPROMWearLevelingWriter::~EEPROMWearLevelingWriter()
{
	close();
}

void EEPROMWearLevelingWriter::nextPut(uint8 byte)
{
	if (atEnd())
	{
		// TODO(Richo): How to signal an exception?
		return;
	}
	else
	{
		escapeIfNecessary(byte);
		writer.nextPut(byte);
	}
}

bool EEPROMWearLevelingWriter::atEnd()
{
	return writer.getPosition() == beginPosition;
}

void EEPROMWearLevelingWriter::escapeIfNecessary(uint8 byte)
{
	if (byte == EEPROM_BEGIN_MARK || byte == EEPROM_END_MARK)
	{
		writer.nextPut(byte);
	}
}

void EEPROMWearLevelingWriter::close()
{
	if (closed) return;
	closed = true;

	writer.nextPut(EEPROM_END_MARK);
	// Make sure it's not escaped
	if (EEPROM.read(writer.getPosition()) == EEPROM_END_MARK)
	{
		EEPROM.write(writer.getPosition(), 0);
	}
}

int16 EEPROMWearLevelingWriter::findPosition()
{
	EEPROMReader reader;
	int16 count, position;

	// Skip beginning end marks
	count = 0;
	while (count < EEPROM_SIZE && reader.peek() == EEPROM_END_MARK)
	{
		// Skip this byte
		reader.next();
		count++;
	}

	// Now, look for the end mark on the entire memory
	position = count = 0;
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
				reader.back();
				position = reader.getPosition();
				break;
			}
		}
	}

	return position;
}