initialize-release
initialize
	super initialize.
	self at: #reporting
		put: (RESTActionMap new
			at: #activate do: [:req | self processActivateReporting: req];
			at: #deactivate do: [:req | self processDeactivateReporting: req];
			at: #toggle do: [:req | self processToggleReporting: req];
			yourself)