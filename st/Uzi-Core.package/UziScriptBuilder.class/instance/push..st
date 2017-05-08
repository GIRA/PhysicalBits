instruction creation
push: arg
	^ args detect: [:each | each name = arg]
		ifFound: [:variable |
			self addInstruction: (UziReadLocalInstruction arg: variable)]
		ifNone: [| variable |
			variable := program registerVariable: arg.
			self addInstruction: (UziPushInstruction arg: variable)]