building
buildPinsPanel
	^ self newColumn
		addMorphBack: (self newRow addMorphBack: 'Pins:' asMorph);
		addMorphBack: self buildPinsList
		