private
build: aParser left: aChoiceParser
	^ (aParser separatedBy: aChoiceParser) foldLeft: [ :a :op :b | op first value: a value: op second value: b ]