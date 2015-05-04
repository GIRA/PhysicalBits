initializing
findOffset
	| index next peek firstBegin lastBegin boxedIndex |
	firstBegin := lastBegin := nil.
	index := 0.
	boxedIndex := [index \\ eeprom size].
	peek := [eeprom read: boxedIndex value].
	next := [peek ensure: [index := index + 1]].
	
	[[index >= eeprom size ifTrue: [^ 0].
	next value = self class beginMark] whileFalse.
	
	lastBegin := index - 1.
	firstBegin ifNil: [firstBegin := lastBegin].

	[boxedIndex value = firstBegin ifTrue: [^ 0].
	next value = self class endMark] whileFalse]
		doWhileTrue: [peek value = self class beginMark].	
	
	^ index