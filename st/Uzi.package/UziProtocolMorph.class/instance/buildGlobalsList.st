building
buildGlobalsList
	| globalsList |
	globalsList := self newColumn.
	globalTexts := Dictionary new.
	self globals asSortedCollection do: [:var || varText |
		varText := (self getGlobal: var) asString asMorph.
		globalTexts at: var put: varText.
		globalsList addMorphBack: (self newRow
			addMorphBack: ('  #{1} -> ' format: { var }) asMorph;
			addMorphBack: varText)].
	^ globalsList