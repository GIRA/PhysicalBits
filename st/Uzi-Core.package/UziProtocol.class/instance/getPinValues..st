accessing-pins
getPinValues: pinNumber
	| list now |
	list := pinValues at: pinNumber ifAbsentPut: [OrderedCollection new].
	now := DateAndTime now.
	list removeAllSuchThat: [:kvp | (now - kvp key) seconds > 10].
	^ list