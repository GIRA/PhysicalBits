private
build: aParser right: aChoiceParser
	^ (aParser separatedBy: aChoiceParser) foldRight: [ :a :op :b | op first value: a value: op second value: b ]