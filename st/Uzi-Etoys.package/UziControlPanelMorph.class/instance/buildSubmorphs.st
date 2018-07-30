building
buildSubmorphs
	self addMorphBack: self buildButtons;
		addMorphBack: self buildConnectionPanel;
		addMorphBack: self buildFreeRAMPanel;
		addMorphBack: self buildPinsPanel;
		addMorphBack: self buildGlobalsPanel;
		addMorphBack: self buildScriptsPanel