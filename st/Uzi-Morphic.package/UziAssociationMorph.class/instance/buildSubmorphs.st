initialize
buildSubmorphs
	self 
		addMorphBack: self buildViewerButton;
		addMorphBack: (keyText := key asString asMorph);
		addMorphBack: '->' asMorph;
		addMorphBack: (valueText := value asString asMorph)