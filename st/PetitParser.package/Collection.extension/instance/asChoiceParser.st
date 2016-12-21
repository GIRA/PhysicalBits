*petitparser-core-converting
asChoiceParser
	^ PPChoiceParser withAll: (self collect: [ :each | each asParser ])