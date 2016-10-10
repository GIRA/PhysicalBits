building
buildGlobalsList
	| globalsList globals |
	globalsList := self newColumn.
	globals := self globals.
	globalTexts := globalTexts 
		associationsSelect: [:pair | globals includes: pair key].
	globals 
		ifEmpty: [globalsList addMorphBack: '  *no globals available*' asMorph]
		ifNotEmpty: [globals asSortedCollection 
			do: [:var || m |
				m := globalTexts at: var 
					ifAbsentPut: [UziAssociationMorph new key: var asSymbol].
				globalsList addMorphBack: m]].
	^ globalsList