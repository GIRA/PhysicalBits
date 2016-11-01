accessing
value: newValue
	valueChanged ifNotNil: [valueChanged value: key value: newValue]