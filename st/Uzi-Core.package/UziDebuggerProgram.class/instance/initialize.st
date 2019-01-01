initialize-release
initialize
	super initialize.
	scripts := program scripts collect: [:script | UziDebuggerScript on: script]