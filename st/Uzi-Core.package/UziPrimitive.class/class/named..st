instance creation
named: primitiveName	
	^ self primSpec
		detect: [:spec | spec first = primitiveName]
		ifFound: [:spec || name code stackTransition |
			name := spec first.
			code := spec second.
			stackTransition := spec third.
			self name: name code: code stackTransition: stackTransition]
		ifNone: [UziPrimitiveNotFoundError signal: primitiveName]