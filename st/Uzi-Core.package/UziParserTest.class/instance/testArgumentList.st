tests
testArgumentList
	| pp args |
	pp := parser argumentList.
	args := pp parse: ':a 	:b :c  |'.
	self assert: 3 equals: args size.
	self assert: #a equals: args first name.
	self assert: #b equals: args second name.
	self assert: #c equals: args third name