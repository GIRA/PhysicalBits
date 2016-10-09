building
buildPinsList
	| pinsList |
	pinsList := self newColumn.
	pinTexts := Dictionary new.
	self pinsReporting asSortedCollection do: [:pin || pinText |
		pinText := (self getPinValue: pin) asString asMorph.
		pinTexts at: pin put: pinText.
		pinsList addMorphBack: (self newRow
			addMorphBack: ('  {1} -> ' format: { pin }) asMorph;
			addMorphBack: pinText)].
	^ pinsList