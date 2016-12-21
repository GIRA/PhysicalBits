factory-chars
char: aCharacter
	^ self expect: aCharacter message: (String with: $" with: aCharacter with: $") , ' expected'