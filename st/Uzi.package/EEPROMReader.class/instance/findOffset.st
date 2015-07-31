initializing
findOffset
	| index next peek firstBegin lastBegin boxedIndex beginMark endMark break |
	beginMark := self class beginMark.
	endMark := self class endMark.
	firstBegin := lastBegin := nil.
	index := 0.
	boxedIndex := [index \\ eeprom size].
	peek := [eeprom read: boxedIndex value].
	next := [peek ensure: [index := index + 1]].
		
	[
		"Look for begin mark"
		break := false.
		[break] whileFalse: [
			index >= eeprom size 
				ifTrue: [	"We reached the end" ^ 0].		
			next value = beginMark
				ifTrue: [peek value = beginMark
					ifTrue: [	"The begin mark has been escaped. Ignore"
						next value]
					ifFalse: [break := true]]].
	
		lastBegin := index - 1.
		firstBegin ifNil: [firstBegin := lastBegin].
		
		"Look for end mark"
		break := false.
		[break] whileFalse: [
			boxedIndex value = firstBegin
				ifTrue: ["We reached our tail" ^ 0].			
			next value = endMark
				ifTrue: [peek value = endMark
					ifTrue: [	"The end mark has been escaped. Ignore"
						next value]
					ifFalse: [break := true]]]
			
	] doWhileTrue: [peek value = beginMark].
		
	^ lastBegin