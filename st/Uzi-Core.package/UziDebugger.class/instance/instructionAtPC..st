accessing
instructionAtPC: anInteger
	| instructions |
	anInteger ifNil: [^ nil].
	instructions := program scripts inject: #() into: [:subTotal :next | subTotal , next instructions].
	^ instructions at: anInteger + 1 ifAbsent: nil