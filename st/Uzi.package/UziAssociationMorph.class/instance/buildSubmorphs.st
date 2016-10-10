initialize
buildSubmorphs
	self 
		addMorphBack: self buildViewerButton;
		addMorphBack: (keyText := key printString asMorph);
		addMorphBack: '->' asMorph;
		addMorphBack: (valueText := value asString asMorph)