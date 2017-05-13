instruction creation
push: arg
	^ args, locals detect: [:each | each name = arg]
		ifFound: [:variable |
			self addInstruction: (UziReadLocalInstruction arg: variable)]
		ifNone: [| variable |
			variable := program registerGlobal: arg.
			self addInstruction: (UziPushInstruction arg: variable)]