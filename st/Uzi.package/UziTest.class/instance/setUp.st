setup
setUp
	"Shortcuts to create bytecodes"
	push := UziPushBytecode.
	prim := UziPrimitiveCallBytecode.
	end := UziEndBytecode new..
	jnz := UziJNZBytecode.
	jmp := UziJMPBytecode