#include "ReadStream.h"

long ReadStream::nextLong(int size) {
	long result = 0;
	for (int i = size - 1; i >= 0; i--) {
		result |= ((unsigned long)nextChar() << (i * 8));
	}
	return result;
}
