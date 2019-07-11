button actions
exploreMenu
	(UIManager default 
		chooseFrom: #('Protocol' 'Server' 'Running Program' 'Current Program')
		values: { protocol . server . protocol runningProgram . protocol currentProgram })
		ifNotNil: [:option | option explore]