initialize-release
initialize
	super initialize.
	self at: #actions 
		put: (RESTActionMap new
			at: #continue do: [:req | self processContinue: req];
			at: #over do: [:req | self processOver: req];
			at: #into do: [:req | self processInto: req];
			at: #out do: [:req | self processOut: req];
			at: #next do: [:req | self processNext: req];
			at: #breakpoints do: [:req | self processBreakpoints: req]
			yourself)