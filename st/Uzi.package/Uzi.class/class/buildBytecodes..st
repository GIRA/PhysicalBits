private
buildBytecodes: aBlockClosure 
	| builder |
	builder := UziBytecodeBuilder new.
	^ (aBlockClosure value: builder) bytecodes