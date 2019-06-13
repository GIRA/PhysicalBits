receiving
processTickingScripts
	| count values watchers |
	count := serial next.
	values := (1 to: count) collect: [:ign | serial next].
	watchers := Dictionary new.
	
	currentProgram ifNil: [^ self].
	count ~= currentProgram scripts size ifTrue: [^ self].
	
	values withIndexDo: [:next :index || script isRunning errorCode |
		script := currentProgram scripts at: index.
		isRunning := (next bitAnd: 2r10000000) > 0.
		errorCode := next bitAnd: 2r01111111.
		watchers
			at: script 
			put: (UziScriptWatcher
				script: script
				isRunning: isRunning
				errorCode: errorCode).
		errorCode > 0 ifTrue: [| errorMsg |
			errorMsg := self errorMsgForCode: errorCode.			
			scriptWatchers
				at: script
				ifPresent: [:w | w errorCode ~= errorCode
					ifTrue: [Uzi logger 
						warning: '{1} detected on script "{2}". The script has been stopped.'
						format: { errorMsg . script name };
						flush]]]].
	scriptWatchers := watchers