drawing
drawEEPROMUsageOn: aCanvas
	| bottom height maxCounts left |
	maxCounts := eeprom writeLimit.
	left := self left.
	bottom := self bottom.
	height := self height.
	0 to: eeprom size - 1 do: [:i || count h |
		count := eeprom writesAt: i.
		h := (count / maxCounts * height) rounded min: height.
		aCanvas 
			line: (left + i + 1) @ bottom
			to: (left + i + 1) @ (bottom - h)
			width: 1
			color: (count >= maxCounts ifTrue: [Color red] ifFalse: [Color green])
		]