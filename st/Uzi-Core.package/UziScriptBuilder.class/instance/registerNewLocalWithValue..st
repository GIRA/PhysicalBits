variables
registerNewLocalWithValue: value
	| name |
	name := (((1 to: locals size + 1) collect: [:n | '@', n asString])
		copyWithoutAll: (locals collect: [:var | var name]))
		first.
	^ self registerLocal: name value: value