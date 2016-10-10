literals
numberLiteral
	^ super numberLiteral
		==> [:token | UziNumberNode value: token inputValue asNumber]