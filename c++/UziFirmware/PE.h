#pragma once

// The following macros will work for standard arduino, other versions should redefine.
#define TOTAL_PINS											  18
#define PIN_INDEX(x)								   ((x) - 2)
#define IS_ANALOG(x)					((x) >= 14 && (x) <= 19)
#define IS_DIGITAL(x)							 (!IS_ANALOG(x))

class PE {

public:
	PE(void) {
		for (int i = 0; i < TOTAL_PINS; i++) {
			_pinValues[i] = 0;
			_pinModes[i] = 0;
		}
	}
	~PE(void) {}
	
	unsigned char getMode(unsigned int);
	void setMode(unsigned int, unsigned char);
	unsigned short getValue(unsigned int);
	void setValue(unsigned int, unsigned short);
	long getMillis(void);
	void delayMs(unsigned long);

private:
	
	unsigned short _pinValues[TOTAL_PINS];
	unsigned char _pinModes[TOTAL_PINS];

};

