running programs
run: aString
	self compile: aString 
		thenDo: [:program | 
			protocol run: program.
			self rebuild]