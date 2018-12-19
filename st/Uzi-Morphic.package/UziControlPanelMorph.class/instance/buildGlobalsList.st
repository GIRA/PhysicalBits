building
buildGlobalsList
	| globalsList globals |
	globalsList := self newColumn.
	globals := self globals intersection: self globalsReporting.	
	(globalTexts keys copyWithoutAll: globals)
		do: [:key |
			(globalTexts at: key) delete.
			globalTexts removeKey: key].
	globals 
		ifEmpty: [globalsList addMorphBack: '  *no globals available*' asMorph]
		ifNotEmpty: [globals asSortedCollection 
			do: [:var || m |
				m := globalTexts at: var 
					ifAbsentPut: [UziAssociationMorph new 
						key: var;
						name: ('Global {1}' format: { var });
						valueChanged: [:key :val |
							 self setGlobal: key value: val]].
				globalsList addMorphBack: m]].
	^ globalsList