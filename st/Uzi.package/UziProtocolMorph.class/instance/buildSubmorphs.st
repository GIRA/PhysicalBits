building
buildSubmorphs
	self addMorphBack: self buildButtons;
		addMorphBack: self buildConnectionPanel.
	self pinsReporting 
		ifNotEmpty: [self addMorphBack: self buildPinsPanel].
	self globals
		ifNotEmpty: [self addMorphBack: self buildGlobalsPanel]