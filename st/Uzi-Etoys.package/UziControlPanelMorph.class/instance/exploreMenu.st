button actions
exploreMenu
	(UIManager default 
		chooseFrom: #('Protocol' 'Server')
		values: { protocol . server })
		ifNotNil: [:option | option explore]