drawing
drawStatisticsOn: aCanvas
	| pos |
	pos := -10.
	aCanvas 
		drawString: ('Usage: {1} %' format:  {((eeprom use * 100) printShowingDecimalPlaces: 2) asString}) 
		at: self topLeft + (10 @ (pos := pos + 20))
		font: Preferences windowTitleFont
		color: Color black;
		
		drawString: ('Programs: {1}' format:  {programs}) 
		at: self topLeft + (10 @ (pos := pos + 20))
		font: Preferences windowTitleFont
		color: Color black;
		
		drawString: ('Avg program size: {1} bytes' format:  {self avgProgramSize printShowingDecimalPlaces: 2}) 
		at: self topLeft + (10 @ (pos := pos + 20))
		font: Preferences windowTitleFont
		color: Color black;
		
		drawString: ('EEPROM size: {1} bytes' format:  {eeprom size}) 
		at: self topLeft + (10 @ (pos := pos + 20))
		font: Preferences windowTitleFont
		color: Color black;
		
		drawString: ('Write limit: {1}' format:  {eeprom writeLimit}) 
		at: self topLeft + (10 @ (pos := pos + 20))
		font: Preferences windowTitleFont
		color: Color black