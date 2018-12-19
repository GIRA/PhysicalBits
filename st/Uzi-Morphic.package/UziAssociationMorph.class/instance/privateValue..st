accessing
privateValue: newValue
	value := newValue.
	valueText contents: (value ifNotNil: [value asFloat]) printString