button actions
exploreMenu
	(UIManager default 
		chooseFrom: #('Controller' 'Server' 'Running Program' 'Current Program')
		values: { controller . server . controller runningProgram . controller currentProgram })
		ifNotNil: [:option | option explore]