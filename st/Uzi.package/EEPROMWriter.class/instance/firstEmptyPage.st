initializing
firstEmptyPage
	0 to: eeprom size - 1 by: 8 do: [:i |
		(eeprom read: i) < 255 ifTrue: [^ i]].
	^ 0