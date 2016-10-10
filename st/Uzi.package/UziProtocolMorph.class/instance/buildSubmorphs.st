building
buildSubmorphs
	self addMorphBack: self buildButtons;
		addMorphBack: self buildConnectionPanel;
		addMorphBack: self buildPinsPanel;
		addMorphBack: self buildGlobalsPanel