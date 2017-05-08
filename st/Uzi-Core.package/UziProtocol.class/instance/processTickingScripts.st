receiving
processTickingScripts
	| byteSize ticking count |
	count := serial next.
	byteSize := (count / 8) ceiling.
	ticking := Array streamContents: [:stream |
		byteSize timesRepeat: [| byte |
			byte := serial next.
			0 to: 7 do: [:i | stream nextPut: ((byte >> i) bitAnd: 1) = 1]
			]].
	tickingScripts removeAll.
	(currentProgram notNil and: [count = currentProgram scripts size])
		ifTrue: [currentProgram scripts withIndexDo: [:script :index |
			(ticking at: index) ifTrue: [tickingScripts add: script]]]