#pragma once
#include <inttypes.h>

class LiquidCrystal_I2C {
public:
	uint8_t addr;
	uint8_t cols;
	uint8_t rows;

	LiquidCrystal_I2C(uint8_t lcd_Addr, uint8_t lcd_cols, uint8_t lcd_rows) {
		addr = lcd_Addr;
		cols = lcd_cols;
		rows = lcd_rows;
	}
	void begin(uint8_t cols, uint8_t rows, uint8_t charsize = 0) {}
	void clear() {}
	void home() {}
	void noDisplay() {}
	void display() {}
	void noBlink() {}
	void blink() {}
	void noCursor() {}
	void cursor() {}
	void scrollDisplayLeft() {}
	void scrollDisplayRight() {}
	void printLeft() {}
	void printRight() {}
	void leftToRight() {}
	void rightToLeft() {}
	void shiftIncrement() {}
	void shiftDecrement() {}
	void noBacklight() {}
	void backlight() {}
	void autoscroll() {}
	void noAutoscroll() {} 
	void createChar(uint8_t, uint8_t[]) {}
	void setCursor(uint8_t, uint8_t) {} 
	void write(uint8_t) {}
	void command(uint8_t) {}
	void init() {}

	////compatibility API function aliases
	void blink_on() {}						// alias for blink()
	void blink_off() {}       					// alias for noBlink()
	void cursor_on() {}      	 					// alias for cursor()
	void cursor_off() {}      					// alias for noCursor()
	void setBacklight(uint8_t new_val) {}				// alias for backlight() and nobacklight()
	void load_custom_character(uint8_t char_num, uint8_t *rows) {}	// alias for createChar()
	void printstr(const char[]) {}
	 
	// Print.h
	void print(float) {}
};
