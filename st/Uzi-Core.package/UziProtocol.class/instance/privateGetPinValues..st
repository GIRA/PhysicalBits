accessing-pins
privateGetPinValues: pinNumber
	| list |
	list := pinValues at: pinNumber ifAbsentPut: [OrderedCollection new].
	list ifNotEmpty: [| begin |
		begin := list last key - 10000.
		list removeAllSuchThat: [:kvp | kvp key < begin]].
	^ list