#pragma once

const unsigned int MAX_SIZE = 100;

class StackArray
{

public:
	StackArray(void)
	{
		pointer = 0;
	}
	~StackArray(void) {}

	void push(float);
	float pop(void);
	float top(void);
	void reset(void);
	bool overflow(void);

private:

	float elements[MAX_SIZE];
	unsigned int pointer;

};

