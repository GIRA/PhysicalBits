building
buildFreeRAMPanel
	^ self newColumn 
		addMorphBack: (self newRow
			addMorphBack: (self newLabel: 'Available RAM:');
			addMorphBack: (freeRAMText := self freeRAM asString asMorph))