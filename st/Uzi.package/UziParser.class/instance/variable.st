literals
variable
	^ super variable
		==> [:token | UziVariableNode named: token inputValue]