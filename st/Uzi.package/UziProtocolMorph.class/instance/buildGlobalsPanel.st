building
buildGlobalsPanel
	^ self newColumn
		addMorphBack: (self newRow addMorphBack: 'Globals:' asMorph);
		addMorphBack: self buildGlobalsList
		