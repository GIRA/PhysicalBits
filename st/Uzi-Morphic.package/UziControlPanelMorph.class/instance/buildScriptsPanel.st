building
buildScriptsPanel
	^ self newColumn
		addMorphBack: (self newRow addMorphBack: (self newLabel: 'Scripts:'));
		addMorphBack: self buildScriptsList
		