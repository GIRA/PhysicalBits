accessing
next
	| val |
	[val := eeprom read: self actualIndex.
	^ (val = self class beginMark
		or: [val = self class endMark])
		ifTrue: [self next]
		ifFalse: [val]]
		ensure: [self incrementPosition]