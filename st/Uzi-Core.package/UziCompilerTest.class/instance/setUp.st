private
setUp
	push := UziPushInstruction.
	pop := UziPopInstruction.
	prim := UziPrimitiveCallInstruction.
	jz := UziJZInstruction.
	jnz := UziJNZInstruction.
	jmp := UziJMPInstruction.
	start := UziStartScriptInstruction.
	stop := UziStopScriptInstruction.
	writeLocal := UziWriteLocalInstruction.
	readLocal := UziReadLocalInstruction.
	call := UziScriptCallInstruction 