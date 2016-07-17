#include "EEPROMWearLevelingWriter.h"


EEPROMWearLevelingWriter::EEPROMWearLevelingWriter()
{
	writer = new EEPROMWriter(findPosition());
	beginPosition = writer->getPosition();
	// Erase previous end mark and then write the begin mark"
	writer->nextPut(0);
	writer->nextPut(BEGIN_MARK);
}

EEPROMWearLevelingWriter::~EEPROMWearLevelingWriter()
{
	close();
	delete writer;
}

void EEPROMWearLevelingWriter::nextPut(unsigned char byte)
{
	if (atEnd())
	{
		// TODO(Richo): How to signal an exception?
		return;
	}
	else
	{
		escapeIfNecessary(byte);
		writer->nextPut(byte);
	}
}

bool EEPROMWearLevelingWriter::atEnd()
{
	return writer->getPosition() == beginPosition;
}

void EEPROMWearLevelingWriter::escapeIfNecessary(unsigned char byte)
{
	if (byte == BEGIN_MARK || byte == END_MARK)
	{
		writer->nextPut(byte);
	}
}

void EEPROMWearLevelingWriter::close()
{
	if (closed) return;
	closed = true;

	writer->nextPut(END_MARK);
	// Make sure it's not escaped
	if (EEPROM.read(writer->getPosition()) == END_MARK)
	{
		EEPROM.write(writer->getPosition(), 0);
	}
}

int EEPROMWearLevelingWriter::findPosition()
{
	EEPROMReader reader;
	int count, position;

	// Skip beginning end marks
	count = 0;
	while (count < EEPROM_SIZE && reader.peek() == END_MARK)
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
		if (reader.next() == END_MARK)
		{
			if (reader.peek() == END_MARK)
			{
				// It was escaped. Skip next
				reader.next();
			}
			else
			{
				// We found it! Break out of the loop
				position = reader.getPosition() - 1 % EEPROM_SIZE;
				break;
			}
		}
	}

	return position;
}