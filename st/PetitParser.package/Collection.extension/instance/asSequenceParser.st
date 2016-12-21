*petitparser-core-converting
asSequenceParser
	^ PPSequenceParser withAll: (self collect: [ :each | each asParser ])