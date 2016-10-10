drawing
drawOn: aCanvas
	super drawOn: aCanvas.
	eeprom ifNotNil: [
		self drawEEPROMUsageOn: aCanvas;
			drawStatisticsOn: aCanvas]