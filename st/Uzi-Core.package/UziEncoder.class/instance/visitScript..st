encoding scripts
visitScript: anUziScript 
	script := anUziScript.
	self writeScriptHeader: anUziScript;
		writeInstructions: anUziScript