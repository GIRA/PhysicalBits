building
buildPinsPanel
	^ self newColumn
		addMorphBack: (self newRow addMorphBack: (self newLabel: 'Pins:'));
		addMorphBack: self buildPinsList
		