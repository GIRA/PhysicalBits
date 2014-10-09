setup
setUp
	"Shortcuts to create bytecodes"
	pushLit := UziPushLiteralBytecode.
	primCall := UziPrimitiveCallBytecode.
	end := UziEndBytecode new..
	jnz := UziJNZBytecode.
	jmp := UziJMPBytecode.
	"... and primitives"
	prim := UziPrimitive