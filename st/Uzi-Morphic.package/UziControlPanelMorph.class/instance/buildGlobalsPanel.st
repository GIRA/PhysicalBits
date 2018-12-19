building
buildGlobalsPanel
	^ self newColumn
		addMorphBack: (self newRow addMorphBack: (self newLabel: 'Globals:'));
		addMorphBack: self buildGlobalsList
		