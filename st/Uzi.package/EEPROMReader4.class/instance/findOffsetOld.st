initializing
findOffsetOld
	| beginMark endMark firstBegin increment index lastBegin nextIndex |
	beginMark := self class beginMark.
	endMark := self class endMark.
	index := 0.
	firstBegin := lastBegin := nil.
	nextIndex := [index + 1 \\ eeprom size].
	increment := [index := nextIndex value].
	[[(eeprom read: index) ~= beginMark
		and: [index < eeprom size]] 
			whileTrue: increment.
	firstBegin ifNil: [firstBegin := index].
	lastBegin := index.
	[(eeprom read: index) ~= endMark
		and: [index ~= firstBegin]]
			whileTrue: increment]
		doWhileTrue: [(eeprom read: nextIndex value) = beginMark].
	
	.
	
		
	