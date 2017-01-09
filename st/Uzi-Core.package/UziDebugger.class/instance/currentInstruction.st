accessing
currentInstruction
	^ pc ifNotNil: [| instructions |
		instructions := protocol scripts inject: #() into: [:subTotal :next | subTotal , next instructions].
		instructions at: pc + 1 ifAbsent: nil]