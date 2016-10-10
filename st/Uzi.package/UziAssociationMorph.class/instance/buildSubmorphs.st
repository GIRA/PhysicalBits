initialize
buildSubmorphs
	self addMorphBack: (keyText := key printString asMorph);
		addMorphBack: ' -> ' asMorph;
		addMorphBack: (valueText := value asString asMorph)