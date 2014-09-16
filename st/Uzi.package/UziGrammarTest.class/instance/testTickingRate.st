as yet unclassified
testTickingRate
	| pp d h m s |
	pp := UziGrammar new tickingRate.
	s := pp parse: '1 / s'.
	m := pp parse: '10/m'.
	h := pp parse: '100/ h'.
	d := pp parse: '1000 /d'.
	self assert: (s first inputValue = '1' and: [s second = $/ and: [s third inputValue = 's']]).
	self assert: (m first inputValue = '10' and: [m second = $/ and: [m third inputValue = 'm']]).
	self assert: (h first inputValue = '100' and: [h second = $/ and: [h third inputValue = 'h']]).
	self assert: (d first inputValue = '1000' and: [d second = $/ and: [d third inputValue = 'd']]).