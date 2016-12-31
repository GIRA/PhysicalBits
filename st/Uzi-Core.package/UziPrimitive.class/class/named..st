instance creation
named: primitiveName	
	^ self primSpec
		detect: [:spec | spec first = primitiveName]
		ifFound: [:spec || name code expr |
			name := spec first.
			code := spec second.
			expr := spec third.
			self name: name code: code isExpression: expr]
		ifNone: [UziPrimitiveNotFoundError signal: primitiveName]