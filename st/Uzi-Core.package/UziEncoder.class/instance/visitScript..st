encoding scripts
visitScript: anUziScript 
	script := anUziScript.
	self writeScriptHeader: anUziScript;
		writeLocals: anUziScript;
		writeInstructions: anUziScript