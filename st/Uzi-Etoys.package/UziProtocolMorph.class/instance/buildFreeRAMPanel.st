building
buildFreeRAMPanel
	^ self newColumn 
		addMorphBack: (self newRow
			addMorphBack: (self newLabel: 'Free RAM:');
			addMorphBack: (freeRAMText := self freeRAM asString asMorph))