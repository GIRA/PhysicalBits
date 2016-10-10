building
buildPinsList
	| pinsList pins |
	pinsList := self newColumn.
	pins := self pinsReporting.
	(pinTexts keys copyWithoutAll: pins)
		do: [:key |
			(pinTexts at: key) delete.
			pinTexts removeKey: key].
	pins
		ifEmpty: [pinsList addMorphBack: '  *no pins enabled*' asMorph]
		ifNotEmpty: [pins asSortedCollection 
			do: [:pin || m |
				m := pinTexts at: pin
					ifAbsentPut: [UziAssociationMorph new key: pin].
				pinsList addMorphBack: m]].
	^ pinsList