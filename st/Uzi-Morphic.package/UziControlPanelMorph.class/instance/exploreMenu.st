button actions
exploreMenu
	(UIManager default 
		chooseFrom: #('Protocol' 'Server' 'Program')
		values: { protocol . server . protocol currentProgram })
		ifNotNil: [:option | option explore]