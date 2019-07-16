receiving
processTickingScripts
	| count values watchers update |
	count := serial next.
	values := (1 to: count) collect: [:ign | serial next].
	watchers := Dictionary new.
	
	runningProgram ifNil: [^ self].
	count ~= runningProgram scripts size ifTrue: [^ self].
	
	update := false.
	values withIndexDo: [:next :index || script isRunning errorCode |
		script := runningProgram scripts at: index.
		isRunning := (next bitAnd: 2r10000000) > 0.
		errorCode := next bitAnd: 2r01111111.
		watchers
			at: script 
			put: (UziScriptWatcher
				script: script
				isRunning: isRunning
				errorCode: errorCode).
		scriptWatchers at: script
			ifPresent: [:w |
				w isRunning ~= isRunning 
					ifTrue: [update := true].
				w errorCode ~= errorCode
					ifTrue: [update := true.
						errorCode > 0
							ifTrue: [Uzi logger 
								warning: '{1} detected on script "{2}". The script has been stopped.'
								format: { self errorMsgForCode: errorCode . script name };
								flush]]]
			ifAbsent: [update := true.
				errorCode > 0
					ifTrue: [Uzi logger 
						warning: '{1} detected on script "{2}". The script has been stopped.'
						format: { self errorMsgForCode: errorCode . script name };
						flush]]].
	scriptWatchers := watchers.
	update ifTrue: [self triggerEvent: UziEvents reportingUpdate]